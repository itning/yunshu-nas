export class NasConfigRequest {
  outDir: string;
  ffmpegBinDir: string;
  aria2cFile: string;
  serverUrl: string;
  basicAuth: BasicAuthConfig;
}

export class NasConfigResponse {
  outDir: string;
  ffmpegBinDir: string;
  aria2cFile: string;
  serverUrl: string;
  basicAuth: BasicAuthConfig;
}

export class BasicAuthConfig {
  username: string;
  password: string;
  ignorePath: string[];
}
