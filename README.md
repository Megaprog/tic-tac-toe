# TIC-TAC-TOE game server

Async REST web service using [Vert.x](http://vertx.io/) framework and [Cassandra](http://cassandra.apache.org/) NoSQL data storage.

Cassandra can be downloaded from [DataStax Distribution](http://www.planetcassandra.org/cassandra/) but it is option 
because game server contains embedded cassandra in self.

## How to package it?

mvn clean package

The tic-tac-toe-1.0-SNAPSHOT-fat.jar file will be created at target directory

## How to run it?

java -jar target/tic-tac-toe-1.0-SNAPSHOT-fat.jar 

## How to run scenario test?

mvn clean verify

## How to change configuration?

There is external.properties file in the project root.
You can copy it and change it how you will.
Then specify your config location: 

java -jar target/tic-tac-toe-1.0-SNAPSHOT-fat.jar -Dttt.config=my.properties

