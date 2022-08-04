import {Component, OnInit} from '@angular/core';
import {VideoService} from "../../../../service/video.service";
import {FileEntity} from "../../../../http/model/FileEntity";
import {Link} from "../../../../http/model/Link";
import {Router} from "@angular/router";

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {

  data: FileEntity[];
  breadcrumb: Link[] = [new Link()];

  constructor(private videoService: VideoService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.videoService.location().subscribe(data => this.data = data);
  }

  go(item: FileEntity): void {
    const path = encodeURIComponent(item.location);
    if (!item.file) {
      this.videoService.location(path).subscribe(data => this.data = data);
    } else if (item.canPlay) {
      this.router.navigateByUrl(`/video/play/${path}`).catch(console.error);
    }
    this.videoService.links(path).subscribe(data => this.breadcrumb = data);
  }

  goFolder(path): void {
    this.videoService.location(path).subscribe(data => this.data = data);
    this.videoService.links(path).subscribe(data => this.breadcrumb = data);
  }
}
