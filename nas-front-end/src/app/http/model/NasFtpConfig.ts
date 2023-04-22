export class NasFtpConfigRequest {
  config: NasFtpConfigItem[];
}

export class NasFtpConfigResponse {
  config: NasFtpConfigItem[];
}

export class NasFtpConfigItem {
  name: string;
  port: number;
  serverAddress: string;
  users: NasFtpConfigUser[];
}

export class NasFtpConfigUser {
  enableAnonymousAccess: boolean;
  username: string;
  password: string;
  homeDir: string;
}
