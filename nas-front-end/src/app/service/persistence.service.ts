import {Injectable} from '@angular/core';
import {Base64} from 'js-base64';

@Injectable({
  providedIn: 'root'
})
export class PersistenceService {

  /**
   * 保存选择的TAB页的持久化唯一键
   * @private
   */
  private static readonly GENERIC_META_INFO_KEY = 'YUNSHU_NAS_PERSISTENCE_META_INFO';

  constructor() {
  }

  saveMetaInfo(key: string, value: any): void {
    const base64Str = window.localStorage.getItem(PersistenceService.GENERIC_META_INFO_KEY);
    let info;
    if (null === base64Str) {
      info = {};
    } else {
      const json = Base64.decode(base64Str);
      info = JSON.parse(json);
    }

    info[key] = value;
    const encode = Base64.encode(JSON.stringify(info));
    window.localStorage.setItem(PersistenceService.GENERIC_META_INFO_KEY, encode);
  }

  getMetaInfo(key: string): any {
    return this.getMetaInfos()[key];
  }

  getMetaInfos(): any {
    const base64Str = window.localStorage.getItem(PersistenceService.GENERIC_META_INFO_KEY);
    if (null === base64Str) {
      return {};
    }
    try {
      const json = Base64.decode(base64Str);
      return JSON.parse(json);
    } catch (e) {
      window.localStorage.removeItem(PersistenceService.GENERIC_META_INFO_KEY);
      return {};
    }
  }
}
