# YT-DLP Online Interface

A simple web interface to run yt-dlp online and easily manipulate YouTube videos as MP3 (audio) or MP4 (video) files.

![Interface Preview](preview.png)

## How to Use

1. **Access the application**:
   - If running locally: `http://localhost:8080`
   - Online version: [https://onlineytdlp.onrender.com/](https://onlineytdlp.onrender.com/)

2. **Paste the YouTube link**:
   - Example: `https://www.youtube.com/watch?v=...`

3. **Choose the format**:
   - MP3 for music/podcasts
   - MP4 for videos

4. **Click "Download"**:
   - Wait a few seconds
   - The file will be downloaded automatically

## Features

- Direct conversion to high-quality MP3
- Download high-resolution videos
- Video preview before downloading
- Clean and easy-to-use interface

> **Legal Notice**:  
> Use only for content you have permission to download. Respect the [YouTube Terms of Service](https://www.youtube.com/t/terms).

## For Developers

[See the technical documentation](docs/DOCS.md)

## Makefile Usage

This repository includes a `Makefile` to run tests and builds for both Java and Python.

### Available targets

- `make help` - show help
- `make test` - run Java and Python unit tests
- `make build` - build only the Maven JAR
- `make build-web` - build the Spring Boot web app and publish to `build/web/`
- `make build-web-graal` - build the Spring Boot web app with GraalVM and publish to `build/web/`
- `make build-package` - build desktop package with jpackage to `build/package/` (jpackage + ytdlp)
- `make build-graal` - build native desktop executable with GraalVM to `build/graal/` (graal + ytdlp)
- `make build-python` - compile Python executable with Nuitka to `build/python/`
- `make update-docker` - update `docker/` folder with artifacts for Docker builds (only `build/python/` and `build/web/` are copied)
- `make clean` - clean all build artifacts (including `build/` directory)

### Build Output Structure

All build artifacts are organized in the `build/` directory:

```sh
build/
├── web/           # Spring Boot web app JAR
│   └── videodownloader.jar
├── package/       # jPackage desktop executables (Windows/macOS)
│   └── videodownloader-jpackage
├── graal/         # GraalVM native binaries
│   └── videodownloader-graal
├── python/        # Python executable with bundled FFmpeg
│   └── ytdlp
└── releases/      # Release packages (ZIP files)
    ├── videodownloader-windows-32bit.zip
    ├── videodownloader-windows-64bit.zip
    ├── videodownloader-macos-x64.zip
    └── videodownloader-linux-x64.zip
```

### Key Notes

- **build-python** automatically downloads the appropriate FFmpeg binary for your OS and bundles it with the Nuitka-compiled Python executable.
- **package-* targets** create release ZIPs containing both the executable and Python binary.
- **Windows 32-bit** uses jpackage for the executable (`build/package/`).
- **All other platforms** use GraalVM native image compilations (`build/graal/`).
- **update-docker** copies artifacts to `docker/` folder (not `build/`) for Docker builds.
- The `build/` directory is in `.gitignore` and is cleaned with `make clean`.

## License

This software is licensed under the GNU General Public License v3.0 (GPL v3) and its terms of use and privacy can be found in [AGREEMENTS.md](AGREEMENTS.md) and [PRIVACY.md](PRIVACY.md). By using this software, you agree to the terms of use and privacy described in this file.

## Acknowledgments

> It is normal to receive a 429 too many requests HTTP error when downloading the media, this is due to the YouTube servers and pytubefix and not this software. Please try again later.
