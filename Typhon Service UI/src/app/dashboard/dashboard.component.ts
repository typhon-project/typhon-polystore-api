import { Component, OnInit } from '@angular/core';
import { Service } from '../service';
// tslint:disable-next-line:import-spacing
import  {SERVICES} from '../mock-services';
import {Router} from '@angular/router';


@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: [ './dashboard.component.css' ]
})
export class DashboardComponent implements OnInit {
  services: Service[] = SERVICES;
  selectedService: Service;
  constructor(private router: Router) {}


  ngOnInit() {

  }


  onSelect(service: Service) {
    this.selectedService = service;
    if (this.selectedService.name === 'backupAPI') {
      this.router.navigate(['/backup']);
    } else {
      this.router.navigate(['/detail/' + this.selectedService.name]);
    }

  }
}
