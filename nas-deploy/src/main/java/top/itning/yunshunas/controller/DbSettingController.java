package top.itning.yunshunas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.itning.yunshunas.common.db.DbEntry;
import top.itning.yunshunas.common.db.DbSourceConfig;
import top.itning.yunshunas.common.model.RestModel;
import top.itning.yunshunas.converter.SettingConverter;
import top.itning.yunshunas.dto.DbInfoRequest;
import top.itning.yunshunas.dto.DbInfoResponse;

/**
 * @author itning
 * @since 2023/4/6 14:22
 */
@Validated
@RequestMapping("/api/setting/db")
@RestController
public class DbSettingController {

    private final DbSourceConfig dbSourceConfig;

    @Autowired
    public DbSettingController(DbSourceConfig dbSourceConfig) {
        this.dbSourceConfig = dbSourceConfig;
    }

    @GetMapping
    public ResponseEntity<RestModel<DbInfoResponse>> getDbInfo() {
        DbInfoResponse dbInfoResponse = SettingConverter.INSTANCE.entity2dto(dbSourceConfig.getDbEntry());
        return RestModel.ok(dbInfoResponse);
    }

    @PostMapping("/check")
    public ResponseEntity<RestModel<Boolean>> checkDbInfo(@RequestBody DbInfoRequest request) {
        DbEntry dbEntry = SettingConverter.INSTANCE.dto2entity(request);
        return RestModel.ok(dbSourceConfig.checkConnection(dbEntry));
    }

    @PostMapping
    public ResponseEntity<RestModel<Void>> setDbInfo(@RequestBody DbInfoRequest request) {
        DbEntry dbEntry = SettingConverter.INSTANCE.dto2entity(request);
        dbSourceConfig.setDataSource(dbEntry);
        return RestModel.created();
    }
}
