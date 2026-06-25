SHELL := /bin/bash
MVN   := ./mvnw
PLATFORM := $(shell uname -s 2>/dev/null || echo Windows)
ifeq ($(findstring MINGW,$(PLATFORM)),MINGW)
    OS_IS_WINDOWS := yes
else ifeq ($(findstring MSYS,$(PLATFORM)),MSYS)
    OS_IS_WINDOWS := yes
else ifeq ($(PLATFORM),Windows)
    OS_IS_WINDOWS := yes
else
    OS_IS_WINDOWS := no
endif

PY := $(if $(filter yes,$(OS_IS_WINDOWS)),python,python3)

ifeq ($(OS_IS_WINDOWS),yes)
    VENV := .venv
    VENV_ACTIVATE := $(VENV)/Scripts/activate
    PIP := $(VENV)/Scripts/pip
    PYTHON := $(VENV)/Scripts/python
    CHMOD := @rem
else
    VENV := .venv
    VENV_ACTIVATE := $(VENV)/bin/activate
    PIP := $(VENV)/bin/pip
    PYTHON := $(VENV)/bin/python
    CHMOD := chmod +x
endif

JPACKAGE := $(shell command -v jpackage 2>/dev/null || true)
VERSION  := 2.0.0

MVN_EXTRA_PROFILES ?=

BUILD_DIR        := build
BUILD_WEB_DIR    := $(BUILD_DIR)/web
BUILD_PACKAGE_DIR:= $(BUILD_DIR)/package
BUILD_GRAAL_DIR  := $(BUILD_DIR)/graal
BUILD_PYTHON_DIR := $(BUILD_DIR)/python

DOCKER_DIR       := docker
DOCKER_PYTHON    := $(DOCKER_DIR)/ytdlp

TARGET_JAR         := target/videodownloader-$(VERSION).jar
TARGET_NATIVE      := target/videodownloader
TARGET_JPACKAGE_DIR:= target/jpackage
TARGET_PYTHON_DIR  := target/python_build
PYTHON_EXEC        := ytdlp
TARGET_PYTHON_EXEC := $(TARGET_PYTHON_DIR)/$(PYTHON_EXEC)

BUILD_JAR     := $(BUILD_WEB_DIR)/videodownloader-$(VERSION).jar
BUILD_PACKAGE := $(BUILD_PACKAGE_DIR)/videodownloader-jpackage
BUILD_GRAAL   := $(BUILD_GRAAL_DIR)/videodownloader-graal
BUILD_PYTHON  := $(BUILD_PYTHON_DIR)/ytdlp

JPACKAGE_TARGET_FLAG := $(if $(JP_TARGET),--target-platform $(JP_TARGET),)
NUIKTA_WINDOWS_FLAGS := $(if $(filter yes,$(OS_IS_WINDOWS)),--windows-dependency-tool=pefile,)

UPX_AVAILABLE := $(shell command -v upx 2>/dev/null)
NUIKTA_UPX := $(if $(UPX_AVAILABLE),--enable-plugin=upx,)

BUNDLE_FFMPEG ?= 1

UNAME_S       := $(shell uname -s)
FFMPEG_BIN_DIR:= $(TARGET_PYTHON_DIR)/bin

ifeq ($(UNAME_S),Linux)
    FFMPEG_ARCH := x86_64
    FFMPEG_OS   := linux
else ifeq ($(UNAME_S),Darwin)
    FFMPEG_ARCH := x86_64
    FFMPEG_OS   := macos
else ifneq (,$(findstring MINGW,$(UNAME_S)))
    FFMPEG_ARCH := $(shell $(PY) -c "import struct; print('x86' if struct.calcsize('P')*8 == 32 else 'x86_64')")
    FFMPEG_OS   := windows
else ifneq (,$(findstring MSYS,$(UNAME_S)))
    FFMPEG_ARCH := $(shell $(PY) -c "import struct; print('x86' if struct.calcsize('P')*8 == 32 else 'x86_64')")
    FFMPEG_OS   := windows
