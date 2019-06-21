import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ApiService } from '../api.service';
import { Ng6NotifyPopupService } from 'ng6-notify-popup';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
    constructor(private authenticationService: ApiService, private notify: Ng6NotifyPopupService) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(catchError(err => {
            if (err.status === 500) {
                this.notify.show(err.error.message, { position:'bottom', duration:'3000', type: 'error' });
            }

            if (err.status === 401) {
                // auto logout if 401 response returned from api
                this.authenticationService.logout();
                location.reload(true);
            }
            
            const error = err.error.message || err.statusText;
            return throwError(error);
        }))
    }
}