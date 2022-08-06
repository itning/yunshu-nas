import {Component, OnInit} from '@angular/core';
import {Music} from "../../../../http/model/Music";
import {Page} from "../../../../http/model/page/Page";
import {NzTableQueryParams} from "ng-zorro-antd/table";
import {MusicService} from "../../../../service/music.service";
import {debounceTime, distinctUntilChanged, Subject} from "rxjs";
import {NzMessageService} from "ng-zorro-antd/message";

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {

  musicData: Music[] = [];

  music: Page<Music>;

  loading = true;

  private searchSubject = new Subject<string>();

  constructor(private musicService: MusicService,
              private message: NzMessageService) {
  }

  ngOnInit(): void {
    const page = new Page<Music>();
    page.size = 100;
    page.number = 1;
    this.music = page;
    this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged())
      .subscribe(item => {
        this.doSearch(item);
      });
  }

  onQueryParamsChange($event: NzTableQueryParams) {
    this.musicService.getMusicList($event).subscribe(data => {
      this.musicData = data.content;
      this.music = data;
      this.loading = false;
    });
  }

  delete(musicId: string) {
    console.log(`删除音乐：${musicId}`);
    this.musicService.delMusic(musicId).subscribe(data => {
      this.message.success(data);
      this.onQueryParamsChange({pageSize: 100, pageIndex: 1, sort: [], filter: []});
    })
  }

  onSearch($event: any) {
    const keyword = $event.target.value;
    this.searchSubject.next(keyword);
  }

  private doSearch(keyword: string) {
    this.loading = true;
    if (!keyword || keyword.trim() === '') {
      this.onQueryParamsChange({pageSize: 100, pageIndex: 1, sort: [], filter: []});
      return
    }
    this.musicService.searchMusic(keyword, {pageSize: 100, pageIndex: 1, sort: [], filter: []}).subscribe(data => {
      this.musicData = data.content;
      this.music = data;
      this.loading = false;
    });
  }
}
