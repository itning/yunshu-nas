package top.itning.yunshunas.music.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import top.itning.yunshunas.music.dto.SearchResultDTO;
import top.itning.yunshunas.music.entity.SearchResult;

import java.util.List;

/**
 * @author itning
 * @since 2022/11/12 19:57
 */
@Mapper
public interface SearchConverter {
    SearchConverter INSTANCE = Mappers.getMapper(SearchConverter.class);

    /**
     * 实体转DTO
     *
     * @param searchResult 实体
     * @return DTO
     */
    @Mappings({})
    SearchResultDTO entity2dto(SearchResult searchResult);

    @Mappings({})
    List<SearchResultDTO> entity2dto(List<SearchResult> searchResults);
}
