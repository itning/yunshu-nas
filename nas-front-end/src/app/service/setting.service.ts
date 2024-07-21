import {Injectable} from '@angular/core';
import { HttpClient } from "@angular/common/http";
import {DbInfoResponse} from "../http/model/DbInfoResponse";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {DbInfoRequest} from "../http/model/DbInfoRequest";
import {DbInfoCheckResponse} from "../http/model/DbInfoCheckResponse";
import {NasConfigRequest, NasConfigResponse} from "../http/model/NasConfig";
import {DatasourceConfigRequest, DatasourceConfigResponse} from "../http/model/DatasourceConfig";
import {NasFtpConfigRequest, NasFtpConfigResponse} from "../http/model/NasFtpConfig";
import {ElasticsearchConfigRequest, ElasticsearchConfigResponse} from "../http/model/ElasticsearchConfig";

@Injectable({
  providedIn: 'root'
})
export class SettingService {

  constructor(private http: HttpClient) {
  }

  getDbConfigSetting(): Observable<DbInfoResponse> {
    return this.http.get<DbInfoResponse>(environment.backEndUrl + "/api/setting/db");
  }

  checkDbConfigSetting(request: DbInfoRequest): Observable<DbInfoCheckResponse> {
    return this.http.post<DbInfoCheckResponse>(environment.backEndUrl + "/api/setting/db/check", request);
  }

  setDbConfigSetting(request: DbInfoRequest): Observable<Object> {
    return this.http.post(environment.backEndUrl + "/api/setting/db", request);
  }

  getNasConfigSetting(): Observable<NasConfigResponse> {
    return this.http.get<NasConfigResponse>(environment.backEndUrl + "/api/setting/nas");
  }

  setNasConfigSetting(request: NasConfigRequest): Observable<NasConfigResponse> {
    return this.http.post<NasConfigResponse>(environment.backEndUrl + "/api/setting/nas", request);
  }

  getDatasourceConfigSetting(): Observable<DatasourceConfigResponse> {
    return this.http.get<DatasourceConfigResponse>(environment.backEndUrl + "/api/setting/datasource")
  }

  setDatasourceConfigSetting(request: DatasourceConfigRequest): Observable<DatasourceConfigResponse> {
    return this.http.post<DatasourceConfigResponse>(environment.backEndUrl + "/api/setting/datasource", request)
  }

  getFtpConfigSetting(): Observable<NasFtpConfigResponse> {
    return this.http.get<NasFtpConfigResponse>(environment.backEndUrl + "/api/setting/ftp")
  }

  setFtpConfigSetting(request: NasFtpConfigRequest): Observable<NasFtpConfigResponse> {
    return this.http.post<NasFtpConfigResponse>(environment.backEndUrl + "/api/setting/ftp", request)
  }

  getEsConfigSetting(): Observable<ElasticsearchConfigResponse> {
    return this.http.get<ElasticsearchConfigResponse>(environment.backEndUrl + "/api/setting/es")
  }

  setEsConfigSetting(request: ElasticsearchConfigRequest): Observable<ElasticsearchConfigResponse> {
    return this.http.post<ElasticsearchConfigResponse>(environment.backEndUrl + "/api/setting/es", request)
  }
}
