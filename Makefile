# Makefile
VENV_DIR := venv
BUILD_DIR := build
DOCKER_DIR := docker
DOCKER_IMAGE := yt-downloader
PYTHON_SOURCE := src/main/python/ytdlp/ytdlp.py

# Default target
all: clean build-native build-python copy-artifacts clean-build
	@echo "Build finished successfully."

test: clean build-native-test build-python copy-artifacts clean-build
	@echo "Tests finished successfully."

# Cleanup Build
clean-build:
	rm -rf $(BUILD_DIR)

# Setup virtual environment
setup-venv:
	@if [ ! -d $(VENV_DIR) ]; then \
		echo "Creating virtual environment..."; \
		virtualenv $(VENV_DIR); \
	fi
	$(VENV_DIR)/bin/pip install -r ./requirements.txt

# Create build directory
setup-build-dir:
	@if [ ! -d $(BUILD_DIR) ]; then \
		mkdir $(BUILD_DIR); \
	fi

# Python build
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

# Native build
build-native:
	./mvnw clean
	./mvnw -Pnative -DskipTests native:compile

build-native-test:
	./mvnw clean
	./mvnw -Pnative -X native:compile

# Copy artifacts to docker directory
copy-artifacts: build-python build-native
	cp target/videodownloader $(BUILD_DIR)/videodownloader
	cp $(BUILD_DIR)/ytdlp $(DOCKER_DIR)/ytdlp
	cp $(BUILD_DIR)/videodownloader $(DOCKER_DIR)/videodownloader

# Docker operations
docker-build: copy-artifacts
	docker build -t $(DOCKER_IMAGE) $(DOCKER_DIR) || true

docker-run: docker-build
	docker run -it --rm -p 8080:8080 $(DOCKER_IMAGE)

# Cleanup
clean:
	rm -rf $(BUILD_DIR)/
	./mvnw clean

.PHONY: all test setup-venv setup-build-dir build-python build-native build-native-test copy-artifacts docker-build docker-run clean