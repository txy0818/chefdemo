package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Label {
    private String label;
    private Integer value;
}
