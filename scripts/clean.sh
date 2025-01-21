#!bin/bash

printf "Removing Scala files... "
rm -rf .metals/
rm -rf .bloop/
rm -rf .bsp/
printf "done\n"

printf "Removing compiled App files... "
rm -rf static/
rm -rf target/
printf "done\n"

printf "Removing Tauri and Laminar targets... "
rm -rf src-tauri/target/
rm -rf src-laminar/target/
printf "done\n"
