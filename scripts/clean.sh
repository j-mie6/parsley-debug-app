#!bin/bash

echo Removing Scala files...
rm -rf .metals/
rm -rf .bloop/
rm -rf .bsp/

echo Removing compiled App files...
rm -rf static/
rm -rf target/

echo Removing Tauri and Laminar targets...
rm -rf src-tauri/target/
rm -rf src-laminar/target/

echo Done
