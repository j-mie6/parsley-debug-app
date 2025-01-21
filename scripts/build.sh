#!bin/bash

printf "\nBuilding DILL App\n\n"

printf "Building frontend...\n\n"
sbt buildFrontend 

printf "\nStarting app...\n\n"
npm run tauri dev

if [ $? -ne 0 ]
then
    printf "\n\e[31mError while building DILL App\e[0m\n\n"
    exit 1
fi

printf "\nDone. Built DILL into app\n"