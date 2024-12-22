import {Component, OnInit} from '@angular/core';
import {VideoService} from "../../../../service/video.service";
import {FileEntity} from "../../../../http/model/FileEntity";
import {Link} from "../../../../http/model/Link";
import {ActivatedRoute, Router} from "@angular/router";
import {filter} from "rxjs";
import {map} from "rxjs/operators";

@Component({
    selector: 'app-list',
    templateUrl: './list.component.html',
    styleUrls: ['./list.component.scss'],
    standalone: false
})
export class ListComponent implements OnInit {

  data: FileEntity[];
  breadcrumb: Link[] = [new Link()];

  constructor(private route: ActivatedRoute,
              private videoService: VideoService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.route.params
      .pipe(
        filter(item => item['path'] !== null && item['path'] !== undefined),
        map(item => item['path']),
      )
      .subscribe(path => {
        this.videoService.location(path).subscribe(data => this.data = data);
        this.videoService.links(path).subscribe(data => this.breadcrumb = data);
      })
    if (this.router.url === '/video/list') {
      this.videoService.location().subscribe(data => this.data = data);
    }
  }

  go(item: FileEntity): void {
    const path = item.location;
    if (!item.file) {
      this.router.navigateByUrl(`/video/list/${path}`).catch(console.error);
    } else if (item.canPlay) {
      this.router.navigateByUrl(`/video/play/${path}`).catch(console.error);
    }
  }

  goFolder(path): void {
    this.router.navigateByUrl(`/video/list/${path}`).catch(console.error);
  }
}
