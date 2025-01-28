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

printf "Removing Tauri targets... "
rm -rf src-tauri/target/
printf "done\n"

printf "Removing Laminar targets... "
rm -rf src-laminar/target/
printf "done\n"