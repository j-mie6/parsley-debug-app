#!/bin/bash

printf "Testing frontend...\n"

sbt -v test

if [ $? -ne 0 ]
then
    printf "\n\e[31mError while testing frontend\e[0m\n\n"
    exit 1
fi

printf "\nDone\n"