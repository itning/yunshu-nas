import {Component, OnInit, SecurityContext} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {MusicService} from "../../../../service/music.service";
import {NzMessageService} from "ng-zorro-antd/message";
import * as musicMetadata from "music-metadata-browser";
import {Router} from "@angular/router";

@Component({
    selector: 'app-add',
    templateUrl: './add.component.html',
    styleUrls: ['./add.component.scss'],
    standalone: false
})
export class AddComponent implements OnInit {

  formGroup: UntypedFormGroup;

  type: number;

  musicUri: string | SafeUrl;

  coverUri: string;

  lyric: string = '';

  addLoading = false;

  private files: { [key: string]: File } = {};

  constructor(private musicService: MusicService,
              private fb: UntypedFormBuilder,
              private message: NzMessageService,
              private sanitizer: DomSanitizer,
              private router: Router) {
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      name: [null, [Validators.required]],
      singer: [null, [Validators.required]],
    });
  }

  submitForm() {
    if (!this.formGroup.valid) {
      this.message.error('请检查必填字段！');
      return;
    }
    const formData = new FormData();
    for (let key in this.files) {
      formData.append(key, this.files[key]);
    }
    for (let key in this.formGroup.value) {
      formData.append(key, this.formGroup.value[key]);
    }
    this.addLoading = true;
    this.musicService.addMusic(formData).subscribe(data => {
      this.addLoading = false;
      console.log(data);
      this.message.success('新增成功');
      this.router.navigateByUrl('/music/list').catch(console.error);
    }, error => {
      this.addLoading = false;
      console.error(error);
      const errorMsg = error.error?.msg ?? '出错啦';
      this.message.error(errorMsg);
    });
  }

  handleFile($event: any, formGroupName: string) {
    const file: File = $event.target.files[0];
    this.files[formGroupName] = file;
    switch (formGroupName) {
      case 'musicFile': {
        this.musicUri = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(file));
        this.type = this.coverMusicType(file.type);
        musicMetadata.parseBlob(file).then(data => {
          this.formGroup.patchValue({name: data.common.title});
          this.formGroup.patchValue({singer: data.common.artist});
          const picture = data.common.picture;
          if (picture && picture[0]) {
            this.coverUri = this.sanitizer.sanitize(SecurityContext.URL, this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(new Blob([picture[0].data]))))
          }
        })
        break;
      }
      case 'lyricFile': {
        this.musicService.getLyricFromUrl(this.sanitizer.sanitize(SecurityContext.URL, this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(file)))).subscribe(lyric => this.lyric = lyric);
        break;
      }
      case 'coverFile': {
        this.coverUri = this.sanitizer.sanitize(SecurityContext.URL, this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(file)));
        break;
      }
    }
  }

  private coverMusicType(type: string): number {
    switch (type) {
      case "audio/flac":
        return 1;
      case "audio/mpeg":
        return 2;
      case "audio/wav":
        return 3;
      case "audio/aac":
        return 4;
    }
  }

}
