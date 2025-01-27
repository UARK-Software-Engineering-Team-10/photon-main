# photon-main
Team 10's software for Photon Laser Tag

## How to build
### Prerequisites
Windows
- Install [JDK 17](https://www.oracle.com/java/technologies/downloads/#java17-windows)
- Install [Maven](https://maven.apache.org/install.html)

Linux
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install openjdk-17-jdk
sudo apt install maven
```

Verify versions
```
$ java --version
openjdk 17.0.13 2024-10-15
OpenJDK Runtime Environment (build 17.0.13+11-Debian-1deb11u1)
OpenJDK 64-Bit Server VM (build 17.0.13+11-Debian-1deb11u1, mixed mode, sharing)
$ 
$ mvn --version
Apache Maven 3.6.3
Maven home: /usr/share/maven
Java version: 17.0.13, vendor: Debian, runtime: /usr/lib/jvm/java-17-openjdk-amd64
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "5.10.0-33-amd64", arch: "amd64", family: "unix"
```

### Instructions
- There is a run.bash included which will compile and run the project for you.
- You can compile the project yourself by running `mvn clean install` in the project directory.
- The jar is location in `target/`.
