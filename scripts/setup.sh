#!bin/bash

echo "Installing dependencies..."
echo

# Linux Tauri v2 prerequisites
if [ "$RUNNER_OS" = "Linux" ]
then
    sudo apt update
    sudo apt install libwebkit2gtk-4.1-dev \
    build-essential \
    curl \
    wget \
    file \
    libxdo-dev \
    libssl-dev \
    libayatana-appindicator3-dev \
    librsvg2-dev

    export PKG_CONFIG_PATH=/usr/lib/pkgconfig:/usr/share/pkgconfig:/usr/lib/x86_64-linux-gnu/pkgconfig
fi

# macOS and Windows Tauri v2 prerequisites using npm
if [ "$RUNNER_OS" = "macOS" ] || [ "$RUNNER_OS" = "Windows" ]; then
    echo "Setting up prerequisites for macOS and Windows using npm..."

    # Use npm to install cross-platform tools
    npm install -g webkitgtk \
        curl \
        wget \
        pkg-config \
        librsvg

    # Platform-specific setup
    if [ "$RUNNER_OS" = "macOS" ]; then
        echo "Additional macOS-specific setup..."

        ## Users on Apple Silion (M1, M1 Pro, M1 Max, M2, or later chips) 
        ## may have a default `/opt/homebrew` install!!
        
        # Detect Intel vs. Apple Silicon
        if [ "$(uname -m)" = "arm64" ]; then
            # Apple Silicon
            export PKG_CONFIG_PATH="/opt/homebrew/lib/pkgconfig:/opt/homebrew/share/pkgconfig"
        else
            # Intel
            export PKG_CONFIG_PATH="/usr/local/lib/pkgconfig:/usr/local/share/pkgconfig"
        fi
    fi

    if [ "$RUNNER_OS" = "Windows" ]; then
        echo "Additional Windows-specific setup..."

        export PKG_CONFIG_PATH="/c/Users/runneradmin/AppData/Roaming/npm/node_modules/webkitgtk/lib/pkgconfig"
    fi
fi

# Common steps for all platforms
npm install

echo
echo Done! Installed dependencies