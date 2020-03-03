package com.clms.typhonapi.models;

    public class DatabaseInfo {

        private String host;
        private int port;
        private String dbName;
        private String dbType;
        private String dbms;
        private String user;
        private String password;

        public DatabaseInfo(String host, int port, String dbName, String dbType, String dbms, String user,
                            String password) {
            super();
            this.host = host;
            this.port = port;
            this.dbName = dbName;
            this.dbType = dbType;
            this.dbms = dbms;
            this.user = user;
            this.password = password;
        }


        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getDbName() {
            return dbName;
        }

        public String getDbType() {
            return dbType;
        }

        public String getDbms() {
            return dbms;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }
    }

