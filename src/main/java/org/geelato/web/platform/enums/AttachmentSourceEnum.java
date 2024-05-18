package org.geelato.web.platform.enums;

import org.apache.logging.log4j.util.Strings;

/**
 * @author diabl
 * @description: TODO
 * @date 2024/5/17 18:01
 */
public enum AttachmentSourceEnum {
    PLATFORM_ATTACH("附件表", "attach"), PLATFORM_RESOURCES("资源表", "resources");

    private final String label;// 选项内容
    private final String value;// 选项值

    AttachmentSourceEnum(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public static String getLabel(String value) {
        if (Strings.isNotBlank(value)) {
            for (AttachmentSourceEnum enums : AttachmentSourceEnum.values()) {
                if (enums.getValue().equals(value)) {
                    return enums.getLabel();
                }
            }
        }
        return null;
    }
}