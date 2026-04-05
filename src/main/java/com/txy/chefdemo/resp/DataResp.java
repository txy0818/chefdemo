package com.txy.chefdemo.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataResp<T> {
    private BaseResp baseResp;
    private T data;
}
