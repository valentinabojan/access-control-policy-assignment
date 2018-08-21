# Access Control Policy

## Configuration
This assignment uses a MySQL database connection configured by several properties. Depending on your database configuration, those properties have to be changed in order for the program to work correctly.
The follwoing two files have to be updated:

__build.gradle__
```groovy
flyway {
    url = 'jdbc:mysql://localhost'
    user = 'root'
    password = 'rootp'
    schemas = ['psd']
}
```
This can also be achevied through gradleproperties, at runtime: -Pflyway.user=myUsr -Pflyway.schemas=schema1,schema2

__src/main/resources/META-INF/persistence.xml__
```xml
<property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost/psd" />
<property name="javax.persistence.jdbc.user" value="root" />
<property name="javax.persistence.jdbc.password" value="rootp" />
<property name="javax.persistence.jdbc.driver" value="com.mysql.jdbc.Driver" />
```

## Database migration
I used Flyway as a tool for database cleaning and migration. In _src/main/resources/db_ I have several sql scripts for table creation.  
__Create the necesarry tables__
```
Linux:
  ./gradlew flywayMigrate
Win:
  .\gradlew flywayMigrate
```

__Clean the database__
```
Linux:
  ./gradlew flywayClean
Win:
  .\gradlew flywayClean
```

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
