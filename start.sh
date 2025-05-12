#!/bin/bash

APP_ID=${APP_ID:-0}
APP_API_PORT=${APP_API_PORT:-0}

redis-server --daemonize yes
java -jar app.jar ${APP_ID} --server.port=${APP_API_PORT}