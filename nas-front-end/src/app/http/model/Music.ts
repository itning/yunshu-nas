export class Music {
  /**
   * 音乐ID
   */
  musicId: string;
  /**
   * 音乐名
   */
  name: string;
  /**
   * 歌手
   */
  singer: string;
  /**
   * 歌词ID
   */
  lyricId: string;
  /**
   * 音乐类型
   */
  type: number;

  /**
   * 音乐URL
   */
  musicUri: string;

  /**
   * 歌词URL
   */
  lyricUri: string;

  /**
   * 封面URL
   */
  coverUri: string;

  /**
   * 创建时间
   */
  gmtCreate: string;

  /**
   * 修改时间
   */
  gmtModified: string;
}
