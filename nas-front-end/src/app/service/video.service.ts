import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {FileEntity} from "../http/model/FileEntity";
import { HttpClient } from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Link} from "../http/model/Link";

@Injectable({
  providedIn: 'root'
})
export class VideoService {

  constructor(private http: HttpClient) {
  }

  location(path: string = undefined): Observable<FileEntity[]> {
    return this.http.get<FileEntity[]>(`${environment.backEndUrl}/location?path=${path ? path : ''}`)
  }

  links(path: string = undefined): Observable<Link[]> {
    return this.http.get<Link[]>(`${environment.backEndUrl}/links?path=${path ? path : ''}`)
  }
}
