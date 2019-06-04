import { Component } from '@angular/core';
import { Service } from './service';
import { SERVICES } from './mock-services';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Polystore Service UI';
  items: Service[] = SERVICES;
}
