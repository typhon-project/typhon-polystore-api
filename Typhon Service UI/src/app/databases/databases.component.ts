import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Database } from '../database';

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

  backup(db: Database) {
    console.log("asdsadsadsaD");
    this.api.backupDatabase(db, db.name + "_bak");
  }
}
