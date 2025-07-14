# YouTube Downloader: A Robust Alternative to yt-dlp

## Motivation

This script was created to address several limitations encountered with yt-dlp and similar tools:

1. **Inconsistent MP3 conversion quality and settings**
2. **Frequent breaking due to YouTube API changes**
3. **Complex installation and dependency management**
4. **Lack of control over audio conversion parameters**
5. **Unexpected behavior with certain video formats and resolutions**

Our solution provides a reliable, focused alternative that prioritizes:

- **Consistent audio quality with specific parameters**
- **Minimal dependencies for easier maintenance**
- **Predictable behavior for common use cases**
- **Simple installation and execution**

## Key Features

### 1. Precision Audio Conversion

- **Strict 32kHz sample rate**
- **Fixed 180kbps bitrate**
- **Clean conversion from source audio**

### 2. Reliable Video Download

- **Always selects highest available resolution**
- **Direct download without re-encoding**
- **Preserves original quality**

### 3. Enhanced Stability

- **Uses pytubefix (actively maintained fork)**
- **Fewer dependencies than alternatives**
- **Graceful error handling**

### 4. Simplified Workflow

- **Single-file executable option**
- **Docker support for containerized environments**
- **Clear command-line interface**

### 5. Debugging Support

- **Detailed logging with --debug flag**
- **Title preview without download**
- **Transparent processing steps**

## Usage Philosophy

We believe in "doing one thing well" - this tool focuses exclusively on:

- **YouTube video downloads (MP4)**
- **YouTube audio conversion (MP3 with specific parameters)**

## Why this over yt-dlp?

While yt-dlp is more feature-rich, this solution provides:

- **Consistent output quality**
- **Fewer breaking changes**
- **Smaller footprint**
- **Predictable behavior**
- **Easier troubleshooting**

The trade-off is narrower feature scope, which we consider an advantage for focused use cases.
