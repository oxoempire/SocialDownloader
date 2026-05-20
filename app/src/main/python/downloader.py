import yt_dlp
import os

def download_video(url, output_dir, progress_callback):
    def hook(d):
        if d['status'] == 'downloading':
            try:
                pct_str = d.get('_percent_str', '0.0%').strip().replace('\x1b', '')
                import re
                pct_clean = re.sub(r'\[.*?\]', '', pct_str).replace('%', '').strip()
                if pct_clean.startswith('\x1b'):
                    pct_clean = pct_clean[pct_clean.find('m')+1:]
                pct = float(pct_clean)
                eta = d.get('_eta_str', 'N/A')
                # Calls the Java interface method onProgress
                progress_callback.onProgress(pct, f"Descargando: {pct}% (ETA: {eta})")
            except Exception as e:
                pass
        elif d['status'] == 'finished':
            progress_callback.onProgress(100.0, "Procesando archivo final...")

    ydl_opts = {
        'outtmpl': os.path.join(output_dir, '%(title).50s.%(ext)s'),
        'format': 'best[ext=mp4]/best',
        'progress_hooks': [hook],
        'restrictfilenames': True,
        'quiet': True,
        'no_warnings': True,
    }
    
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.download([url])
