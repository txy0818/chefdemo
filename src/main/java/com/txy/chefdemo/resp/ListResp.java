package com.txy.chefdemo.resp;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

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
        this.total = ObjectUtils.isEmpty(data) ? 0 : data.size();
    }

    public ListResp(BaseResp baseResp, List<T> data, Integer total) {
        this.baseResp = baseResp;
        this.data = data;
        this.total = ObjectUtils.defaultIfNull(total, 0);
    }
}
