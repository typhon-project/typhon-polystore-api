import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Database, DatabaseType } from '../database';

@Component({
  selector: 'app-databases',
  templateUrl: './databases.component.html',
  styleUrls: ['./databases.component.css']
})
export class DatabasesComponent implements OnInit {

  databases: Database[] = [];
  
  constructor(private api: ApiService) { }

  ngOnInit() {
    this.loadDatabases();
  }

  loadDatabases() {
    this.api.getDatabases().subscribe(dbs => this.databases = dbs);
  }

  getImage(db: Database): String {
    switch (db.dbType) {
      case DatabaseType.MariaDb:
        return "../../assets/images/mariadb.png";
      case DatabaseType.MongoDb:
          return "../../assets/images/mongodb.jpg";
    }
  }

  backup(db: Database) {
    this.api.backupDatabase(db, db.name + "_bak");
  }
}
