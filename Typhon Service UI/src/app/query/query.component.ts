import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-query',
  templateUrl: './query.component.html',
  styleUrls: ['./query.component.css']
})
export class QueryComponent implements OnInit {

  public query: string = '';
  public result: string = '';

  constructor(private api: ApiService) { }

  ngOnInit() {
  }

  run() {
    this.api.runQuery(this.query)
      .subscribe(response => this.result = response.response);
  }

}
