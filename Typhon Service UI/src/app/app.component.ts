import { Component } from '@angular/core';
import { Service } from './service';
import { SERVICES } from './mock-services';
import { ApiService } from './api.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  title = 'Polystore Service UI';
  status = false;
  waitingForStatusUpdate = false;
  items: Service[] = SERVICES;

  constructor(private api: ApiService) {
    this.getStatus();
  }

  private getStatus() {
    this.waitingForStatusUpdate = true;
    this.api.getApiStatus().subscribe((status) => {
      this.status = status;
      this.waitingForStatusUpdate = false;
    });
  }

  toogleStatus() {
    if (this.waitingForStatusUpdate == true) {
      return;
    }
    this.waitingForStatusUpdate = true;
    if (this.status == true) {
      this.bringDown();
    } else {
      this.bringUp();
    }
  }

  private bringUp() {
    this.api.bringApiUp().subscribe((status) => {
      this.status = status;
      this.waitingForStatusUpdate = false;
    });
  }

  private bringDown() {
    this.api.bringApiDown().subscribe((status) => {
      this.status = status;
      this.waitingForStatusUpdate = false;
    });
  }
}
