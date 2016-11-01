# File System Security Policy

## How to compile
```
Linux: ./gradlew clean build  
Win: .\gradlew clean build  
```
This will also run the unit tests.

## How to run a server and a client
```
Linux:
  ./gradlew startServer -q
  ./gradlew startClient -q
Win:
  .\gradlew startServer -q
  .\gradlew startClient -q
```
This will start the ServerRunner that will listen for connector on 1235 port and a ClientRunner that will bind to a socket on 1235 port, localhost.
If you want to give your own argumets to thw two runners, then run the following commands:
```
Linux:
  ./gradlew startServer -Pport=1235 -q
  ./gradlew startClient -Pport=1235 -Phost=localhost -q
Win:
  .\gradlew startServer -Pport=1235 -q
  .\gradlew startClient -Pport=1235 -Phost=localhost -q
```
