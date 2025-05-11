FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Etc/UTC

RUN apt-get update && \
    apt-get install -y redis-server openjdk-17-jdk && \
    rm -rf /var/lib/apt/lists/*

USER root
COPY target/RoDeL-1.0-SNAPSHOT.jar /app/app.jar

COPY config/system.config /app/config/system.config
COPY config/benchmark.config /app/config/benchmark.config
COPY config/hostsDocker.config /app/config/hosts.config

COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

WORKDIR /app/

CMD /app/start.sh