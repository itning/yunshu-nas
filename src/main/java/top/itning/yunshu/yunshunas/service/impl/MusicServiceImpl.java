package top.itning.yunshu.yunshunas.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import top.itning.yunshu.yunshunas.converter.MusicConverter;
import top.itning.yunshu.yunshunas.dto.MusicDTO;
import top.itning.yunshu.yunshunas.repository.MusicRepository;
import top.itning.yunshu.yunshunas.service.MusicService;

import javax.transaction.Transactional;

/**
 * @author itning
 * @date 2020/9/5 11:25
 */
@Transactional(rollbackOn = Exception.class)
@Service
public class MusicServiceImpl implements MusicService {
    private final MusicRepository musicRepository;

    @Autowired
    public MusicServiceImpl(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    @Override
    public Page<MusicDTO> findAll(Pageable pageable) {
        return musicRepository.findAll(pageable).map(MusicConverter.INSTANCE::entity2dto);
    }

    @Override
    public Page<MusicDTO> fuzzySearch(String keyword, Pageable pageable) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllByNameLikeOrSingerLike(keyword, keyword, pageable).map(MusicConverter.INSTANCE::entity2dto);
    }

    @Override
    public Page<MusicDTO> fuzzySearchName(String keyword, Pageable pageable) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllByNameLike(keyword, pageable).map(MusicConverter.INSTANCE::entity2dto);
    }

    @Override
    public Page<MusicDTO> fuzzySearchSinger(String keyword, Pageable pageable) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllBySingerLike(keyword, pageable).map(MusicConverter.INSTANCE::entity2dto);
    }
}
