export class ElasticsearchConfigRequest {
  enabled: boolean;
  uris: string[];
  username: string;
  password: string;
  pathPrefix: string;
}

export class ElasticsearchConfigResponse {
  enabled: boolean;
  uris: string[];
  username: string;
  password: string;
  pathPrefix: string;
}
