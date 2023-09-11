package com.colipu.model.dto.Configuration;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigurationDto {
    private String group;
    private String dataId;
    private String content;

}
