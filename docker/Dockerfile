FROM --platform=linux/amd64 ubuntu:24.04

# Install dependencies
RUN apt-get update && apt-get install -y ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN adduser --disabled-password --gecos "" appuser

# Copy executables
COPY videodownloader .
COPY ytdlp .

RUN cp ./videodownloader /usr/local/bin/videodownloader
RUN cp ./ytdlp /usr/local/bin/ytdlp

# Set permissions
RUN chmod +x /usr/local/bin/videodownloader
RUN chmod +x /usr/local/bin/ytdlp

# Switch to non-root user
USER appuser
WORKDIR /home/appuser

# Expose port and set entrypoint
EXPOSE 8080
ENTRYPOINT ["videodownloader"]
