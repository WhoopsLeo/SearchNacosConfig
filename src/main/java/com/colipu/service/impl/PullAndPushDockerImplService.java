package com.colipu.service.impl;

import com.colipu.model.dto.Result;
import com.colipu.service.IDockerService;
import com.colipu.service.IPullAndPushDockerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class PullAndPushDockerImplService implements IPullAndPushDockerService {

    @Resource(name = "AliCloud")
    private IDockerService dockerServiceAli;

    @Resource(name = "HuaWeiCloud")
    private IDockerService dockerServiceHuawei;

    @Async("taskExecutor")
    @Override
    public void pullAndTagAndPushImage(String imageName) {

        imageName = "shc0itacrhub01-registry-vpc.cn-shanghai.cr.aliyuncs.com/clp-uat/" + imageName;
        boolean pullResult = dockerServiceAli.pullImage(imageName);
        if (!pullResult) {
            log.info("拉取镜像：" + imageName + "，失败！");
        } else {
            log.info("拉取镜像：" + imageName + "，成功！");
        }
        String[] imageNameSplit = imageName.split("/");
        String newImageName = "swr.cn-east-3.myhuaweicloud.com/clp-prd/" + imageNameSplit[2];
        boolean tagResult = dockerServiceHuawei.tagImage(imageName, newImageName);
        if (!tagResult) {
            log.info("原镜像：" + imageName + "打tag" + "，失败！");
        } else {
            log.info("新镜像：" + newImageName + "打tag" + "，成功！");
        }

        boolean pushResult = dockerServiceHuawei.pushImage(newImageName);
        if (!pushResult) {
            log.info("push镜像：" + newImageName + "，失败！");
        } else {
            log.info("push镜像：" + newImageName + "，成功！");
        }


    }
}
