# dockerfile amd46
FROM rust:latest


## Copy project files
WORKDIR /home
COPY ./project ./project
COPY ./public ./public
COPY ./src ./src
COPY ./src-tauri ./src-tauri
COPY ./build.sbt ./build.sbt
COPY ./index.html ./index.html
COPY ./package-lock.json ./package-lock.json
COPY ./package.json ./package.json
COPY ./vite.config.ts ./vite.config.ts


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

# Build Frontend
RUN /sbt buildFrontend

# TODO CHANGE TO UBUNTU AND MANAGE DEPENDANCIES

# Rust and tauri
RUN cargo install tauri-cli
RUN rustup target add x86_64-unknown-linux-gnu # add the target for the specific architecture


# Install X11 libraries, OpenSSH server, and necessary packages
RUN apt-get update && apt-get install -y \
    xauth \
    x11-apps \
    openssh-server

# Set up SSH
RUN mkdir /var/run/sshd
RUN echo 'root:password' | chpasswd

# Add a new user
RUN useradd -m myuser && echo 'myuser:mypassword' | chpasswd && usermod -aG sudo myuser

RUN chmod -R a+rxw /home

# Expose the necessary SSH port
EXPOSE 22

COPY run.sh /run.sh
RUN chmod a+x /run.sh

RUN chmod a+rwx /root
RUN echo rustc --version

# Start the SSH server
CMD sh -c "cd /home && /usr/sbin/sshd -D"