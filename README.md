[![Build Status](http://typhon.clmsuk.com:8080/buildStatus/icon?job=TyphonAPI)](http://typhon.clmsuk.com:8080/job/TyphonAPI/)

# TyphonAPI

## Functions
The following functions are all available through the REST HTTP API:

- Upload & Download DL/ML models, ability to download models of any version
- Get database connection information & status
- Manage Users
- Run QL functions(reset,query,update,preparedupdate)
- Backup & Restore databases (currently only mariadb supported)
- Set status of API "up" & "down"

## Features
- Queries going through analytics component for custom analysis (if enabled during the DL process, .jar integration needed for custom analysis)
- Make queries programatically by HTTP calls
- Store models on polystore-mongo
- User creation, authentication & management

## In Developement
- Integration with other components
- Authentication for queries
- Move to stateless paradigm

