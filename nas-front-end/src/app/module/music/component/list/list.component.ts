import {Component, OnInit} from '@angular/core';
import {Music} from "../../../../http/model/Music";
import {MusicService} from "../../../../service/music.service";
import {debounceTime, distinctUntilChanged, Subject} from "rxjs";
import {NzMessageService} from "ng-zorro-antd/message";

@Component({
    selector: 'app-list',
    templateUrl: './list.component.html',
    styleUrls: ['./list.component.scss'],
    standalone: false
})
export class ListComponent implements OnInit {

  music: Music[] = [];

  loading = true;

  private searchSubject = new Subject<string>();

  constructor(private musicService: MusicService,
              private message: NzMessageService) {
  }

  ngOnInit(): void {
    this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged())
      .subscribe(item => {
        this.doSearch(item);
      });
    this.onQueryParamsChange();
  }

  onQueryParamsChange() {
    this.musicService.getMusicList().subscribe(data => {
      this.music = data;
      this.loading = false;
    });
  }

  delete(musicId: string) {
    console.log(`删除音乐：${musicId}`);
    this.musicService.delMusic(musicId).subscribe(data => {
      this.message.success(data);
      this.onQueryParamsChange();
    })
  }

  onSearch($event: any) {
    const keyword = $event.target.value;
    this.searchSubject.next(keyword);
  }

  private doSearch(keyword: string) {
    this.loading = true;
    if (!keyword || keyword.trim() === '') {
      this.onQueryParamsChange();
      return
    }
    this.musicService.searchMusic(keyword, {pageSize: 100, pageIndex: 1, sort: [], filter: []}).subscribe(data => {
      this.music = data;
      this.loading = false;
    });
  }
}
