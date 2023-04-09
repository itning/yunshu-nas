package top.itning.yunshunas.music.service;

import top.itning.yunshunas.music.dto.MusicChangeDTO;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.dto.MusicManageDTO;

import java.util.List;

/**
 * 音乐管理服务
 *
 * @author itning
 * @since 2022/7/10 14:40
 */
public interface MusicManageService {
    /**
     * 分页查找全部音乐
     *
     * @return 音乐信息
     */
    List<MusicManageDTO> getMusicList();

    /**
     * 模糊搜索：搜索音乐名和歌手名，只要包含关键字就返回
     *
     * @param keyword  关键字
     * @return 音乐信息
     */
    List<MusicManageDTO> fuzzySearch(String keyword);

    /**
     * 获取一个音乐
     *
     * @param musicId 音乐ID
     * @return 音乐
     */
    MusicManageDTO getOneMusic(String musicId);

    /**
     * 修改音乐
     *
     * @param music 修改信息
     * @return 修改结果
     * @throws Exception 上传失败
     */
    MusicDTO editMusic(MusicChangeDTO music) throws Exception;

    /**
     * 新增音乐
     *
     * @param music 音乐信息
     * @return 新增结果
     * @throws Exception 新增失败
     */
    MusicDTO addMusic(MusicChangeDTO music) throws Exception;

    /**
     * 删除音乐
     *
     * @param musicId 音乐ID
     */
    void deleteMusic(String musicId);
}
