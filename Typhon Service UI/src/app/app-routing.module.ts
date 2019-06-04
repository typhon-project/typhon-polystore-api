import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DashboardComponent } from './dashboard/dashboard.component';
import { ServicesComponent } from './heroes/services';
import { ServiceDetailComponent } from './hero-detail/service-detail.component';
import {BackupComponent} from './backup/backup.component';
import { UserComponent } from './user/user.component';
import { ModelsComponent } from './models/models.component';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'users', component: UserComponent },
  { path: 'models', component: ModelsComponent },
  { path: 'detail/:name', component: ServiceDetailComponent },
  { path: 'services', component: ServicesComponent },
  { path: 'backup', component: BackupComponent}
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {

  constructor() { }
}
