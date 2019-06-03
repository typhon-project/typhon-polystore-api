import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { User } from '../user';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {

  users: User[] = [];
  editingUser: User = null;
  newUser: boolean;
  private editingUserName: string;

  constructor(private api: ApiService) { }

  ngOnInit() {
    this.api.getUsers().subscribe(users => {
      this.users = users;
    });
  }

  create() {
    this.newUser = true;
    this.editingUser = new User();
  }

  edit(user: User) {
    this.newUser = false;
    this.editingUser = user;
    this.editingUserName = user.username;
  }

  cancel() {
    this.editingUser = null;
  }

  save() {
    if (this.newUser == true) {
      this.api.addUser(this.editingUser).subscribe(() => {
        this.userSaved();
      });
    } else {
      this.api.updateUser(this.editingUserName, this.editingUser).subscribe(() => {
        this.userSaved();
      });
    }
  }

  private userSaved() {
    this.editingUser = null;
    this.api.getUsers().subscribe(users => {
      this.users = users;
    });
  }

}
