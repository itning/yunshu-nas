import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class SettingService {

  constructor(private http: HttpClient) {
  }

  getDataSourceSetting() {

  }
}
