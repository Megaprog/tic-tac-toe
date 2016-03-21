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

java -jar target/tic-tac-toe-1.0-SNAPSHOT-fat.jar -Dttt.config=external.properties

## Protocol description

Only POST requests and only "application/json" MIME type accepted.  
Server usually returns status codes:  
200 - ok (additional information in "data" object),  
400 - client error (description in "error" string),  
500 - server error (server stack trace in "exception" string)

To start the game:
```json
{
    "request": "start",
    
    "data": {
        "name": "player1",
    }
}
```

Server reply:
 ```json
 {
    "response":"start",
    
    "source":{"name":"player1"},
    
    "data":{
        "result":"GameStarted",
        
        "game":{
            "id":"ac46be27-b001-4af0-8913-0323ff6514c3",
            "player1":"player1",
            "player2":"player2",
            "time":1458561074958,
            "finished":false,
            "nextPlayer":"player1"
        }
    }
 }
 ```

To move:
```json
{
    "request": "move",
    "data": {
        "name": "player1",
        "move": {
            "x": 1,
            "y": 1
        }
    }
}
```

Server reply:
 ```json
 {
    "response":"move",
    
    "source":{"name":"player1","move":{"x":1,"y":1}},
    
    "data":{
        "result":"GameStarted",
        
        "game":{
            "id":"ac46be27-b001-4af0-8913-0323ff6514c3",
            "player1":"player1",
            "player2":"player2",
            "time":1458561074958,
            "finished":false,
            "nextPlayer":"player2",
            "moves":[{"x":1,"y":1}]
        }
    }
 }
 ```
 
 To obtain game info:
 ```json
 {
     "request": "info",
     
     "data": {
         "name": "player1",
     }
 }
 ```

Server reply:
 ```json
 {
    "response":"info",
    
    "source":{"name":"player1"},
    
    "data":{
        "result":"GameStarted",
        
        "game":{
            "id":"ac46be27-b001-4af0-8913-0323ff6514c3",
            "player1":"player1",
            "player2":"player2",
            "time":1458561075189,
            "moves":[{"x":1,"y":1},{"x":1,"y":2},{"x":2,"y":0},{"x":2,"y":2},{"x":0,"y":2}],
            "finished":true,
            "result1":"Win",
            "result2":"Loss"
        }
    }
 }
 ```
 
To prepare for new game (will clear reference to previous one):
```json
{
    "request": "new",
    
    "data": {
        "name": "player1",
    }
}
```

Server reply:
 ```json
 {
    "response":"new",
    
    "source":{"name":"player1"},
 }
 ```

