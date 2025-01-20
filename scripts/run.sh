#!bin/bash

echo
echo Starting DILL Development Server
echo

npm i && \
rm -f src-laminar/package.json &&\
cp -f package.json src-laminar/ && \
sbt buildFrontend && \
npm run tauri dev

if [ $? -ne 0 ]
then
    printf "\n\e[31mError In Starting Development Server\e[0m\n\n"
    exit 1
fi