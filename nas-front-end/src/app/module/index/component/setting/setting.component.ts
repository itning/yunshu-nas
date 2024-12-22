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
import {NasConfigRequest} from "../../../../http/model/NasConfig";

@Component({
    selector: 'app-setting',
    templateUrl: './setting.component.html',
    styleUrls: ['./setting.component.scss'],
    standalone: false
})
export class SettingComponent implements OnInit {
  dbConfigFromGroup: UntypedFormGroup;
  nasConfigFromGroup: UntypedFormGroup;
  datasourceConfigFromGroup: UntypedFormGroup;
  datasourceConfigFromGroupArray: UntypedFormArray;
  ftpConfigFromGroup: UntypedFormGroup;
  ftpConfigFromGroupArray: UntypedFormArray;
  esConfigFromGroup: UntypedFormGroup;
  enableBasicAuth = false;
  buttonLoading = {
    testDbConfig: false,
    submitDbConfigForm: false,
    submitNasConfigForm: false,
    submitDataSourceConfigForm: false,
    submitFtpConfigForm: false,
    submitEsConfigForm: false,
  }

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
      dataSource: this.fb.array([])
    });
    this.ftpConfigFromGroup = this.fb.group({
      config: this.fb.array([])
    });
    this.esConfigFromGroup = this.fb.group({
      enabled: [false, [Validators.required]],
      uris: [null],
      username: [null],
      password: [null],
      pathPrefix: [null],
    });
    this.datasourceConfigFromGroupArray = (this.datasourceConfigFromGroup.get('dataSource') as FormArray);
    this.ftpConfigFromGroupArray = (this.ftpConfigFromGroup.get('config') as FormArray);

    this.initDbConfigSetting();
    this.initNasConfigSetting();
    this.initDataSourceConfigSetting();
    this.initFtpConfigSetting();
    this.initEsConfigSetting();
  }

  private initDbConfigSetting() {
    this.setting.getDbConfigSetting().subscribe(response => {
      if (!response) {
        return;
      }
      this.dbConfigFromGroup.patchValue({name: response.name});
      this.dbConfigFromGroup.patchValue({type: response.type});
      this.dbConfigFromGroup.patchValue({jdbcUrl: response.jdbcUrl});
      this.dbConfigFromGroup.patchValue({username: response.username});
    });
  }

  private initNasConfigSetting() {
    this.setting.getNasConfigSetting().subscribe(response => {
      if (!response) {
        return;
      }
      this.nasConfigFromGroup.patchValue({outDir: response.outDir});
      this.nasConfigFromGroup.patchValue({ffmpegBinDir: response.ffmpegBinDir});
      this.nasConfigFromGroup.patchValue({aria2cFile: response.aria2cFile});
      this.nasConfigFromGroup.patchValue({serverUrl: response.serverUrl});
      if (response.basicAuth) {
        this.enableBasicAuth = true;
        this.nasConfigFromGroup.patchValue({
          basicAuth: {
            username: response.basicAuth?.username,
            password: response.basicAuth?.password,
            ignorePath: response.basicAuth?.ignorePath
          }
        });
      }
    });
  }

  private initDataSourceConfigSetting() {
    this.setting.getDatasourceConfigSetting().subscribe(response => {
      if (!response) {
        return;
      }
      this.datasourceConfigFromGroupArray.clear();
      for (let item of response?.dataSource) {
        this.datasourceConfigFromGroupArray.push(
          this.fb.group({
            name: [item.name, [Validators.required]],
            className: [item.className, [Validators.required]],
            musicFileDir: [item.musicFileDir],
            lyricFileDir: [item.lyricFileDir],
            urlPrefix: [item.urlPrefix],
            secretId: [item.secretId],
            secretKey: [item.secretKey],
            regionName: [item.regionName],
            bucketName: [item.bucketName],
            cdnUrl: [item.cdnUrl],
            convertAudioToMp3BeforeUploading: [item.convertAudioToMp3BeforeUploading],
            canWrite: [item.canWrite],
            canRead: [item.canRead],
          })
        )
      }
    })
  }

  private initFtpConfigSetting() {
    this.setting.getFtpConfigSetting().subscribe(response => {
      if (!response || !response.config) {
        return;
      }
      this.ftpConfigFromGroupArray.clear();
      for (let item of response?.config) {
        this.ftpConfigFromGroupArray.push(
          this.fb.group({
            name: [item.name, [Validators.required]],
            port: [item.port, [Validators.required]],
            serverAddress: [item.serverAddress],
            users: this.fb.array(item.users?.map(user =>
              this.fb.group({
                enableAnonymousAccess: [user.enableAnonymousAccess, [Validators.required]],
                username: [user.username],
                password: [user.password],
                homeDir: [user.homeDir, [Validators.required]]
              })
            ))
          })
        )
      }
    })
  }

  private initEsConfigSetting() {
    this.setting.getEsConfigSetting().subscribe(response => {
      if (!response) {
        return;
      }
      this.esConfigFromGroup.patchValue({enabled: response.enabled});
      this.esConfigFromGroup.patchValue({uris: response.uris});
      this.esConfigFromGroup.patchValue({username: response.username});
      this.esConfigFromGroup.patchValue({password: response.password});
      this.esConfigFromGroup.patchValue({pathPrefix: response.pathPrefix});
    })
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
    if (this.datasourceConfigFromGroupArray.length === 0) {
      this.submitDataSourceConfigForm();
    }
  }

  addFtpConfigFromGroup() {
    this.ftpConfigFromGroupArray.push(
      this.fb.group({
        name: [null, [Validators.required]],
        port: [21, [Validators.required]],
        serverAddress: [null],
        users: this.fb.array([
          this.fb.group({
            enableAnonymousAccess: [true, [Validators.required]],
            username: [null],
            password: [null],
            homeDir: [null, [Validators.required]]
          })
        ])
      })
    )
  }

  removeFtpConfigFromGroup(index: number) {
    this.ftpConfigFromGroupArray.removeAt(index);
    if (this.ftpConfigFromGroupArray.length === 0) {
      this.submitFtpConfigForm();
    }
  }

  addFtpUserConfigFromGroup(item: AbstractControl) {
    (item as FormArray).push(this.fb.group({
      enableAnonymousAccess: [true, [Validators.required]],
      username: [null],
      password: [null],
      homeDir: [null, [Validators.required]]
    }));
  }

  removeFtpUserConfigFromGroup(item: AbstractControl, ii: number) {
    (item as FormArray).removeAt(ii);
  }

  submitDbConfigForm() {
    this.dbConfigFromGroup.markAsDirty();
    if (this.dbConfigFromGroup.valid) {
      this.buttonLoading.submitDbConfigForm = true;
      this.setting.setDbConfigSetting(this.dbConfigFromGroup.value).subscribe(response => {
        this.dbConfigFromGroup.patchValue({password: null});
        this.message.success('保存成功');
        this.buttonLoading.submitDbConfigForm = false;
      })
    }
  }

  submitNasConfigForm() {
    this.nasConfigFromGroup.markAsDirty();
    if (this.nasConfigFromGroup.valid) {
      this.buttonLoading.submitNasConfigForm = true;
      const request: NasConfigRequest = this.nasConfigFromGroup.value;
      if (!this.enableBasicAuth) {
        delete request.basicAuth
      }
      this.setting.setNasConfigSetting(request).subscribe(response => {
        this.initNasConfigSetting();
        this.message.success('保存成功');
        this.buttonLoading.submitNasConfigForm = false;
      })
    }
  }

  basicAuthEnableChange() {
    const basicAuth = this.nasConfigFromGroup.get('basicAuth');
    if (this.enableBasicAuth) {
      ['username', 'password'].forEach(key => {
        basicAuth.get(key).setValidators(Validators.required);
        basicAuth.get(key).markAsDirty();
        basicAuth.get(key).updateValueAndValidity();
      });
    } else {
      ['username', 'password'].forEach(key => {
        basicAuth.get(key).clearValidators();
        basicAuth.get(key).markAsPristine();
        basicAuth.get(key).updateValueAndValidity();
      });
    }
  }

  dataSourceTypeChange(item: AbstractControl): void {
    if (item.get('className').value === 'top.itning.yunshunas.music.datasource.impl.FileDataSource') {
      ['secretId', 'secretKey', 'regionName', 'bucketName'].forEach(key => {
        item.get(key).clearValidators();
        item.get(key).markAsPristine();
        item.get(key).updateValueAndValidity();
      });

    } else if (item.get('className').value === 'top.itning.yunshunas.music.datasource.impl.TencentCosDataSource') {
      ['secretId', 'secretKey', 'regionName', 'bucketName'].forEach(key => {
        item.get(key).setValidators(Validators.required);
        item.get(key).markAsDirty();
        item.get(key).updateValueAndValidity();
      });

    } else {
      this.message.error('数据源类型必选！');
    }
  }

  submitDataSourceConfigForm() {
    this.datasourceConfigFromGroup.markAsDirty();
    if (!this.datasourceConfigFromGroup.valid) {
      this.message.error('请填写必填字段');
      return;
    }
    this.buttonLoading.submitDataSourceConfigForm = true;
    this.setting.setDatasourceConfigSetting(this.datasourceConfigFromGroup.value).subscribe(response => {
      this.initDataSourceConfigSetting();
      this.buttonLoading.submitDataSourceConfigForm = false;
      this.message.success('保存成功');
    });
  }

  getGroup(item: AbstractControl) {
    return item as FormGroup;
  }

  getFromArray(item: AbstractControl) {
    return item as FormArray;
  }

  enableFileDataSource(item: AbstractControl) {
    return (item as FormGroup).get('className').value === 'top.itning.yunshunas.music.datasource.impl.FileDataSource';
  }

  enableTencentCosDataSource(item: AbstractControl) {
    return (item as FormGroup).get('className').value === 'top.itning.yunshunas.music.datasource.impl.TencentCosDataSource';
  }

  testDbConfig() {
    if (this.dbConfigFromGroup.valid) {
      this.buttonLoading.testDbConfig = true;
      const value = this.dbConfigFromGroup.value;
      this.setting.checkDbConfigSetting(value).subscribe(result => {
        this.message.create(result.success ? 'success' : 'error', `测试数据库连接 ${result.success ? '成功' : result.message}`);
        this.buttonLoading.testDbConfig = false;
      });
    }
  }

  submitFtpConfigForm() {
    this.ftpConfigFromGroup.markAsDirty();
    if (this.ftpConfigFromGroup.valid) {
      this.buttonLoading.submitFtpConfigForm = true;
      this.setting.setFtpConfigSetting(this.ftpConfigFromGroup.value).subscribe(response => {
        this.initFtpConfigSetting();
        this.buttonLoading.submitFtpConfigForm = false;
        this.message.success('保存成功');
      });
    }
  }

  enableAnonymousAccessChange(itemUser: AbstractControl) {
    if (itemUser.get('enableAnonymousAccess').value) {
      ['username', 'password'].forEach(key => {
        itemUser.get(key).clearValidators();
        itemUser.get(key).markAsPristine();
        itemUser.get(key).updateValueAndValidity();
      });
    } else {
      ['username', 'password'].forEach(key => {
        itemUser.get(key).setValidators(Validators.required);
        itemUser.get(key).markAsDirty();
        itemUser.get(key).updateValueAndValidity();
      });
    }
  }

  submitEsConfigForm() {
    this.esConfigFromGroup.markAsDirty();
    if (this.esConfigFromGroup.valid) {
      this.buttonLoading.submitEsConfigForm = true;
      this.setting.setEsConfigSetting(this.esConfigFromGroup.value).subscribe(response => {
        this.initEsConfigSetting();
        this.buttonLoading.submitEsConfigForm = false;
        this.message.success('保存成功');
      });
    }
  }
}
