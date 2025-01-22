#!/bin/bash

printf "Testing backend...\n"

cd src-tauri || return
cargo test --lib  -- --test-threads=1

printf "\nDone\n"
