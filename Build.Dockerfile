FROM ghcr.io/graalvm/graalvm-community:17 AS builder

RUN microdnf update -y && \
    microdnf install -y gcc glibc-devel zlib-devel python3 python3-pip && \
    microdnf clean all

RUN pip3 install --no-cache-dir virtualenv nuitka

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn/
COPY requirements.txt ./

RUN chmod +x mvnw

COPY src ./src

FROM builder AS build-web

RUN make build-python && \
    ./mvnw -Pnative -DskipTests native:compile && \
    mkdir -p /artifacts/web && \
    cp target/videodownloader /artifacts/web/ && \
    cp build/ytdlp /artifacts/web/


FROM builder AS build-desktop

RUN make build-python && \
    ./mvnw -Pdesktop -DskipTests native:compile && \
    mkdir -p /artifacts/desktop && \
    cp target/videodownloader /artifacts/desktop/ && \
    cp build/ytdlp /artifacts/desktop/


FROM alpine:latest AS exporter
WORKDIR /output

COPY --from=build-web /artifacts/web/ /output/web/
COPY --from=build-desktop /artifacts/desktop/ /output/desktop/

CMD ["echo", "Build finished! Use 'docker cp' to extract files to /output."]
