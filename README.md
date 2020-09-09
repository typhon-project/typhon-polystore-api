[![Build Status](http://typhon.clmsuk.com:8080/buildStatus/icon?job=TyphonPolystoreAPI/job/master/)](http://typhon.clmsuk.com:8080/job/TyphonPolystoreAPI/job/master/)

# Typhon Polystore API [![License](https://img.shields.io/badge/License-EPL%202.0-red.svg)](https://opensource.org/licenses/EPL-2.0)

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
