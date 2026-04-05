package com.txy.chefdemo.resp;

import com.txy.chefdemo.domain.dto.WalletRecordDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryWalletRecordResp {
    private BaseResp baseResp;
    private List<WalletRecordDTO> data;
    private int total;
}
