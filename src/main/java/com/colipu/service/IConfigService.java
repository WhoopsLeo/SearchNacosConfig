package com.colipu.service;

import com.colipu.dto.ConfigurationDto;
import com.colipu.dto.Result;

import java.util.List;

public interface IConfigService {
    Result findConfig(String instanceId, String nameSpaceId, Integer pageNum, Integer pageSize,String targetSubString) throws Exception;

    Result findSharedFileConfig(String ip, String targetSubString);

}
