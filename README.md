# DataService

This project allows managing data with the database, cache or other through several services.
The goal is that a developer no longer needs to manipulate data handlers in his program to store and retrieve data.
So he uses a set of interface to do the operations he wants.

## Environment

We have chosen to use [Kotlin](https://kotlinlang.org/) to simplify our codes, learn the language and take advantage of coroutines
for the I/O operations.

[Gradle](https://gradle.org/) is used to manage dependencies because he's the more friendly with Kotlin.

The project is compiled to [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html), so you
need this version to use the project.

## Build

To build the project, you need to use the gradle app in the application ([gradlew.bat](gradlew.bat) for windows and [gradlew](gradlew) for linux).
`gradlew` is a wrapper to run gradle command without install it on our computer.

````shell
gradlew build
````

## Test

Some tests in the project require a **docker environment**.

### Windows

- Install [Docker Desktop](https://www.docker.com/products/docker-desktop/).
- You need to enable [WSL](https://docs.microsoft.com/en-us/windows/wsl/install) to give to docker desktop an environment with docker.
- Add a linux system for WSL, you can take [Ubuntu](https://apps.microsoft.com/store/detail/ubuntu-20044-lts/9MTTCL66CPXJ) or others
- In your linux system, update your packages and install docker.io. For example, with *apt*, the command is `apt install docker.io`

Now the application Docker Desktop can connect to the linux system to download and execute docker container

### Linux

- In your linux system, update your packages and install docker.io. For example, with *apt*, the command is `apt install docker.io`

When an image will be pulled and executed, this one will be run on your docker directly.

### Run tests

````shell
gradlew test
````