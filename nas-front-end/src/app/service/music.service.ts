import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Music} from "../http/model/Music";
import {NzTableQueryParams} from "ng-zorro-antd/table";
import {map} from "rxjs/operators";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class MusicService {
  private readonly isHttps: boolean = window.location.protocol === 'https:';

  constructor(private http: HttpClient) {
  }

  getMusicList(): Observable<Music[]> {
    return this.http.get<Music[]>(environment.backEndUrl + "/api/music/list").pipe(this.replaceMusicListUriIfNeed());
  }

  searchMusic(keyword: string, queryParams: NzTableQueryParams): Observable<Music[]> {
    return this.http.get<Music[]>(environment.backEndUrl + "/api/music/list/search" + this.convertQueryParameters(queryParams, `keyword=${keyword}`)).pipe(this.replaceMusicListUriIfNeed());
  }

  getOneMusic(musicId: string): Observable<Music> {
    return this.http.get<Music>(environment.backEndUrl + "/api/music/id/" + musicId).pipe(this.replaceMusicUriIfNeed());
  }

  getLyricFromUrl(url: string): Observable<string> {
    return this.http.get(url, {responseType: 'text'});
  }

  editMusic(formData: FormData): Observable<Music> {
    return this.http.post<Music>(environment.backEndUrl + "/api/music/edit", formData).pipe(this.replaceMusicUriIfNeed());
  }

  addMusic(formData: FormData): Observable<Music> {
    return this.http.post<Music>(environment.backEndUrl + "/api/music/add", formData).pipe(this.replaceMusicUriIfNeed());
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

  private replaceMusicListUriIfNeed() {
    return map((musicList: Music[]) => {
      if (this.isHttps) {
        return musicList.map((music: Music) => {
          if (music.coverUri && music.coverUri.startsWith('http://')) {
            music.coverUri = music.coverUri.replace('http://', 'https://');
          }
          if (music.lyricUri && music.lyricUri.startsWith('http://')) {
            music.lyricUri = music.lyricUri.replace('http://', 'https://');
          }
          if (music.musicUri && music.musicUri.startsWith('http://')) {
            music.musicUri = music.musicUri.replace('http://', 'https://');
          }
          return music;
        });
      }
      return musicList;
    });
  }

  private replaceMusicUriIfNeed() {
    return map((music: Music) => {
      if (this.isHttps) {
        if (music.coverUri && music.coverUri.startsWith('http://')) {
          music.coverUri = music.coverUri.replace('http://', 'https://');
        }
        if (music.lyricUri && music.lyricUri.startsWith('http://')) {
          music.lyricUri = music.lyricUri.replace('http://', 'https://');
        }
        if (music.musicUri && music.musicUri.startsWith('http://')) {
          music.musicUri = music.musicUri.replace('http://', 'https://');
        }
        return music;
      }
      return music;
    });
  }
}
