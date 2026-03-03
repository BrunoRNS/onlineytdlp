FROM alpine:latest

RUN apk add --no-cache \
    ffmpeg \
    nodejs \
    npm \
    ca-certificates

COPY videodownloader /app/videodownloader
COPY ytdlp /usr/local/bin/ytdlp

RUN chmod +x /app/videodownloader
RUN chmod +x /usr/local/bin/ytdlp

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

EXPOSE 8080

ENTRYPOINT ["/app/videodownloader"]
