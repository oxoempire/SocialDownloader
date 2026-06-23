/*
 * Copyright (c) 2026 Manu Cabello (oxoempire)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.oxoempire.socialdownloader

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Palette
import android.Manifest
import android.os.Build

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object AppLogger {
    private var logFile: File? = null

    fun init(context: Context) {
        logFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "downloader_log.txt")
        if (logFile?.exists() == false) {
            logFile?.createNewFile()
        }
        log("--- App Started ---")
    }

    fun log(message: String) {
        try {
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val line = "[$time] $message\n"
            logFile?.appendText(line)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogContent(): String {
        return try {
            logFile?.readText() ?: "No hay logs disponibles."
        } catch (e: Exception) {
            "Error leyendo log: ${e.message}"
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        AppLogger.init(this)
        
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
                AppLogger.log("Chaquopy Python 3.11 Iniciado Correctamente.")
            }
        } catch (e: Exception) {
            AppLogger.log("Error al iniciar Chaquopy: ${e.message}")
        }
        
        var initialUrl = ""
        if (intent?.action == Intent.ACTION_SEND) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                val urlRegex = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\((?:[^\\s()<>]+|\\([^\\s()<>]+\\))*\\))+(?:\\((?:[^\\s()<>]+|\\([^\\s()<>]+\\))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))".toRegex()
                val matchResult = urlRegex.find(sharedText)
                initialUrl = matchResult?.value ?: ""
                AppLogger.log("Recibido Intent Share: $initialUrl")
            }
        }

        setContent {
            val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            var themeMode by remember { 
                mutableStateOf(ThemeMode.valueOf(sharedPrefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)) 
            }
            
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Permissions Launcher
            val permissionsLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.entries.all { it.value }
                if (allGranted) {
                    AppLogger.log("Todos los permisos concedidos.")
                } else {
                    AppLogger.log("Algunos permisos fueron denegados.")
                }
            }

            LaunchedEffect(Unit) {
                val permissionsToRequest = mutableListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
                } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if (permissionsToRequest.isNotEmpty()) {
                    permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                }
            }

            MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        initialUrl = initialUrl,
                        themeMode = themeMode,
                        onThemeChanged = { newMode ->
                            themeMode = newMode
                            sharedPrefs.edit().putString("theme_mode", newMode.name).apply()
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun MainScreen(initialUrl: String, themeMode: ThemeMode, onThemeChanged: (ThemeMode) -> Unit) {
    var url by remember { mutableStateOf(initialUrl) }
    var isDownloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("") }
    var downloadedVideoUri by remember { mutableStateOf<Uri?>(null) }
    
    var showAboutDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Acerca de Social Downloader") },
            text = {
                Column {
                    Text("Autor: Manu Cabello")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Versión: 1.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Plataformas soportadas: Facebook, Instagram, X, Tik Tok, Youtube.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { 
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("ursus.empirium@gmail.com"))
                    }) {
                        Text("Copiar email de contacto")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Social Downloader",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Video URL") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDownloading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (url.isNotEmpty() && !isDownloading) {
                        isDownloading = true
                        statusText = "Preparando descarga..."
                        AppLogger.log("Iniciando proceso para URL: $url")
                        coroutineScope.launch {
                            downloadVideo(context, url, 
                                onProgress = { p, status ->
                                    progress = p
                                    statusText = status
                                },
                                onComplete = { uri ->
                                    isDownloading = false
                                    progress = 0f
                                    downloadedVideoUri = uri
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = url.isNotEmpty() && !isDownloading
            ) {
                Text(if (isDownloading) "Descargando..." else "Descargar")
            }
            
            if (isDownloading || statusText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = statusText)
            }

            if (downloadedVideoUri != null && !isDownloading) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(downloadedVideoUri, "video/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Abrir video"))
                    }) {
                        Text("Abrir Video")
                    }
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/*"
                            putExtra(Intent.EXTRA_STREAM, downloadedVideoUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir video"))
                    }) {
                        Text("Compartir")
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Filled.Palette, contentDescription = "Tema")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Sistema") },
                        onClick = { onThemeChanged(ThemeMode.SYSTEM); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Claro") },
                        onClick = { onThemeChanged(ThemeMode.LIGHT); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Oscuro") },
                        onClick = { onThemeChanged(ThemeMode.DARK); expanded = false }
                    )
                }
            }
            IconButton(onClick = { showAboutDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info"
                )
            }
        }
    }
}

interface DownloadCallback {
    fun onProgress(pct: Float, status: String)
}

suspend fun downloadVideo(
    context: Context,
    url: String,
    onProgress: (Float, String) -> Unit,
    onComplete: (Uri?) -> Unit
) {
    withContext(Dispatchers.IO) {
        var finalUri: Uri? = null
        try {
            AppLogger.log("Iniciando motor nativo yt-dlp vía Chaquopy...")
            withContext(Dispatchers.Main) { onProgress(0f, "Iniciando motor Python...") }
            
            val py = Python.getInstance()
            val module = py.getModule("downloader")
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            
            val mainHandler = Handler(Looper.getMainLooper())
            val callback = object : DownloadCallback {
                override fun onProgress(pct: Float, status: String) {
                    mainHandler.post {
                        onProgress(pct, status)
                    }
                }
            }
            
            AppLogger.log("Configurando petición de descarga para: $url")
            withContext(Dispatchers.Main) { onProgress(0f, "Obteniendo información del video...") }
            
            // This is a blocking call in Python, but we are inside Dispatchers.IO
            module.callAttr("download_video", url, downloadDir?.absolutePath, callback)
            
            AppLogger.log("Descarga de Python completada.")
            
            downloadDir?.listFiles()?.forEach { file ->
                if (file.extension == "mp4" || file.extension == "mkv" || file.extension == "webm") {
                    AppLogger.log("Guardando archivo en Galería: ${file.name}")
                    val savedUri = saveToGallery(context, file)
                    if (savedUri != null) {
                        finalUri = savedUri
                    }
                    file.delete()
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(100f, "¡Descarga Completada!")
                onComplete(finalUri)
            }
            AppLogger.log("Proceso exitoso.")
        } catch (e: Exception) {
            AppLogger.log("Excepción durante la descarga: " + e.stackTraceToString())
            withContext(Dispatchers.Main) {
                onProgress(0f, "Error: ${e.message}")
                onComplete(null)
            }
        }
    }
}

fun saveToGallery(context: Context, videoFile: File): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.name)
        put(MediaStore.Video.Media.MIME_TYPE, "video/${videoFile.extension}")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/SocialDownloader")
        }
    }

    val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let {
        context.contentResolver.openOutputStream(it).use { outputStream ->
            FileInputStream(videoFile).use { inputStream ->
                inputStream.copyTo(outputStream!!)
            }
        }
    }
    return uri
}
