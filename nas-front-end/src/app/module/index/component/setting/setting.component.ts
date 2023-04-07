import {Component, OnInit} from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormGroup,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup,
  Validators
} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {NzMessageService} from "ng-zorro-antd/message";
import {SettingService} from "../../../../service/setting.service";

@Component({
  selector: 'app-setting',
  templateUrl: './setting.component.html',
  styleUrls: ['./setting.component.scss']
})
export class SettingComponent implements OnInit {
  dbConfigFromGroup: UntypedFormGroup;
  nasConfigFromGroup: UntypedFormGroup;
  datasourceConfigFromGroup: UntypedFormGroup;
  datasourceConfigFromGroupArray: UntypedFormArray;
  enableBasicAuth = false;

  constructor(private route: ActivatedRoute,
              private fb: UntypedFormBuilder,
              private message: NzMessageService,
              private setting: SettingService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.dbConfigFromGroup = this.fb.group({
      name: [null, [Validators.required]],
      type: ['MYSQL', [Validators.required]],
      jdbcUrl: [null, [Validators.required]],
      username: [null],
      password: [null],
    });
    this.nasConfigFromGroup = this.fb.group({
      outDir: [null],
      ffmpegBinDir: [null],
      aria2cFile: [null],
      serverUrl: [null],
      basicAuth: this.fb.group({
        username: [null],
        password: [null],
        ignorePath: [null]
      }),
    });
    this.datasourceConfigFromGroup = this.fb.group({
      datasource: this.fb.array([])
    });
    this.datasourceConfigFromGroupArray = (this.datasourceConfigFromGroup.get('datasource') as FormArray);
  }

  addDatasourceConfigFromGroup() {
    this.datasourceConfigFromGroupArray.push(
      this.fb.group({
        name: [null, [Validators.required]],
        className: ['top.itning.yunshunas.music.datasource.impl.FileDataSource', [Validators.required]],
        musicFileDir: [null],
        lyricFileDir: [null],
        urlPrefix: [null],
        secretId: [null],
        secretKey: [null],
        regionName: [null],
        bucketName: [null],
        cdnUrl: [null],
        convertAudioToMp3BeforeUploading: [false],
        canWrite: [true],
        canRead: [true],
      })
    )
  }

  removeDatasourceConfigFromGroup(index: number) {
    this.datasourceConfigFromGroupArray.removeAt(index);
  }

  submitDbConfigForm() {
    console.log(this.dbConfigFromGroup.value);
  }

  submitNasConfigForm() {
    console.log(this.nasConfigFromGroup.value);
  }

  submitDataSourceConfigForm() {
    console.log(this.datasourceConfigFromGroup.value);
  }

  protected readonly FormGroup = FormGroup;

  getGroup(item: AbstractControl) {
    return item as FormGroup;
  }

  enableFileDataSource(item: AbstractControl) {
    return (item as FormGroup).get('className').value === 'top.itning.yunshunas.music.datasource.impl.FileDataSource';
  }

  enableTencentCosDataSource(item: AbstractControl) {
    return (item as FormGroup).get('className').value === 'top.itning.yunshunas.music.datasource.impl.TencentCosDataSource';
  }
}
