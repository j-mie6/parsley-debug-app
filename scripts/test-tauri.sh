#!/bin/bash

printf "Testing backend...\n"

cd src-tauri || return
cargo test

printf "\nDone\n"