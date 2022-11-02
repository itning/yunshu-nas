package top.itning.yunshunas.music.repository;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import top.itning.yunshunas.music.entity.Lyric;

/**
 * @author itning
 * @since 2022/11/2 15:31
 */
public interface LyricElasticsearchRepository extends ElasticsearchRepository<Lyric, String> {
    Page<Lyric> searchByContent(String content, Pageable pageable);

    Page<Lyric> search(QueryBuilder query, Pageable pageable);
}
