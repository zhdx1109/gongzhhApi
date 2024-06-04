package com.github.niefy.common.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/2 10:51
 * @Version 1.0
 */
public class MultipartFileToFileConverterUtils {
    public static File convert(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }
}
