import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api.service';
import { Model } from '../model';

@Component({
  selector: 'app-model-list',
  templateUrl: './model-list.component.html',
  styleUrls: ['./model-list.component.css']
})
export class ModelListComponent implements OnInit {

  models: Model[];
  file:any;
  @Input()
  type: string;

  constructor(private api: ApiService) { }

  ngOnInit() {
    this.getModels();
  }

  download(type: string) {
    this.api.downloadModel(type);
  }

  private getModels() {
    if (this.type == "ml") {
      this.api.getMlModels().subscribe(m => {
        this.models = m;
      });
    } else {
      this.api.getDlModels().subscribe(m => {
        this.models = m;
      });
    }
  }

  fileChanged(e) {
      this.file = e.target.files[0];

      this.uploadDocument();
  }

  uploadDocument() {
    let fileReader = new FileReader();
    fileReader.onload = (e) => {
      if (this.type == "ml") {
        this.api.addMlModel(fileReader.result.toString()).subscribe(m => {
          this.getModels();
        });
      } else {
        this.api.addDlModel(fileReader.result.toString()).subscribe(m => {
          this.getModels();
        });
      }
    }
    fileReader.readAsText(this.file);
  }

}
