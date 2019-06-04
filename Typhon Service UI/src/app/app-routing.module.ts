import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DashboardComponent } from './dashboard/dashboard.component';
import {BackupComponent} from './backup/backup.component';
import { UserComponent } from './user/user.component';
import { ModelsComponent } from './models/models.component';
import { DatabasesComponent } from './databases/databases.component';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'databases', component: DatabasesComponent },
  { path: 'users', component: UserComponent },
  { path: 'models', component: ModelsComponent },
  { path: 'backup', component: BackupComponent}
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {

  constructor() { }
}
