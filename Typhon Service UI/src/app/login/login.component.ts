import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
    loading = false;
    submitted = false;
    returnUrl: string;
    error = '';

    constructor(
        private formBuilder: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private api: ApiService) {}

    ngOnInit() {
        this.loginForm = this.formBuilder.group({
            username: ['', Validators.required],
            password: ['', Validators.required]
        });

        // reset login status
        this.api.logout();

        // get return url from route parameters or default to '/'
        this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    }

    // convenience getter for easy access to form fields
    get f() { return this.loginForm.controls; }

    onSubmit() {
        this.submitted = true;

        // stop here if form is invalid
        if (this.loginForm.invalid) {
            return;
        }

        this.loading = true;
        this.api.login(this.f.username.value, this.f.password.value)
            .subscribe(data => {
                this.loading = false;
                
                if (data == true) {
                    localStorage.setItem('currentUser', JSON.stringify(
                        { 
                            username: this.f.username.value,
                            authdata: window.btoa(this.f.username.value + ':' + this.f.password.value) 
                        }
                      ));
                      this.api.userStatusChanged.emit(this.f.username.value);
                      this.router.navigate([this.returnUrl]);
                      this.error = "";
                } else {
                    this.error = "Not valid credentials";
                }

                },
                error => {
                    this.error = error;
                    this.loading = false;
                });
      }

}
