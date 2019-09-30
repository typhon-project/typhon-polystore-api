import { Injectable, Output, EventEmitter } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { User } from './user';
import { Database } from './database';
import { Model } from './model';
import { QueryResponse } from './QueryResponse';
import { saveAs } from 'file-saver';
import {environment} from '../environments/environment';

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type':  'application/json'
  })
};

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  @Output() userStatusChanged: EventEmitter<string> = new EventEmitter();
  
  constructor(private http: HttpClient) {

  }

  getApiPath(path: string) {
    return 'http://' + environment.api_host + ':' + environment.api_port + path;
  }

  login(username: string, password: string): Observable<boolean> {
    return this.http.post<boolean>(this.getApiPath("/api/users/authenticate"), { username, password }, httpOptions);
  }

  getLogedinUser(): string {
    const currentUser = localStorage.getItem("currentUser");
    try {
      const userInfo = JSON.parse(currentUser);
      return userInfo.username;
    } catch (e) {
      return null;
    }
  }

  logout() {
    localStorage.removeItem('currentUser');
  }

  getDatabases(): Observable<Database[]> {
    return this.http.get<Database[]>(this.getApiPath("/api/databases"));
  }

  backupDatabase(db: Database, backupName: string): void {
    var data = db as any;
    data["backup_name"] = backupName;
    this.http.post<void>(this.getApiPath("/api/backup"), JSON.stringify(data), httpOptions)
      .subscribe(data => {
        this.http.get<string>(
          this.getApiPath("/api/download/" + data['filename']),
          { responseType: 'blob' as 'json'}
        ).subscribe(blob => {
          saveAs(blob,  data['filename']);
        });
      });
  }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.getApiPath("/users"));
  }

  addUser(user: User): Observable<void> {
    return this.http.post<void>(this.getApiPath("/user/register"), JSON.stringify(user), httpOptions);
  }

  updateUser(username: string, user: User): Observable<void> {
    return this.http.post<void>(this.getApiPath("/user/" + username), JSON.stringify(user), httpOptions);
  }

  getMlModels(): Observable<Model[]> {
    return this.http.get<Model[]>(this.getApiPath("/api/models/ml"));
  }

  getDlModels(): Observable<Model[]> {
    return this.http.get<Model[]>(this.getApiPath("/api/models/dl"));
  }

  addDlModel(contents: string): Observable<Model> {
    var data = {
      name: "dl_model",
      contents: contents
    };
    return this.http.post<Model>(this.getApiPath("/api/model/dl"), JSON.stringify(data), httpOptions);
  }

  addMlModel(contents: string): Observable<Model> {
    var data = {
      name: "ml_model",
      contents: contents
    };
    return this.http.post<Model>(this.getApiPath("/api/model/ml"), JSON.stringify(data), httpOptions);
  }

  downloadModel(type: string, version: number): void {
    this.http.get<string>(
      this.getApiPath("/api/model/" + type + "/" + version),
      { responseType: 'blob' as 'json'}
    ).subscribe(blob => {
      saveAs(blob, "model_" + version + ".t" + type);
    });
  }

  runQuery(query: string): Observable<QueryResponse> {
    return this.http.post<QueryResponse>(this.getApiPath("/api/query"), query, httpOptions);
  }

  getApiStatus(): Observable<boolean> {
    return this.http.get<boolean>(this.getApiPath("/api/status"));
  }

  bringApiUp(): Observable<boolean> {
    return this.http.get<boolean>(this.getApiPath("/api/up"));
  }

  bringApiDown(): Observable<boolean> {
    return this.http.get<boolean>(this.getApiPath("/api/down"));
  }
}
