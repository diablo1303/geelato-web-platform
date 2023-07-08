package org.geelato.web.platform.m.base.entity;

import org.geelato.core.enums.DeleteStatusEnum;
import org.geelato.core.meta.annotation.Col;
import org.geelato.core.meta.annotation.Entity;
import org.geelato.core.meta.annotation.Title;
import org.geelato.core.meta.model.entity.BaseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author diabl
 * @description: TODO
 * @date 2023/7/5 10:57
 */
@Entity(name = "platform_attach")
@Title(title = "附件")
public class Attach extends BaseEntity {
    private String appId;
    private String name;
    private String type;
    private Long size;
    private String path;
    private String url;

    public Attach() {
    }

    public Attach(MultipartFile file) {
        setDelStatus(DeleteStatusEnum.NO.getCode());
        this.name = file.getOriginalFilename();
        this.type = file.getContentType();
        this.size = file.getSize();
    }

    @Col(name = "app_id")
    @Title(title = "所属应用")
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Col(name = "name")
    @Title(title = "名称")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Col(name = "type")
    @Title(title = "类型")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Col(name = "size")
    @Title(title = "大小")
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Col(name = "path")
    @Title(title = "绝对地址")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Col(name = "url")
    @Title(title = "相对地址")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
