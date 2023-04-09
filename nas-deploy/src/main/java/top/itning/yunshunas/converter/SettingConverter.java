package top.itning.yunshunas.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import top.itning.yunshunas.common.db.DbCheckConnectionResult;
import top.itning.yunshunas.common.db.DbEntry;
import top.itning.yunshunas.dto.DbInfoCheckResponse;
import top.itning.yunshunas.dto.DbInfoRequest;
import top.itning.yunshunas.dto.DbInfoResponse;

/**
 * @author itning
 * @since 2023/4/6 14:34
 */
@Mapper
public interface SettingConverter {
    SettingConverter INSTANCE = Mappers.getMapper(SettingConverter.class);

    @Mappings({})
    DbEntry dto2entity(DbInfoRequest request);

    @Mappings({})
    DbInfoResponse entity2dto(DbEntry dbEntry);

    @Mappings({})
    DbInfoCheckResponse entity2dto(DbCheckConnectionResult result);
}
