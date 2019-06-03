import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient) {

  }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>("http://localhost:8080/users");
    //.pipe(
      //catchError(this.handleError)
    //);
    //return of([{ username: "nemo" }, { username: "lakis" }]);
  }
}
