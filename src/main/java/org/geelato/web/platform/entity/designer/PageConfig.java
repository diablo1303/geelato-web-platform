package org.geelato.web.platform.entity.designer;

import org.geelato.core.meta.annotation.Col;
import org.geelato.core.meta.annotation.Entity;
import org.geelato.core.meta.annotation.Title;
import org.geelato.core.meta.model.entity.BaseEntity;

/**
 * @author itechgee@126.com
 * @date 2017/5/27.
 */
@Entity(name = "platform_page_config", table = "platform_page_config")
@Title(title = "页面配置")
public class PageConfig extends BaseEntity {

    private Long extendId;
//    private String name;
    private String type;
    private String code;
    private String content;
    private String description;


    @Col(name = "extend_id", nullable = true)
    @Title(title = "扩展信息", description = "扩展id，如对应的叶子节点id")
    public Long getExtendId() {
        return extendId;
    }

    public void setExtendId(Long extendId) {
        this.extendId = extendId;
    }

//    @Col(name = "name", nullable = true)
//    @Title(title = "名称")
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }

    @Col(name = "type", nullable = false)
    @Title(title = "类型",description = "如api|form|table")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Col(name = "code", nullable = true)
    @Title(title = "编码")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Col(name = "content", nullable = false, dataType = "longText")
    @Title(title = "内容")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Title(title = "描述")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
