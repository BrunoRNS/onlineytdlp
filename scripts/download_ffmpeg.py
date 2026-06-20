#!/usr/bin/env python3
"""
Cross-platform FFmpeg downloader for Nuitka bundling.
Downloads FFmpeg binaries appropriate for the current OS and extracts 
to target directory.
"""

import os
import sys
import platform
import urllib.request
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

def download_file(url: str, destination: str):
    """Download file from URL"""
    print(f"Downloading {url}...")
    try:
        urllib.request.urlretrieve(url, destination)
        print(f"Downloaded to {destination}")
    except Exception as e:
        print(f"Error downloading {url}: {e}")
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
    """Download FFmpeg for Linux"""
    os.makedirs(bin_dir, exist_ok=True)
    
    url = "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz"
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
    """Download FFmpeg for macOS"""
    os.makedirs(bin_dir, exist_ok=True)
    
    url = "https://evermeet.cx/ffmpeg/download/zip"
    temp_file = "/tmp/ffmpeg.zip"
    temp_extract = "/tmp/ffmpeg_extract"
    
    download_file(url, temp_file)
    os.makedirs(temp_extract, exist_ok=True)
    extract_zip(temp_file, temp_extract)
    
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

def download_ffmpeg_windows(bin_dir: str, arch: str = "x86_64"):
    """Download FFmpeg for Windows (32-bit or 64-bit)"""
    os.makedirs(bin_dir, exist_ok=True)
    
    # Determine architecture
    if arch == "x86" or arch == "i386" or arch == "32":
        url = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win32-gpl.zip"
        print("Downloading FFmpeg for Windows 32-bit...")
    else:
        url = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip"
        print("Downloading FFmpeg for Windows 64-bit...")
    
    temp_file = "C:\\Windows\\Temp\\ffmpeg.zip" if sys.platform == "win32" else "/tmp/ffmpeg.zip"
    temp_extract = "C:\\Windows\\Temp\\ffmpeg_extract" if sys.platform == "win32" else "/tmp/ffmpeg_extract"
    
    download_file(url, temp_file)
    os.makedirs(temp_extract, exist_ok=True)
    extract_zip(temp_file, temp_extract)
    
    # Find ffmpeg.exe and ffprobe.exe in extracted files
    for root, _, files in os.walk(temp_extract):
        for file in files:
            if file in ['ffmpeg.exe', 'ffprobe.exe']:
                src = os.path.join(root, file)
                dst = os.path.join(bin_dir, file)
                shutil.copy2(src, dst)
                print(f"Copied {file} to {bin_dir}")
    
    # Cleanup
    shutil.rmtree(temp_extract, ignore_errors=True)
    if os.path.exists(temp_file):
        os.remove(temp_file)

def main():
    """Main entry point"""
    if len(sys.argv) < 2:
        print("Usage: download_ffmpeg.py <output_directory> [architecture]")
        sys.exit(1)
    
    bin_dir = sys.argv[1]
    arch = sys.argv[2] if len(sys.argv) > 2 else None
    system, machine, bits = get_system_info()
    
    print(f"System: {system}, Machine: {machine}, Bits: {bits}")
    print(f"Target directory: {bin_dir}")
    
    # Auto-detect architecture if not provided
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
