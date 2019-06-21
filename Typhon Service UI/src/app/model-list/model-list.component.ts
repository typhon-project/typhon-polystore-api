import { Component, OnInit, Input } from '@angular/core';
import { ApiService } from '../api.service';
import { Model } from '../model';
import { Ng6NotifyPopupService } from 'ng6-notify-popup';

@Component({
  selector: 'app-model-list',
  templateUrl: './model-list.component.html',
  styleUrls: ['./model-list.component.css'],
  providers: [Ng6NotifyPopupService]
})
export class ModelListComponent implements OnInit {

  models: Model[];
  file:any;
  @Input()
  type: string;

  constructor(private api: ApiService, private notify: Ng6NotifyPopupService) { }

  ngOnInit() {
    this.getModels();
  }

  download(model: Model) {
    this.api.downloadModel(this.type, model.version);
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
          this.notify.show("Model uploaded succesfully", { position:'top', duration:'3000', type: 'success' });
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
