import {Component, Input, OnInit} from '@angular/core';
import {Service} from '../service';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';

@Component({
  selector: 'app-backup',
  templateUrl: './backup.component.html',
  styleUrls: ['./backup.component.css']
})
export class BackupComponent implements OnInit {
  database_name: string;
  database_type: string;
  host: string;
  port: string;
  backup_name: string;
  username: string;
  password: string;
  ngOnInit(): void {

  }
  callAPI() {

  }

  constructor(
    private route: ActivatedRoute,
    private location: Location
  ) {}

}