else
    FFMPEG_ARCH := x86_64
    FFMPEG_OS   := linux
endif

.DEFAULT_GOAL := help

.PHONY: help clean test java-test python-test \
        build build-web build-web-graal build-package build-graal \
        update-docker update-docker-no-graal all \
        python-deps ffmpeg-download build-python

help:
	@printf "YT-DLP Online Interface Build System\n\n"
	@printf "Usage: make <target> [OPTIONS]\n\n"
	@printf "Targets:\n"
	@printf "  help                   Show this message\n"
	@printf "  test                   Run all unit tests\n"
	@printf "  build                  Build Maven JAR only\n"
	@printf "  build-web              Build web app (JAR + Python)\n"
	@printf "  build-web-graal        Build web app with GraalVM native image\n"
	@printf "  build-package          Build desktop app with jPackage\n"
	@printf "  build-graal            Build desktop app with GraalVM native image\n"
	@printf "  build-python           Build standalone Python executable\n"
	@printf "  update-docker          Update docker/ folder with all artifacts\n"
	@printf "  update-docker-no-graal Update docker/ folder with JAR only\n"
	@printf "  clean                  Remove all build artifacts\n\n"
	@printf "Output structure:\n"
	@printf "  build/web/             - Web app JAR\n"
	@printf "  build/package/         - jPackage executables\n"
	@printf "  build/graal/           - GraalVM native binaries\n"
	@printf "  build/python/          - Nuitka Python executable\n\n"
	@printf "Environment variables:\n"
	@printf "  BUNDLE_FFMPEG=0        Build Python without embedded ffmpeg\n"
	@printf "                           (smaller binary, requires system ffmpeg)\n"
	@printf "  MVN_EXTRA_PROFILES=,win-x86  Add extra Maven profiles (comma-separated)\n\n"
	@printf "Prerequisites for Linux:\n"
	@printf "  sudo apt install upx-ucl   (required to keep binary < 100 MB)\n"

clean:
	rm -rf target $(VENV) .pytest_cache $(BUILD_DIR) $(DOCKER_DIR)

$(VENV_ACTIVATE):
	$(PY) -m venv $(VENV)
	$(PYTHON) -m pip install --upgrade pip

python-deps: $(VENV_ACTIVATE)
	$(PIP) install -r requirements.txt

ffmpeg-download:
	@mkdir -p $(FFMPEG_BIN_DIR)
	$(PY) scripts/download_ffmpeg.py $(FFMPEG_BIN_DIR) $(FFMPEG_ARCH)

$(TARGET_PYTHON_EXEC): python-deps $(if $(filter 1,$(BUNDLE_FFMPEG)),ffmpeg-download)
	@mkdir -p $(TARGET_PYTHON_DIR)
	$(PYTHON) -m compileall src/main/python
	$(PYTHON) -m nuitka \
		--onefile \
		$(NUIKTA_UPX) \
		$(NUIKTA_WINDOWS_FLAGS) \
		--assume-yes-for-downloads \
		--noinclude-unittest-mode=nofollow \
		--noinclude-pytest-mode=nofollow \
		--noinclude-setuptools-mode=nofollow \
		--include-package=ytdlp \
		--include-package=pydub \
		--include-package=pytubefix \
		$(if $(filter 1,$(BUNDLE_FFMPEG)),--include-data-dir=$(FFMPEG_BIN_DIR)=bin) \
		--output-dir=$(TARGET_PYTHON_DIR) \
		src/main/python/ytdlp/ytdlp.py
	@mv $(TARGET_PYTHON_DIR)/ytdlp.bin $@
	@echo "Python executable built: $@"
	@ls -lh $@

$(BUILD_PYTHON): $(TARGET_PYTHON_EXEC) | $(BUILD_PYTHON_DIR)
	cp $< $@
	$(CHMOD) $@

$(BUILD_PYTHON_DIR):
	mkdir -p $@

build-python: $(BUILD_PYTHON)

$(TARGET_JAR):
	$(MVN) -DskipTests package

