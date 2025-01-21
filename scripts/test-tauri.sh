#!/bin/bash
echo Testing Tauri...

cd src-tauri || return
cargo test --verbose
