import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-restore',
  templateUrl: './restore.component.html',
  styleUrls: ['./restore.component.css']
})
export class RestoreComponent implements OnInit {
  database_name: string;
  database_type: string;
  host: string;
  port: string;
  backup_name: string;
  username: string;
  password: string;
  constructor() { }

  ngOnInit() {
  }
  callAPI() {

  }
}
