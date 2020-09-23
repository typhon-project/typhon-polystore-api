[![Build Status](http://typhon.clmsuk.com:8080/buildStatus/icon?job=TyphonPolystoreAPI%2Fmaster)](http://typhon.clmsuk.com:8080/job/TyphonPolystoreAPI/job/master/)

# Typhon Polystore API  [![License](https://img.shields.io/badge/License-EPL%202.0-red.svg)](https://opensource.org/licenses/EPL-2.0)

The Polystore API component is responsible both for providing to external systems a variety of
endpoints serving the platform’s core functionalities and for orchestrating the other components
within the Polystore accordingly. If the analytics component is used, it is responsible for populating
appropriate queues with incoming queries. It is also responsible for orchestrating the initialization
of the databases through the QL Server. It forwards authenticated Typhon QL queries to the QL
Server and prepares the responses containing the unified query results. Simple CRUD operations
for declared entities are also available through the API, using the QL Server. Moreover, it handles
all Metadata Database operations required for user management, model versioning etc. It is
implemented using the Java programming language and the Spring Enterprise Applications
Framework.

## Functions
The following functions are all available through the REST HTTP API:

- User Services (Register, GetAll, Get, Delete)
- Model Services (Get/Set DL and ML model)
- Query Services (Select, Insert, Update and Batch)
- Databases Services (Connection info & status)
- Backup/Restore Services (Backup and Restore for MariaDB and Download by user)
- Status Services (Up, Down and Current status)

## Endpoints

- [![Generic badge](https://img.shields.io/badge/HTTP-GET-brightgreen)](https://shields.io/)  **/api/resetdatabases** : Reset Databases
- [![Generic badge](https://img.shields.io/badge/HTTP-GET-brightgreen)](https://shields.io/)  **/api/status** : Check API's status
- [![Generic badge](https://img.shields.io/badge/HTTP-GET-brightgreen)](https://shields.io/)  **/api/down** : Turn API off
- [![Generic badge](https://img.shields.io/badge/HTTP-GET-brightgreen)](https://shields.io/)  **/api/up** : Turn API on
- [![Generic badge](https://img.shields.io/badge/HTTP-GET-brightgreen)](https://shields.io/)  **/api/users** : Get all users
- [![Generic badge](https://img.shields.io/badge/HTTP-POST-yellow)](https://shields.io/)  **/api/user/register** : Register user
- [![Generic badge](https://img.shields.io/badge/HTTP-GET-brightgreen)](https://shields.io/)  **/api/user/{username}** : Get user by username
- [![Generic badge](https://img.shields.io/badge/HTTP-POST-yellow)](https://shields.io/)  **/api/user/{username}** : Update user by username
- [![Generic badge](https://img.shields.io/badge/HTTP-DELETE-red)](https://shields.io/)  **/api/user/{username}** : Delete user 
- [![Generic badge](https://img.shields.io/badge/HTTP-GET-brightgreen)](https://shields.io/)  **/api/model/ml** : Get ML
- [![Generic badge](https://img.shields.io/badge/HTTP-GET-brightgreen)](https://shields.io/)  **/api/model/dl** : Get DL
- [![Generic badge](https://img.shields.io/badge/HTTP-POST-yellow)](https://shields.io/)  **/api/model/ml** : Set ML
- [![Generic badge](https://img.shields.io/badge/HTTP-POST-yellow)](https://shields.io/)  **/api/model/dl** : Set DL
- [![Generic badge](https://img.shields.io/badge/HTTP-GET-brightgreen)](https://shields.io/)  **/api/databases** : Get Polystore Databases
- [![Generic badge](https://img.shields.io/badge/HTTP-GET-brightgreen)](https://shields.io/)  **/api/services** : Get Polystore Services
- [![Generic badge](https://img.shields.io/badge/HTTP-POST-yellow)](https://shields.io/)  **/api/query** : Execute Select Query
- [![Generic badge](https://img.shields.io/badge/HTTP-POST-yellow)](https://shields.io/)  **/api/update** : Execute Insert/Update/Batch Query
- [![Generic badge](https://img.shields.io/badge/HTTP-POST-yellow)](https://shields.io/)  **/api/ddl** : Execute DDL command
- [![Generic badge](https://img.shields.io/badge/HTTP-POST-yellow)](https://shields.io/)  **/api/noAnalytics/query** : Execute Select Query bypassing analytics
- [![Generic badge](https://img.shields.io/badge/HTTP-POST-yellow)](https://shields.io/)  **/api/noAnalytics/update** : Execute Insert/Update/Batch Query bypassing analytics

## Examples for API functions

Based on the ML example provided on the [QL User Manual](https://github.com/typhon-project/typhonql/blob/master/typhonql/doc/typhonql.md#introduction-1), there is a Postman collection to execute every function of the API.
The collection can be found here.  

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/0325f37b11ed3ea14b58)