package com.colipu.controller;


import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.excel.EasyExcel;
import com.colipu.dto.ConfigurationDto;
import com.colipu.dto.Result;
import com.colipu.exception.BusinessException;
import com.colipu.service.IConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/Config")
@Slf4j
public class ConfigController {

    @Resource
    private IConfigService configService;

    @RequestMapping("/searchNacos")
    public Result searchNacos(@RequestParam(required = false, value = "instanceId", defaultValue = "mse_prepaid_public_cn-2r42fir9y0f") String instanceId,
                              @RequestParam("nameSpaceId") String nameSpaceId,
                              @RequestParam(required = false, value = "pageNum", defaultValue = "1") Integer pageNum,
                              @RequestParam(required = false, value = "pageSize", defaultValue = "500") Integer pageSize,
                              @RequestParam("targetSubstring") String targetSubString) throws Exception {
        return configService.findConfig(instanceId, nameSpaceId, pageNum, pageSize, targetSubString);
    }

    @RequestMapping("/searchShared")
    public Result searchSharedFile(@RequestParam("ip") String ip,
                                   @RequestParam("targetSubstring") String targetSubstring) {
        return configService.findSharedFileConfig(ip, targetSubstring);
    }

    @RequestMapping("/download")
    public void download(@RequestBody List<ConfigurationDto> configList, HttpServletResponse response) {

        try {
            // 设置响应
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("configFile", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 写入数据
            EasyExcel.write(response.getOutputStream(), ConfigurationDto.class).sheet("sheet1").doWrite(configList);
        } catch (IOException e) {
            log.error("Excel写入失败，错误原因：{}", ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("Excel写入失败！");
        }


    }

}
