FROM --platform=linux/amd64 eclipse-temurin:21-jre-noble

RUN apt-get update && apt-get install -y ffmpeg nodejs npm ca-certificates && \
    rm -rf /var/lib/apt/lists/*

COPY videodownloader-1.0.0.jar /app/videodownloader.jar
COPY ytdlp /usr/local/bin/ytdlp

RUN chmod +x /usr/local/bin/ytdlp

WORKDIR /app

EXPOSE 8080
ENTRYPOINT ["java", "-XX:ActiveProcessorCount=1", "-Xmx500M", "-jar", "videodownloader.jar"]