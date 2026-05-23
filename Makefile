VENV_DIR := venv
BUILD_DIR := build
DOCKER_DIR := docker
DOCKER_IMAGE := yt-downloader
PYTHON_SOURCE := src/main/python/ytdlp/ytdlp.py

JAR_NAME := videodownloader-2.0.0.jar
MAIN_CLASS := com.ytdlp.videodownloader.DesktopLauncher
RUNTIME_DIR := $(BUILD_DIR)/runtime-32
OUTPUT_EXE_DIR := dist-windows-32

all: clean build-native build-python copy-artifacts clean-build
	@echo "Build finished successfully."

test: clean build-native-test build-python copy-artifacts clean-build
	@echo "Tests finished successfully."

clean-build:
	rm -rf $(BUILD_DIR)

setup-venv:
	@if [ ! -d $(VENV_DIR) ]; then \
		echo "Creating virtual environment..."; \
		virtualenv $(VENV_DIR); \
	fi
	$(VENV_DIR)/bin/pip install -r ./requirements.txt

setup-build-dir:
	@if [ ! -d $(BUILD_DIR) ]; then \
		mkdir $(BUILD_DIR); \
	fi

build-python: setup-venv setup-build-dir
	$(VENV_DIR)/bin/python -m nuitka \
		--onefile \
		--standalone \
		--follow-imports \
		--include-module=pytubefix \
		--include-module=pydub \
		--include-module=logging \
		--include-module=sys \
		--remove-output \
		--output-dir=$(BUILD_DIR) \
		--output-filename=ytdlp \
		$(PYTHON_SOURCE)

build-native:
	./mvnw clean
	./mvnw -Pnative -DskipTests native:compile

build-native-test:
	./mvnw clean
	./mvnw -Pnative -X native:compile

copy-artifacts: build-python build-native
	cp target/videodownloader $(BUILD_DIR)/videodownloader
	cp $(BUILD_DIR)/ytdlp $(DOCKER_DIR)/ytdlp
	cp $(BUILD_DIR)/videodownloader $(DOCKER_DIR)/videodownloader

docker-build: copy-artifacts
	docker build -t $(DOCKER_IMAGE) $(DOCKER_DIR) || true

docker-run: docker-build
	docker run -it --rm -p 8080:8080 $(DOCKER_IMAGE)

clean:
	rm -rf $(BUILD_DIR)/
	./mvnw clean

validate-32bit:
	@echo "Validating environment..."
	@if [ "$$(uname -m)" != "i686" ] && [ "$$PROCESSOR_ARCHITECTURE" != "x86" ] && [ "$$PROCESSOR_ARCHITEW6432" != "AMD64" ]; then \
		echo "ERROR: This script must be run in a 32-bit environment (or with a JDK 32-bit version)."; \
		exit 1; \
	fi
	@echo "Environment validation succeeded."


build-win32: clean validate-32bit setup-build-dir
	./mvnw clean package -Pwindows-32 -DskipTests

	rm -rf $(RUNTIME_DIR)
	jlink --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.sql,java.xml \
	      --strip-debug \
	      --no-man-pages \
	      --no-header-files \
	      --compress=2 \
	      --output $(RUNTIME_DIR)

	rm -rf $(OUTPUT_EXE_DIR)
	jpackage --type app-image \
	         --name "YTDownloader32" \
	         --input target/ \
	         --main-jar $(JAR_NAME) \
	         --main-class $(MAIN_CLASS) \
	         --runtime-image $(RUNTIME_DIR) \
	         --dest $(OUTPUT_EXE_DIR) \
	         --win-console

	@if [ -f $(DOCKER_DIR)/ytdlp.exe ]; then \
		cp $(DOCKER_DIR)/ytdlp.exe $(OUTPUT_EXE_DIR)/YTDownloader32/; \
	elif [ -f $(BUILD_DIR)/ytdlp.exe ]; then \
		cp $(BUILD_DIR)/ytdlp.exe $(OUTPUT_EXE_DIR)/YTDownloader32/; \
	fi

	@echo "32 bit build finished!"
	@echo "Output directory: $(OUTPUT_EXE_DIR)/YTDownloader32/"

.PHONY: all test setup-venv setup-build-dir build-python build-native build-native-test copy-artifacts docker-build docker-run clean build-win32 validate-32bit
