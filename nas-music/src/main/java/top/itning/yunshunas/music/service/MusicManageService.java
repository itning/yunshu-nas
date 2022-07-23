package top.itning.yunshunas.music.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import top.itning.yunshunas.music.dto.MusicChangeDTO;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.dto.MusicManageDTO;

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
     * @param pageable 分页
     * @return 音乐信息
     */
    Page<MusicManageDTO> getMusicList(Pageable pageable);

    /**
     * 模糊搜索：搜索音乐名和歌手名，只要包含关键字就返回
     *
     * @param keyword  关键字
     * @param pageable 分页
     * @return 音乐信息
     */
    Page<MusicManageDTO> fuzzySearch(String keyword, Pageable pageable);

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
}
