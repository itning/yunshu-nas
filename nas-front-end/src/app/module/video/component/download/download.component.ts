import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";
import {environment} from "../../../../../environments/environment";

@Component({
    selector: 'app-download',
    templateUrl: './download.component.html',
    styleUrls: ['./download.component.scss'],
    standalone: false
})
export class DownloadComponent implements OnInit, OnDestroy {

  @ViewChild('myiframe', {static: true})
  private myiframeElement: ElementRef<HTMLIFrameElement>;

  uiSrc: SafeResourceUrl;

  constructor(private sanitizer: DomSanitizer) {
  }

  ngOnInit(): void {
    this.uiSrc = this.sanitizer.bypassSecurityTrustResourceUrl(`${environment.backEndUrl}/webui-aria2/index.html`);
    this.changeFrameHeight();
    window.onresize = () => {
      this.changeFrameHeight();
    };
  }

  ngOnDestroy(): void {
    window.onresize = null;
  }

  private changeFrameHeight(): void {
    let height = document.body.clientHeight - 200;
    this.myiframeElement.nativeElement.height = document.documentElement.clientHeight.toString();
    this.myiframeElement.nativeElement.style.height = height + 'px';
  }

}
