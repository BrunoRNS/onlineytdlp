SHELL := /bin/bash
MVN := ./mvnw
PY := python3
VENV := .venv
PIP := $(VENV)/bin/pip
PYTHON := $(VENV)/bin/python
JPACKAGE := $(shell command -v jpackage 2>/dev/null || true)
VERSION := 2.0.0

# Build structure
BUILD_DIR := build
BUILD_WEB_DIR := $(BUILD_DIR)/web
BUILD_PACKAGE_DIR := $(BUILD_DIR)/package
BUILD_GRAAL_DIR := $(BUILD_DIR)/graal
BUILD_PYTHON_DIR := $(BUILD_DIR)/python
BUILD_RELEASES_DIR := $(BUILD_DIR)/releases

# Maven and build targets
TARGET_JAR := target/videodownloader-$(VERSION).jar
TARGET_NATIVE := target/videodownloader
TARGET_JPACKAGE_DIR := target/jpackage
TARGET_PYTHON_DIR := target/python_build
PYTHON_EXEC := ytdlp
TARGET_PYTHON_EXEC := $(TARGET_PYTHON_DIR)/$(PYTHON_EXEC)

# Build output files
BUILD_JAR := $(BUILD_WEB_DIR)/videodownloader-$(VERSION).jar
BUILD_PACKAGE := $(BUILD_PACKAGE_DIR)/videodownloader-jpackage
BUILD_GRAAL := $(BUILD_GRAAL_DIR)/videodownloader-graal
BUILD_PYTHON := $(BUILD_PYTHON_DIR)/ytdlp

# Docker directory
DOCKER_DIR := docker
DOCKER_PYTHON_EXEC := $(DOCKER_DIR)/$(PYTHON_EXEC)

# JPackage configuration
JPACKAGE_TARGET_FLAG := $(if $(JP_TARGET),--target-platform $(JP_TARGET),)

# FFmpeg bundling
UNAME_S := $(shell uname -s)
FFMPEG_BIN_DIR := $(TARGET_PYTHON_DIR)/bin

# Detect architecture (32-bit or 64-bit)
ifeq ($(UNAME_S),Linux)
	FFMPEG_ARCH := x86_64
	FFMPEG_OS := linux
else ifeq ($(UNAME_S),Darwin)
	FFMPEG_ARCH := x86_64
	FFMPEG_OS := macos
else ifneq (,$(findstring MINGW,$(UNAME_S)))
	# Windows with MINGW - detect if 32-bit or 64-bit
	FFMPEG_ARCH := $(shell python3 -c "import struct; print('x86' if struct.calcsize('P')*8 == 32 else 'x86_64')")
	FFMPEG_OS := windows
else ifneq (,$(findstring MSYS,$(UNAME_S)))
	# Windows with MSYS - detect if 32-bit or 64-bit
	FFMPEG_ARCH := $(shell python3 -c "import struct; print('x86' if struct.calcsize('P')*8 == 32 else 'x86_64')")
	FFMPEG_OS := windows
else
	FFMPEG_ARCH := x86_64
	FFMPEG_OS := linux
endif

.DEFAULT_GOAL := help

help:
	@printf "Usage: make <target>\n\n"
	@printf "Targets:\n"
	@printf "  help                 Show this message\n"
	@printf "  test                 Run unit tests\n"
	@printf "  build                Build Maven JAR\n"
	@printf "  build-web            Build web app\n"
	@printf "  build-web-graal      Build web app with GraalVM native image\n"
	@printf "  build-package        Build with jPackage\n"
	@printf "  build-graal          Build with GraalVM native image\n"
	@printf "  build-python         Compile Python with Nuitka + FFmpeg\n"
	@printf "  update-docker        Update docker/ folder with artifacts\n"
	@printf "  clean                Clean all build artifacts\n\n"
	@printf "Output directory: build/\n"
	@printf "  build/web/          - Web app JAR\n"
	@printf "  build/package/      - jPackage executables\n"
	@printf "  build/graal/        - GraalVM native binaries\n"
	@printf "  build/python/       - Nuitka Python executables\n"

clean:
	rm -rf target $(VENV) .pytest_cache $(BUILD_DIR)

$(VENV)/bin/activate:
	$(PY) -m venv $(VENV)
	$(VENV)/bin/python -m pip install --upgrade pip

python-deps: $(VENV)/bin/activate
	$(PIP) install -r requirements.txt

ffmpeg-download:
	@mkdir -p $(FFMPEG_BIN_DIR)
	$(PY) scripts/download_ffmpeg.py $(FFMPEG_BIN_DIR) $(FFMPEG_ARCH)

python-test: python-deps
	$(PYTHON) -m unittest discover -s tests/python -p "test_*.py"

java-test:
	$(MVN) -Pweb-app test

test: java-test python-test

build:
	$(MVN) -DskipTests package

