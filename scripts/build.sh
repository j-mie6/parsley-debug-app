#!bin/bash

echo
echo Starting DILL Build
echo

# Linux Tauri v2 prerequisites
if [ "$RUNNER_OS" == "Linux" ]
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

    export PKG_CONFIG_PATH=/usr/lib/pkgconfig:/usr/share/pkgconfig:/usr/lib/x86_64-linux-gnu/pkgconfig
fi

npm install && \
sbt buildFrontend && \
npm run tauri build



if [ $? -ne 0 ]
then
    printf "\n\e[31mError In Starting Development Server\e[0m\n\n"
    exit 1
fi
