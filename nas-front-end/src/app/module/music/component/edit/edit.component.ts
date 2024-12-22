import {Component, OnInit, SecurityContext} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {filter, mergeMap} from "rxjs";
import {map} from "rxjs/operators";
import {MusicService} from "../../../../service/music.service";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {Music} from "../../../../http/model/Music";
import {NzMessageService} from "ng-zorro-antd/message";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import * as musicMetadata from 'music-metadata-browser';

@Component({
    selector: 'app-edit',
    templateUrl: './edit.component.html',
    styleUrls: ['./edit.component.scss'],
    standalone: false
})
export class EditComponent implements OnInit {

  formGroup: UntypedFormGroup;

  music: Music;

  musicUri: string | SafeUrl;

  lyric: string = '';

  private files: { [key: string]: File } = {};

  isUnderModification: boolean = false;

  constructor(private route: ActivatedRoute,
              private musicService: MusicService,
              private fb: UntypedFormBuilder,
              private message: NzMessageService,
              private sanitizer: DomSanitizer,
              private router: Router) {
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      musicId: [null, [Validators.required]],
      name: [null, [Validators.required]],
      singer: [null, [Validators.required]],
    });
    this.route.params
      .pipe(
        filter(item => item['id'] !== null && item['id'] !== undefined),
        map(item => item['id']),
        mergeMap(musicId => this.musicService.getOneMusic(musicId)),
        filter(music => music !== null)
      )
      .subscribe(music => {
        this.music = music;
        this.musicUri = music.musicUri;
        this.formGroup.patchValue({musicId: music.musicId});
        this.formGroup.patchValue({name: music.name});
        this.formGroup.patchValue({singer: music.singer});
        this.musicService.getLyricFromUrl(music.lyricUri).subscribe(lyric => this.lyric = lyric);
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
    this.isUnderModification = true;
    this.musicService.editMusic(formData).subscribe(data => {
      console.log(data);
      this.message.success('修改成功');
      this.router.navigateByUrl('/music/list').catch(console.error);
    }, error => {
      console.error(error);
      const errorMsg = error.error?.msg ?? '出错啦';
      this.isUnderModification = false;
      this.message.error(errorMsg);
    });
  }

  handleFile($event: any, formGroupName: string) {
    const file: File = $event.target.files[0];
    this.files[formGroupName] = file;
    switch (formGroupName) {
      case 'musicFile': {
        this.musicUri = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(file));
        this.music.type = this.coverMusicType(file.type);
        musicMetadata.parseBlob(file).then(data => {
          this.formGroup.patchValue({name: data.common.album});
          this.formGroup.patchValue({singer: data.common.artist});
          const picture = data.common.picture;
          if (picture && picture[0]) {
            this.music.coverUri = this.sanitizer.sanitize(SecurityContext.URL, this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(new Blob([picture[0].data]))))
          }
        })
        break;
      }
      case 'lyricFile': {
        this.musicService.getLyricFromUrl(this.sanitizer.sanitize(SecurityContext.URL, this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(file)))).subscribe(lyric => this.lyric = lyric);
        break;
      }
      case 'coverFile': {
        this.music.coverUri = this.sanitizer.sanitize(SecurityContext.URL, this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(file)));
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