build-python: ffmpeg-download python-deps
	@mkdir -p $(TARGET_PYTHON_DIR)
	$(PYTHON) -m compileall src/main/python
	$(PYTHON) -m nuitka --onefile --follow-imports --include-data-dir=$(FFMPEG_BIN_DIR)=bin --output-dir=$(TARGET_PYTHON_DIR) src/main/python/ytdlp/ytdlp.py
	@mv $(TARGET_PYTHON_DIR)/ytdlp.bin $(TARGET_PYTHON_EXEC)

build-web: build-python build
	@mkdir -p $(BUILD_WEB_DIR)
	@cp $(TARGET_JAR) $(BUILD_JAR)
	@mkdir -p $(BUILD_PYTHON_DIR)
	@cp $(TARGET_PYTHON_EXEC) $(BUILD_PYTHON_DIR)/ytdlp
	@chmod +x $(BUILD_PYTHON_DIR)/ytdlp
	@mkdir -p $(DOCKER_DIR)
	@cp $(TARGET_PYTHON_EXEC) $(DOCKER_PYTHON_EXEC)
	@chmod +x $(DOCKER_PYTHON_EXEC)
	@echo "Web build complete: $(BUILD_JAR)"

build-web-graal: build-python build
	@mkdir -p $(BUILD_WEB_DIR)
	native-image -jar $(TARGET_JAR) -o $(BUILD_WEB_DIR)/videodownloader --no-fallback
	
	@mkdir -p $(BUILD_PYTHON_DIR)
	@cp $(TARGET_PYTHON_EXEC) $(BUILD_PYTHON_DIR)/ytdlp
	@chmod +x $(BUILD_PYTHON_DIR)/ytdlp
	@mkdir -p $(DOCKER_DIR)
	@cp $(TARGET_PYTHON_EXEC) $(DOCKER_PYTHON_EXEC)
	@cp $(BUILD_WEB_DIR)/videodownloader $(DOCKER_DIR)/videodownloader
	@chmod +x $(DOCKER_PYTHON_EXEC) $(DOCKER_DIR)/videodownloader
	@echo "Web build with GraalVM complete: $(BUILD_WEB_DIR)/videodownloader"

build-package: build-python build
	$(MVN) -Pdesktop-package -DskipTests clean package
	@if [ -z "$(JPACKAGE)" ]; then \
		echo "jpackage not found in PATH. Install JDK with jpackage or set PATH."; exit 1; \
	fi
	@rm -rf $(TARGET_JPACKAGE_DIR)
	@mkdir -p $(BUILD_PACKAGE_DIR)
	
	@mkdir -p target/jpackage-input
	@cp target/videodownloader-$(VERSION).jar target/jpackage-input/
	
	$(JPACKAGE) --type app-image --name videodownloader --input target/jpackage-input --main-jar videodownloader-$(VERSION).jar --main-class com.ytdlp.videodownloader.DesktopLauncher $(JPACKAGE_TARGET_FLAG) --dest $(TARGET_JPACKAGE_DIR)
	
	@if [ -f "$(TARGET_JPACKAGE_DIR)/videodownloader/bin/videodownloader.exe" ]; then \
		cp $(TARGET_JPACKAGE_DIR)/videodownloader/bin/videodownloader.exe $(BUILD_PACKAGE); \
	else \
		cp $(TARGET_JPACKAGE_DIR)/videodownloader/bin/videodownloader $(BUILD_PACKAGE); \
	fi
	@chmod +x $(BUILD_PACKAGE)
	@mkdir -p $(BUILD_PYTHON_DIR)
	@cp $(TARGET_PYTHON_EXEC) $(BUILD_PYTHON_DIR)/ytdlp
	@chmod +x $(BUILD_PYTHON_DIR)/ytdlp
	@rm -rf target/jpackage-input
	@echo "Package build complete: $(BUILD_PACKAGE)"


build-graal: build-python build
	$(MVN) -Pdesktop-graal -DskipTests native:compile
	@mkdir -p $(BUILD_GRAAL_DIR)
	@cp $(TARGET_NATIVE) $(BUILD_GRAAL)
	@chmod +x $(BUILD_GRAAL)
	@mkdir -p $(BUILD_PYTHON_DIR)
	@cp $(TARGET_PYTHON_EXEC) $(BUILD_PYTHON_DIR)/ytdlp
	@chmod +x $(BUILD_PYTHON_DIR)/ytdlp
	@echo "Graal build complete: $(BUILD_GRAAL)"

update-docker: build-web build-web-graal
	@mkdir -p $(DOCKER_DIR)
	@cp $(BUILD_JAR) $(DOCKER_DIR)/videodownloader.jar
	@cp $(BUILD_WEB_DIR)/videodownloader $(DOCKER_DIR)/videodownloader
	@cp $(BUILD_PYTHON_DIR)/ytdlp $(DOCKER_PYTHON_EXEC)
	@chmod +x $(DOCKER_DIR)/videodownloader $(DOCKER_PYTHON_EXEC)
	@echo "Docker artifacts updated"

all: test build-web build-package build-graal
