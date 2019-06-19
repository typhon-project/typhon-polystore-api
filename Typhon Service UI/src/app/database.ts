export class Database {
    name: string;
    status: string;
    dbType: DatabaseType;
}

export enum DatabaseType {
	MongoDb = "MongoDb",
	MariaDb = "MariaDb"
}