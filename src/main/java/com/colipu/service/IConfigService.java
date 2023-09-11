package com.colipu.service;

import com.colipu.model.dto.Result;

public interface IConfigService {
    Result findConfig(String instanceId, String nameSpaceId, Integer pageNum, Integer pageSize,String targetSubString) throws Exception;

    Result findSharedFileConfig(String ip, String targetSubString);

}
