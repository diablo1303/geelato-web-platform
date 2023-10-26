package org.geelato.web.platform.exception.file;

/**
 * @author diabl
 * @description: 12.6 文件内容校验失败异常
 * @date 2023/10/25 11:37
 */
public class FileContentValidFailedException extends FileException {
    private static final String MESSAGE = "12.6 File Content Validate Failed Exception";
    private static final int CODE = 1216;

    public FileContentValidFailedException() {
        super(MESSAGE, CODE);
    }

    public FileContentValidFailedException(String msg, int code) {
        super(msg, code);
    }

    public FileContentValidFailedException(String detailMessage) {
        super(String.format("%s：%s", MESSAGE, detailMessage), CODE);
    }
}
