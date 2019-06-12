import { NgModule }       from '@angular/core';
import { BrowserModule }  from '@angular/platform-browser';
import { FormsModule }    from '@angular/forms';
import { HttpClientModule }    from '@angular/common/http';
import { AppRoutingModule }     from './app-routing.module';

import { AppComponent }         from './app.component';
import { DashboardComponent }   from './dashboard/dashboard.component';
import { MessagesComponent }    from './messages/messages.component';
import { BackupComponent } from './backup/backup.component';
import { RestoreComponent } from './restore/restore.component';
import { UserComponent } from './user/user.component';
import { ModelsComponent } from './models/models.component';
import { DatabasesComponent } from './databases/databases.component';
import { ModelListComponent } from './model-list/model-list.component';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule,

    // The HttpClientInMemoryWebApiModule module intercepts HTTP requests
    // and returns simulated server responses.
    // Remove it when a real server is ready to receive requests.
  ],
  declarations: [
    AppComponent,
    DashboardComponent,
    MessagesComponent,
    BackupComponent,
    RestoreComponent,
    UserComponent,
    ModelsComponent,
    DatabasesComponent,
    ModelListComponent,
  ],
  bootstrap: [ AppComponent ]
})
export class AppModule { }
