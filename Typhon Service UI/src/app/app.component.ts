import { Component } from '@angular/core';
import { Service } from './service';
import { SERVICES } from './mock-services';
import { ApiService } from './api.service';
import { Router } from '@angular/router';

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
  username: string;
  isLoggedIn: boolean;

  constructor(private api: ApiService, private router: Router) {
    this.getStatus();
    this.refreshLoginStatus();

    this.api.userStatusChanged.subscribe(username => {
      this.refreshLoginStatus();
    });
  }

  refreshLoginStatus() {
    this.username = this.api.getLogedinUser();
    this.isLoggedIn = this.username != null && this.username != "";
  }

  logout() {
    this.api.logout();
    this.refreshLoginStatus();
    this.router.navigate(["/login"]);
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
