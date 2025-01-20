#!bin/bash

echo
echo Starting DILL Build
echo

npm install && \
sbt buildFrontend && \
npm run tauri build

# Linux Tauri v2 prerequisites
if [ $RUNNER_OS == "ubuntu-latest"]
then
    apt update
    apt install libwebkit2gtk-4.1-dev \
    build-essential \
    curl \
    wget \
    file \
    libxdo-dev \
    libssl-dev \
    libayatana-appindicator3-dev \
    librsvg2-dev
fi

if [ $? -ne 0 ]
then
    printf "\n\e[31mError In Starting Development Server\e[0m\n\n"
    exit 1
fi
