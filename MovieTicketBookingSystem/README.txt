===================================================================
SQLite JDBC Driver Directory (lib/)
===================================================================

Place your single external dependency JAR file inside this directory:
- sqlite-jdbc-<version>.jar (e.g., sqlite-jdbc-3.45.1.0.jar)

NOTE:
In compliance with the standalone architecture requirements, NO .jar
files are pre-packaged in this archive. Simply copy your local
sqlite-jdbc JAR file into this `lib/` folder before compiling and
running the application.

Compile & Run Commands (from project root `MovieTicketBookingSystem/`):

On macOS / Linux:
  mkdir -p bin
  javac -cp "lib/*" -d bin src/*.java
  java -cp "bin:lib/*" MovieServer

On Windows (Command Prompt / PowerShell):
  mkdir bin
  javac -cp "lib/*" -d bin src/*.java
  java -cp "bin;lib/*" MovieServer



http://localhost:8080