import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { User } from './user';
import { Database } from './database';
import { Model } from './model';
import { QueryResponse } from './QueryResponse';

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type':  'application/json'
  })
};

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient) {

  }

  getApiPath(path: string) {
    return "http://localhost:8080" + path;
  }

  getDatabases(): Observable<Database[]> {
    return this.http.get<Database[]>(this.getApiPath("/api/databases"));
  }

  backupDatabase(db: Database, backupName: string): void {
    var data = db as any;
    data["backup_name"] = backupName;
    this.http.post<void>(this.getApiPath("/api/backup"), JSON.stringify(data), httpOptions)
      .subscribe(data => {
        console.log(data['filename']);
        window.open(this.getApiPath("/api/download/" + data['filename']));
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
    window.open(this.getApiPath("/api/model/" + type + "/" + version));
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
