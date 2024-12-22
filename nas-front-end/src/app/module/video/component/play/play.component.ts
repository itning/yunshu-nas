import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import DPlayer from 'dplayer';
import {ActivatedRoute, Router} from "@angular/router";
import {environment} from "../../../../../environments/environment";
import {Link} from "../../../../http/model/Link";
import {FileEntity} from "../../../../http/model/FileEntity";
import {VideoService} from "../../../../service/video.service";

@Component({
    selector: 'app-play',
    templateUrl: './play.component.html',
    styleUrls: ['./play.component.scss'],
    standalone: false
})
export class PlayComponent implements OnInit, OnDestroy {

  @ViewChild('videoPlayerElement', {static: true})
  private videoPlayerElement: ElementRef;

  private dp: DPlayer;
  breadcrumb: Link[] = [new Link()];

  constructor(private route: ActivatedRoute,
              private videoService: VideoService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(path => {
      const pathInfo = path['path'];
      this.dp = new DPlayer({
        container: this.videoPlayerElement.nativeElement,
        video: {
          url: `${environment.backEndUrl}/video/${pathInfo}`,
        },
        autoplay: true
      });
      this.videoService.links(pathInfo).subscribe(data => this.breadcrumb = data);
    });
  }

  ngOnDestroy(): void {
    if (this.dp) {
      this.dp.destroy();
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

  goFolder(path, last = false): void {
    if (last) {
      return;
    }
    this.router.navigateByUrl(`/video/list/${path}`).catch(console.error);
  }

}
