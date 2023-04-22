export class DatasourceConfigRequest {
  dataSource: DataSourceConfigItem[];
}

export class DatasourceConfigResponse {
  dataSource: DataSourceConfigItem[];
}

export class DataSourceConfigItem {
  name: string;
  className: string;
  musicFileDir: string;
  lyricFileDir: string;
  urlPrefix: string;
  secretId: string;
  secretKey: string;
  regionName: string;
  bucketName: string;
  cdnUrl: string;
  convertAudioToMp3BeforeUploading: boolean;
  canWrite: boolean;
  canRead: boolean;
}
