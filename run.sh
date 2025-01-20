#!bin/bash

echo
echo Starting DILL Development Server
echo

rm -f src-laminar/package.json &&\
cp -f package.json src-laminar/ && \
sbt buildFrontend && \
npm run tauri dev