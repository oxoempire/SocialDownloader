# Copyright (c) 2026 Manu Cabello (oxoempire)
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

import yt_dlp
import os

def download_video(url, output_dir, progress_callback, cookies_path=None):
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
    if cookies_path and os.path.exists(cookies_path):
        ydl_opts['cookiefile'] = cookies_path
    
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.download([url])
