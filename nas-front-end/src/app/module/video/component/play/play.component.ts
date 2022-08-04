import {Component, OnDestroy, OnInit} from '@angular/core';
import DPlayer from 'dplayer';
import {ActivatedRoute} from "@angular/router";
import {environment} from "../../../../../environments/environment";

@Component({
  selector: 'app-play',
  templateUrl: './play.component.html',
  styleUrls: ['./play.component.scss']
})
export class PlayComponent implements OnInit, OnDestroy {

  private dp: DPlayer;

  constructor(private route: ActivatedRoute,) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(path => {
      this.dp = new DPlayer({
        container: document.getElementById('dplayer'),
        video: {
          url: `${environment.backEndUrl}/video/${encodeURIComponent(path['path'])}`,
        },
      });
    });
  }

  ngOnDestroy(): void {
    if (this.dp) {
      this.dp.destroy();
    }
  }

}
