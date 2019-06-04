import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { User } from './user';
import { Database } from './database';

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
    data[":backup_name"] = backupName;
    console.log(JSON.stringify(data));
    this.http.post<void>(this.getApiPath("/api/backup"), JSON.stringify(data), httpOptions);
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

  downloadModel(type: string): void {
    window.open(this.getApiPath("/api/model/" + type));
    //const options = { responseType: 'blob' as 'json' };
    //return this.http.get<Blob>(this.getApiPath("/api/model/" + type), options);
  }
}
