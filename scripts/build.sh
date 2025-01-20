#!bin/bash

echo
echo Starting DILL Build
echo

npm install && \
cd src-laminar && \
npm install && \
cd .. && \
sbt buildFrontend && \
npm run tauri dev

if [ $? -ne 0 ]
then
    printf "\n\e[31mError In Starting Development Server\e[0m\n\n"
    exit 1
fi