build: $(TARGET_JAR)

$(BUILD_WEB_DIR):
	mkdir -p $@

$(BUILD_JAR): $(TARGET_JAR) | $(BUILD_WEB_DIR)
	cp $< $@

build-web: $(BUILD_JAR) $(BUILD_PYTHON)
	@echo "Web build complete: $(BUILD_JAR)"

$(BUILD_WEB_DIR)/videodownloader: $(TARGET_JAR) | $(BUILD_WEB_DIR)
	native-image -jar $< -o $@ --no-fallback
	$(CHMOD) $@

build-web-graal: $(BUILD_WEB_DIR)/videodownloader $(BUILD_PYTHON)
	@echo "Web GraalVM build complete: $(BUILD_WEB_DIR)/videodownloader"

$(BUILD_PACKAGE_DIR):
	mkdir -p $@

$(BUILD_PACKAGE): $(TARGET_JAR) $(BUILD_PYTHON) | $(BUILD_PACKAGE_DIR)
	@if [ -z "$(JPACKAGE)" ]; then \
		echo "jpackage not found in PATH. Install a JDK that includes jpackage."; exit 1; \
	fi
	$(MVN) -Pdesktop-package$(MVN_EXTRA_PROFILES) -DskipTests clean package
	rm -rf $(TARGET_JPACKAGE_DIR)
	mkdir -p target/jpackage-input
	cp $(TARGET_JAR) target/jpackage-input/
	$(JPACKAGE) --type app-image \
		--name videodownloader \
		--input target/jpackage-input \
		--main-jar videodownloader-$(VERSION).jar \
		--main-class com.ytdlp.videodownloader.DesktopLauncher \
		$(JPACKAGE_TARGET_FLAG) \
		--dest $(TARGET_JPACKAGE_DIR)
	@if [ -f "$(TARGET_JPACKAGE_DIR)/videodownloader/bin/videodownloader.exe" ]; then \
		cp $(TARGET_JPACKAGE_DIR)/videodownloader/bin/videodownloader.exe $@; \
	else \
		cp $(TARGET_JPACKAGE_DIR)/videodownloader/bin/videodownloader $@; \
	fi
	$(CHMOD) $@
	rm -rf target/jpackage-input
	@echo "Package build complete: $(BUILD_PACKAGE)"

build-package: $(BUILD_PACKAGE)

$(TARGET_NATIVE):
	$(MVN) -Pdesktop-graal -DskipTests native:compile

$(BUILD_GRAAL_DIR):
	mkdir -p $@

$(BUILD_GRAAL): $(TARGET_NATIVE) $(BUILD_PYTHON) | $(BUILD_GRAAL_DIR)
	cp $(TARGET_NATIVE) $@
	$(CHMOD) $@

build-graal: $(BUILD_GRAAL)
	@echo "GraalVM desktop build complete: $(BUILD_GRAAL)"

$(DOCKER_DIR):
	mkdir -p $@

update-docker: build-web build-web-graal $(DOCKER_DIR)
	cp $(BUILD_JAR)                     $(DOCKER_DIR)/videodownloader.jar
	cp $(BUILD_WEB_DIR)/videodownloader $(DOCKER_DIR)/videodownloader
	cp $(BUILD_PYTHON)                  $(DOCKER_PYTHON)
	$(CHMOD) $(DOCKER_DIR)/videodownloader
	$(CHMOD) $(DOCKER_PYTHON)
	@echo "Docker artifacts updated"

update-docker-no-graal: build-web $(DOCKER_DIR)
	cp $(BUILD_JAR)    $(DOCKER_DIR)/videodownloader.jar
	cp $(BUILD_PYTHON) $(DOCKER_PYTHON)
	$(CHMOD) $(DOCKER_PYTHON)
	@echo "Docker artifacts (JAR only) updated"

python-test: python-deps
	$(PYTHON) -m unittest discover -s tests/python -p "test_*.py"

java-test:
	$(MVN) -Pweb-app test

test: java-test python-test

all: test build-web build-package build-graal