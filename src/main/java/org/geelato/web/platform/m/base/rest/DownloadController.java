package org.geelato.web.platform.m.base.rest;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.geelato.core.api.ApiResult;
import org.geelato.core.constants.ApiErrorMsg;
import org.geelato.web.platform.m.base.entity.Attach;
import org.geelato.web.platform.m.base.service.AttachService;
import org.geelato.web.platform.m.base.service.DownloadService;
import org.geelato.web.platform.m.base.service.UploadService;
import org.geelato.web.platform.m.excel.entity.OfficeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author diabl
 * @description: TODO
 * @date 2023/7/5 14:00
 */
@Controller
@RequestMapping(value = "/api/resources")
public class DownloadController extends BaseController {
    private static final String ROOT_DIRECTORY = "upload";
    private static final String ROOT_CONFIG_DIRECTORY = "/upload/config";
    private final Logger logger = LoggerFactory.getLogger(DownloadController.class);
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private AttachService attachService;

    @RequestMapping(value = "/file", method = RequestMethod.GET)
    @ResponseBody
    public void downloadFile(String id, String name, String path, boolean isPdf, boolean isPreview, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 抽取出来
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = null;
            String appId = null;
            String tenantCode = null;
            if (Strings.isNotBlank(id)) {
                Attach attach = attachService.getModel(id);
                Assert.notNull(attach, ApiErrorMsg.IS_NULL);
                appId = attach.getAppId();
                tenantCode = attach.getTenantCode();
                file = downloadService.downloadFile(attach.getName(), attach.getPath());
                name = attach.getName();
            } else if (Strings.isNotBlank(path)) {
                file = downloadService.downloadFile(name, path);
                name = Strings.isNotBlank(name) ? name : file.getName();
            }
            if (isPdf) {
                String ext = name.substring(name.lastIndexOf("."));
                name = Strings.isNotBlank(name) ? name.replace(ext, ".pdf") : null;
                String outputPath = UploadService.getSavePath(ROOT_DIRECTORY, tenantCode, appId, "word-to-pdf.pdf", true);
                OfficeUtils.toPdf(file.getAbsolutePath(), outputPath, ext);
                File pFile = new File(outputPath);
                file = pFile.exists() ? pFile : null;
            }
            if (file != null && Strings.isNotBlank(name)) {
                out = response.getOutputStream();
                // 编码
                String encodeName = URLEncoder.encode(name, StandardCharsets.UTF_8);
                String mineType = request.getServletContext().getMimeType(encodeName);
                response.setContentType(mineType);
                // 在线查看图片、pdf
                if (isPreview && Strings.isNotBlank(mineType) && (mineType.startsWith("image/") || mineType.equalsIgnoreCase("application/pdf"))) {
                    //  file = downloadService.copyToFile(file, name);
                } else {
                    response.setHeader("Content-Disposition", "attachment; filename=" + encodeName);
                }
                // 读取文件
                in = new FileInputStream(file);
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            } else {
                throw new Exception("文件不存在");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    @RequestMapping(value = "/json", method = RequestMethod.GET)
    @ResponseBody
    public ApiResult downloadJson(String fileName) throws IOException {
        ApiResult result = new ApiResult();
        if (Strings.isBlank(fileName)) {
            return result.success().setMsg("fileName is null");
        }
        BufferedReader bufferedReader = null;
        try {
            String ext = UploadService.getFileExtension(fileName);
            if (Strings.isBlank(ext) || !ext.equalsIgnoreCase(".config")) {
                fileName += ".config";
            }
            File file = new File(String.format("%s/%s", ROOT_CONFIG_DIRECTORY, fileName));
            if (!file.exists()) {
                return result.success().setMsg("File (.config) does not exist");
            }
            StringBuilder contentBuilder = new StringBuilder();
            bufferedReader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contentBuilder.append(line);
            }
            result.setData(contentBuilder.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());
            result.success().setMsg(e.getMessage());
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

        return result;
    }
}
