package org.geelato.web.platform.exception.file;

/**
 * @author diabl
 * @description: 12.4 文件大小超出限制异常
 * @date 2023/10/25 16:20
 */
public class FileSizeExceedLimitException extends FileException {
    private static final String MESSAGE = "12.4 File Size Exceed Limit Exception";
    private static final int CODE = 1214;

    public FileSizeExceedLimitException() {
        super(MESSAGE, CODE);
    }

    public FileSizeExceedLimitException(String msg, int code) {
        super(msg, code);
    }

    public FileSizeExceedLimitException(String detailMessage) {
        super(String.format("%s：%s", MESSAGE, detailMessage), CODE);
    }
}
