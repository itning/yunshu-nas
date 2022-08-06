import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Page} from "../http/model/page/Page";
import {Music} from "../http/model/Music";
import {NzTableQueryParams} from "ng-zorro-antd/table";
import {map} from "rxjs/operators";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class MusicService {

  constructor(private http: HttpClient) {
  }

  getMusicList(queryParams: NzTableQueryParams): Observable<Page<Music>> {
    return this.http.get<Page<Music>>(environment.backEndUrl + "/api/music/list" + this.convertQueryParameters(queryParams))
      .pipe(map(item => {
        item.number += 1;
        return item;
      }));
  }

  searchMusic(keyword: string, queryParams: NzTableQueryParams): Observable<Page<Music>> {
    return this.http.get<Page<Music>>(environment.backEndUrl + "/api/music/list/search" + this.convertQueryParameters(queryParams, `keyword=${keyword}`))
      .pipe(map(item => {
        item.number += 1;
        return item;
      }));
  }

  getOneMusic(musicId: string): Observable<Music> {
    return this.http.get<Music>(environment.backEndUrl + "/api/music/id/" + musicId);
  }

  getLyricFromUrl(url: string): Observable<string> {
    return this.http.get(url, {responseType: 'text'});
  }

  editMusic(formData: FormData): Observable<Music> {
    return this.http.post<Music>(environment.backEndUrl + "/api/music/edit", formData);
  }

  addMusic(formData: FormData): Observable<Music> {
    return this.http.post<Music>(environment.backEndUrl + "/api/music/add", formData);
  }

  delMusic(musicId: string): Observable<any> {
    return this.http.delete(environment.backEndUrl + "/api/music/delete/" + musicId);
  }

  /**
   * 转换查询参数
   * @param queryParams NzTableQueryParams
   * @param customQueryParams
   * @private 查询参数
   */
  private convertQueryParameters(queryParams: NzTableQueryParams, customQueryParams: string = ''): string {
    const sort = queryParams.sort;
    let sortQueryParams = '';
    for (let sortElement of sort) {
      if (!sortElement.value) {
        continue;
      }
      sortQueryParams += `&sort=${sortElement.key},${sortElement.value == 'descend' ? 'desc' : 'asc'}`
    }
    if (customQueryParams) {
      customQueryParams = `${customQueryParams}&`
    } else {
      customQueryParams = '';
    }
    return `?${customQueryParams}page=${queryParams.pageIndex - 1}&size=${queryParams.pageSize}${sortQueryParams}`;
  }
}
