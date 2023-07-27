package com.colipu.controller;


import com.colipu.dto.Result;
import com.colipu.service.IFindConfigService;
import com.colipu.service.impl.FindConfigServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/Config")
public class FindConfigController {

    @Resource
    private IFindConfigService findConfigService;

    @RequestMapping("/searchNacos")
    public Result searchNacos(@RequestParam(required = false, value = "instanceId", defaultValue = "mse_prepaid_public_cn-2r42fir9y0f") String instanceId,
                              @RequestParam("nameSpaceId") String nameSpaceId,
                              @RequestParam(required = false, value = "pageNum", defaultValue = "1") Integer pageNum,
                              @RequestParam(required = false, value = "pageSize", defaultValue = "500") Integer pageSize,
                              @RequestParam("targetSubstring") String targetSubString) throws Exception {
        return findConfigService.findConfig(instanceId, nameSpaceId, pageNum, pageSize, targetSubString);
    }

    @RequestMapping("/searchShared")
    public Result searchSharedFile(@RequestParam("ip") String ip,
                                   @RequestParam("targetSubstring") String targetSubstring) {
        return findConfigService.findSharedFileConfig(ip, targetSubstring);
    }
}
