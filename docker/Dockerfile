FROM debian:stable-slim

RUN apt-get update && apt-get install -y \
    ffmpeg \
    nodejs \
    npm \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY videodownloader /app/videodownloader
COPY ytdlp /usr/local/bin/ytdlp

RUN chmod +x /app/videodownloader /usr/local/bin/ytdlp \
    && groupadd -r appgroup && useradd -r -g appgroup appuser

USER appuser
WORKDIR /app

EXPOSE 8080
ENTRYPOINT ["/app/videodownloader"]
