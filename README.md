<h1 align="center">
  <br>
  Social Downloader (DownloadeRS)
  <br>
</h1>

<h4 align="center">Una aplicación para Android que te permite descargar videos de tus redes sociales favoritas directamente a tu galería de manera rápida y sin complicaciones.</h4>

<p align="center">
  <a href="#características">Características</a> •
  <a href="#plataformas-soportadas">Plataformas Soportadas</a> •
  <a href="#cómo-funciona">Cómo Funciona</a> •
  <a href="#tecnologías-y-librerías">Tecnologías</a> •
  <a href="#instalación-y-compilación">Instalación</a> •
  <a href="#contacto">Contacto</a>
</p>

---

## 🚀 Características

- **Descarga Directa**: Descarga videos pegando la URL de forma sencilla.
- **Intercepción de Compartir**: Si ves un video en una red social, solo dale a "Compartir con..." y selecciona esta aplicación para rellenar la URL automáticamente.
- **Selección de Tema**: Soporte nativo para modo **Claro**, **Oscuro** o automático según el **Sistema**.
- **Acciones Post-Descarga**: Una vez descargado, puedes **Abrir** el video en tu reproductor favorito o **Compartirlo** directamente con WhatsApp, Telegram u otras apps.
- **Seguridad**: Solicita los permisos necesarios de Android dinámicamente y guarda los archivos directamente en tu galería (carpeta Movies/SocialDownloader) usando `MediaStore`.

## 🌐 Plataformas Soportadas

Gracias al poderoso motor de descargas bajo el capó, esta aplicación soporta contenido de las redes más populares:
- ✅ YouTube
- ✅ Facebook
- ✅ Instagram (Reels/Videos)
- ✅ X (anteriormente Twitter)
- ✅ TikTok

## 🧠 Cómo Funciona

Social Downloader es una app **híbrida**. La interfaz está completamente desarrollada de forma nativa en **Kotlin**, ofreciendo una experiencia fluida e integrada en Android. 

Por debajo, el motor de descargas se apoya en **Python** ejecutándose de forma incrustada dentro de Android, encargándose del procesamiento complejo de enlaces y la obtención del video en alta calidad de manera eficiente. Todo este proceso es transparente para el usuario.

## 🛠 Tecnologías y Librerías

El proyecto hace uso de herramientas modernas para el ecosistema Android:

- **[Kotlin](https://kotlinlang.org/)**: Lenguaje principal de la aplicación.
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)**: Todo el diseño UI (botones, inputs, menús) está construido con el kit de herramientas declarativo más moderno de Android, haciendo uso de `Material 3`.
- **[Chaquopy](https://chaquo.com/chaquopy/)**: Framework vital que permite ejecutar scripts y paquetes de Python de forma embebida dentro de una aplicación Android.
- **[yt-dlp](https://github.com/yt-dlp/yt-dlp)**: Extraordinario motor Open-Source de línea de comandos en Python que se encarga de analizar e interceptar el archivo multimedia final desde la URL proporcionada.
- **[Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)**: Manejo asíncrono para mantener la aplicación sin bloqueos mientras se realizan las descargas de gran tamaño en segundo plano (`Dispatchers.IO`).

## 💻 Instalación y Compilación

Si deseas compilar la aplicación por tu cuenta, tienes a tu disposición el script automatizado para Linux:

1. **Clona este repositorio**:
   ```bash
   git clone https://github.com/TuUsuario/DownloadeRS.git
   cd DownloadeRS
   ```

2. **Compila usando el script automatizado**:
   El proyecto incluye un script mágico llamado `build_apk.sh` que descarga el SDK de Android, Java (JDK 17) y Gradle necesarios de manera automática y construye tu app.
   ```bash
   bash build_apk.sh
   ```

3. **¡Listo!**: 
   Verás un mensaje de éxito indicando `Success! APK compiled at ./SocialDownloader.apk`. Simplemente copia ese `.apk` a tu dispositivo Android, instálalo y comienza a descargar videos.

## ✉️ Contacto

Creado con dedicación por **Manu Cabello**. 
- 📧 Email: [ursus.empirium@gmail.com](mailto:ursus.empirium@gmail.com)

---
*Nota: Recuerda siempre respetar los derechos de autor al descargar contenido de las redes sociales. Esta herramienta es para uso y archivo personal.*
