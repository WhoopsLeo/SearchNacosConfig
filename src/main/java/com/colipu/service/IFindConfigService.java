package com.colipu.service;

import com.colipu.dto.Result;

public interface IFindConfigService {
    Result findConfig(String instanceId, String nameSpaceId, Integer pageNum, Integer pageSize,String targetSubString) throws Exception;

}
