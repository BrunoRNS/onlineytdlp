FROM ubuntu:24.04

RUN apt-get update && apt-get install -y yt-dlp ffmpeg \
    && rm -rf /var/lib/apt/lists/*

RUN adduser --disabled-password --gecos "" appuser

WORKDIR /app

COPY ../build/videodownloader ./videodownloader

RUN chown appuser:appuser ./videodownloader

USER appuser

EXPOSE 8080

ENTRYPOINT ["./videodownloader"]
