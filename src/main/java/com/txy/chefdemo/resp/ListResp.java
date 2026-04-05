package com.txy.chefdemo.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ListResp<T> {
    private BaseResp baseResp;
    private List<T> data = new ArrayList<>();
    private Integer total = 0;

    public ListResp(BaseResp baseResp, List<T> data) {
        this.baseResp = baseResp;
        this.data = data;
        this.total = data == null ? 0 : data.size();
    }

    public ListResp(BaseResp baseResp, List<T> data, Integer total) {
        this.baseResp = baseResp;
        this.data = data;
        this.total = total == null ? 0 : total;
    }
}
