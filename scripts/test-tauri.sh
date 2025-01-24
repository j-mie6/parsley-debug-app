#!/bin/bash

printf "Testing backend...\n"

cd src-tauri || return
cargo test

if [ $? -ne 0 ]
then
    printf "\n\e[31mError while testing backend\e[0m\n\n"
    exit 1
fi

printf "\nDone\n"