# dockerfile amd46
FROM rust:latest


## Copy project files
WORKDIR /home
COPY ./project ./project
COPY ./public ./public
COPY ./frontend ./frontend
COPY ./backend ./backend
COPY ./build.sbt ./build.sbt
COPY ./index.html ./index.html
COPY ./package-lock.json ./package-lock.json
COPY ./package.json ./package.json
COPY ./styles.css ./styles.css
COPY ./.jvmopts ./.jvmopts


# Scala and sbt
RUN wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add -
RUN echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list
RUN apt-get update
RUN apt-get install -y temurin-11-jdk
RUN curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs && echo "Y" | ./cs setup
RUN ./cs install sbt --install-dir /

# Tauri dependencies
RUN apt-get update && apt-get install -y \
    libwebkit2gtk-4.0-dev \
    build-essential \
    curl \
    wget \
    file \
    libssl-dev \
    libgtk-4-dev \
    libayatana-appindicator3-dev\
    librsvg2-dev \
    javascriptcoregtk-4.1 \
    webkit2gtk-4.1

# Nodejs and npm
RUN apt-get install -y nodejs
RUN apt-get install -y npm
RUN npm install

# Rust and tauri
RUN cargo install tauri-cli
RUN rustup target add x86_64-unknown-linux-gnu # add the target for the specific architecture


# Install X11 libraries and OpenSSH server
RUN apt-get update && apt-get install -y \
    xauth \
    x11-apps \
    openssh-server

# Set up SSH and expose SSH port
RUN mkdir /var/run/sshd
EXPOSE 22

# Add helpful commands for terminal
RUN apt-get update && apt-get install -y \
    vim \
    nano \
    sudo

# cargo is in /usr/local/cargo/bin but only way to get it to work is redownload
RUN echo '\nexport PATH=/usr/local/cargo/bin:$PATH\nalias sbt="sudo /sbt"' >> /root/.bashrc

#Remove root password
RUN passwd -d root

# Allow accessing root and using empty passwords with X11 localhost
RUN \    
    sed -i "s/#PermitRootLogin prohibit-password/PermitRootLogin yes/" /etc/ssh/sshd_config && \
    sed -i "s/#PermitEmptyPasswords no/PermitEmptyPasswords yes/" /etc/ssh/sshd_config && \
    sed -i "s/#X11UseLocalhost/X11UseLocalhost/" /etc/ssh/sshd_config 

# Start the SSH server
CMD sh -c "/usr/sbin/sshd -D"