#!bin/bash

printf "\nStarting DILL App\n\n"

printf "Building frontend...\n\n"
sbt buildFrontend 

printf "\nStarting app...\n\n"
npm run tauri dev