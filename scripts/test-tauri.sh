#!/bin/bash

echo
echo Testing Tauri...
echo

cd src-tauri || return
cargo test --verbose

echo
echo Done! Tested Tauri