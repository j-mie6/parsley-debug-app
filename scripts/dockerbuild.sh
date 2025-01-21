#!bin/bash

echo "Copying Files"

# Copy app mains to docker image
scp -o StrictHostKeyChecking=no -r -P 2222 ./src ./src-tauri root@localhost:/home >> logs

echo "Copied Files"

echo "Building frontend"

# Build frontend inside of the image
echo "cd /home && sbt buildFrontend" | ssh -o StrictHostKeyChecking=no -p 2222 root@localhost >> logs 2>&1

echo "Built frontend"