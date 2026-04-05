package com.txy.chefdemo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Slf4j
public class LogArgFilter {

    public static Object filter(Object arg) {
        if (arg == null) {
            return null;
        }

        if (arg instanceof MultipartFile) {
            return "MultipartFile";
        }

        if (arg instanceof ServletRequest ||
            arg instanceof ServletResponse) {
            return "Servlet";
        }

        if (arg instanceof InputStream ||
            arg instanceof OutputStream) {
            return "Stream";
        }

        if (arg instanceof byte[]) {
            return "byte[]";
        }
        return arg;
    }
}