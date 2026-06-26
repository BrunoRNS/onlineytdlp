#!/usr/bin/env python3
"""
Cross-platform FFmpeg downloader for Nuitka bundling.
Downloads FFmpeg binaries from official BtbN builds (Linux/Windows)
and other trusted sources (macOS).
"""

import os
import sys
import time
import platform
import urllib.request
import urllib.error
import shutil
import zipfile
import tarfile
import struct

def get_system_info():
    """Detect current OS and architecture"""
    system = platform.system()
    machine = platform.machine()
    bits = struct.calcsize("P") * 8
    return system, machine, bits

def download_file(url: str, destination: str, retries: int = 3, delay: int = 5):
    """Download file with retry logic"""
    for attempt in range(1, retries + 1):
        try:
            print(f"Downloading {url} (attempt {attempt}/{retries})...")
            urllib.request.urlretrieve(url, destination)
            print(f"Downloaded to {destination}")
            return
        except urllib.error.URLError as e:
            print(f"Error downloading: {e}")
            if attempt < retries:
                print(f"Retrying in {delay} seconds...")
                time.sleep(delay)
            else:
                print("Max retries reached. Exiting.")
                sys.exit(1)

def extract_tar_xz(file_path: str, extract_to: str):
    """Extract tar.xz file"""
    print(f"Extracting {file_path}...")
    try:
        with tarfile.open(file_path, 'r:xz') as tar:
            tar.extractall(extract_to)
        print(f"Extracted to {extract_to}")
    except Exception as e:
        print(f"Error extracting {file_path}: {e}")
        sys.exit(1)

def extract_zip(file_path: str, extract_to: str):
    """Extract zip file"""
    print(f"Extracting {file_path}...")
    try:
        with zipfile.ZipFile(file_path, 'r') as zip_ref:
            zip_ref.extractall(extract_to)
        print(f"Extracted to {extract_to}")
    except Exception as e:
        print(f"Error extracting {file_path}: {e}")
        sys.exit(1)

def download_ffmpeg_linux(bin_dir: str):
    """Download FFmpeg for Linux (static GPL build from BtbN)"""
    os.makedirs(bin_dir, exist_ok=True)
    
    url = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz"
    temp_file = "/tmp/ffmpeg.tar.xz"
    temp_extract = "/tmp/ffmpeg_extract"
    
    download_file(url, temp_file)
    os.makedirs(temp_extract, exist_ok=True)
    extract_tar_xz(temp_file, temp_extract)
    
    # Find ffmpeg and ffprobe in extracted files
    for root, _, files in os.walk(temp_extract):
        for file in files:
            if file in ['ffmpeg', 'ffprobe']:
                src = os.path.join(root, file)
                dst = os.path.join(bin_dir, file)
                shutil.copy2(src, dst)
                os.chmod(dst, 0o755)
                print(f"Copied {file} to {bin_dir}")
    
    # Cleanup
    shutil.rmtree(temp_extract, ignore_errors=True)
    os.remove(temp_file)

def download_ffmpeg_macos(bin_dir: str):
    """Download FFmpeg for macOS (evermeet.cx)"""
    os.makedirs(bin_dir, exist_ok=True)
    
    url = "https://evermeet.cx/ffmpeg/download/zip"
    temp_file = "/tmp/ffmpeg.zip"
    temp_extract = "/tmp/ffmpeg_extract"
    
    download_file(url, temp_file)
    os.makedirs(temp_extract, exist_ok=True)
    extract_zip(temp_file, temp_extract)
    
    for root, _, files in os.walk(temp_extract):
        for file in files:
            if file in ['ffmpeg', 'ffprobe']:
                src = os.path.join(root, file)
                dst = os.path.join(bin_dir, file)
                shutil.copy2(src, dst)
                os.chmod(dst, 0o755)
                print(f"Copied {file} to {bin_dir}")
    
    shutil.rmtree(temp_extract, ignore_errors=True)
    os.remove(temp_file)

def download_ffmpeg_windows(bin_dir: str, arch: str = "x86_64"):
    """Download FFmpeg for Windows (32-bit or 64-bit)"""
    os.makedirs(bin_dir, exist_ok=True)
    
    if arch == "x86" or arch == "i386" or arch == "32":
        url = "https://github.com/defisym/FFmpeg-Builds-Win32/releases/download/latest/ffmpeg-master-latest-win32-gpl.zip"
        print("Downloading FFmpeg for Windows 32-bit (defisym build)...")
    else:
        url = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip"
        print("Downloading FFmpeg for Windows 64-bit...")
    
    temp_file = "/tmp/ffmpeg.zip"  # simplified; Windows Git Bash handles /tmp
    temp_extract = "/tmp/ffmpeg_extract"
    if sys.platform == "win32":
        temp_file = os.environ.get("TEMP", "C:\\Windows\\Temp") + "\\ffmpeg.zip"
        temp_extract = os.environ.get("TEMP", "C:\\Windows\\Temp") + "\\ffmpeg_extract"
    
    download_file(url, temp_file)
    os.makedirs(temp_extract, exist_ok=True)
    extract_zip(temp_file, temp_extract)
    
    for root, _, files in os.walk(temp_extract):
        for file in files:
            if file in ['ffmpeg.exe', 'ffprobe.exe']:
                src = os.path.join(root, file)
                dst = os.path.join(bin_dir, file)
                shutil.copy2(src, dst)
                print(f"Copied {file} to {bin_dir}")
    
    shutil.rmtree(temp_extract, ignore_errors=True)
    if os.path.exists(temp_file):
        os.remove(temp_file)

def main():
    if len(sys.argv) < 2:
        print("Usage: download_ffmpeg.py <output_directory> [architecture]")
        sys.exit(1)
    
    bin_dir = sys.argv[1]
    arch = sys.argv[2] if len(sys.argv) > 2 else None
    system, machine, bits = get_system_info()
    
    print(f"System: {system}, Machine: {machine}, Bits: {bits}")
    print(f"Target directory: {bin_dir}")
    
    if arch is None:
        if system == "Windows":
            arch = "x86" if bits == 32 else "x86_64"
        else:
            arch = "x86_64"
    
    print(f"Architecture: {arch}")
    
    if system == "Linux":
        download_ffmpeg_linux(bin_dir)
    elif system == "Darwin":
        download_ffmpeg_macos(bin_dir)
    elif system == "Windows":
        download_ffmpeg_windows(bin_dir, arch)
    else:
        print(f"Unsupported OS: {system}")
        sys.exit(1)
    
    print("FFmpeg setup complete!")

if __name__ == "__main__":
    main()