package com.colipu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NacosConfigurationDto {
    private String group;
    private String dataId;
    private String content;

}
