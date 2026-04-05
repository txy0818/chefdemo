package com.txy.chefdemo.exp;

/**
 * @Author tianxinyu
 * @Create 2026-01-08
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}