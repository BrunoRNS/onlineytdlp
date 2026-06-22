FROM debian:stable-slim

COPY videodownloader /app/videodownloader
COPY ytdlp /usr/local/bin/ytdlp

RUN chmod +x /app/videodownloader /usr/local/bin/ytdlp \
    && groupadd -r appgroup && useradd -r -g appgroup appuser

USER appuser
WORKDIR /app

EXPOSE 8080
ENTRYPOINT ["/app/videodownloader"]
