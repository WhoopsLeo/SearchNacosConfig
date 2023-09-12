package com.colipu.controller;

import com.colipu.model.dto.DeploymentsAndImages.*;
import com.colipu.model.dto.Result;
import com.colipu.service.IKubernetesService;
import com.colipu.service.IPullAndPushDockerService;
import com.colipu.service.IRepositoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/opstools/Deployment")
public class DeployController {
    @Resource
    private IRepositoryService repositoryService;
    @Resource
    private IKubernetesService kubernetesService;

    @Resource
    private IPullAndPushDockerService pullAndPushDockerService;

    /**
     * 根据imageName获取所有Tags
     *
     * @param repoName
     * @param instanceId
     * @param repoNamespaceName
     * @param repoStatus
     * @return
     */
    @RequestMapping("/listImageTags")
    public Result listImageTags(@RequestParam(required = true, value = "imageName") String repoName,
                                @RequestParam(required = false, value = "instanceId", defaultValue = "cri-2xl36ikg0mg1eczp") String instanceId,
                                @RequestParam(required = false, value = "repoNamespaceName", defaultValue = "clp-uat") String repoNamespaceName,
                                @RequestParam(required = false, value = "repoStatus", defaultValue = "NORMAL") String repoStatus) {
        List<String> imageTags = repositoryService.listRepository(repoName, instanceId, repoNamespaceName, repoStatus);
        if (imageTags == null) {
            return Result.fail("阿里云仓库中无该镜像！");
        }
        return Result.ok(imageTags, (long) imageTags.size());
    }

    /**
     * 获取华为云Deployment及其对应的Image
     *
     * @return
     */
    @RequestMapping("/findDeploymentsAndImages")
    public Result findDeploymentsAndImages(@RequestParam(required = true, value = "nameSpace") String nameSpace) {
        List<DeploymentsAndImagesDTO> deploymentsAndImagesDTOs = kubernetesService.findDeploymentsAndImages(nameSpace);
        if (deploymentsAndImagesDTOs == null) {
            return Result.fail("没有查询到相关内容或查询Deployment名称和Image名称失败");
        }
        return Result.ok(deploymentsAndImagesDTOs, (long) deploymentsAndImagesDTOs.size());
    }

    /**
     * 根据imageName从阿里云pull镜像仓库并push到华为云镜像仓库
     *
     * @param imageName
     * @return
     */
    @RequestMapping("/pullAndPushImage")
    public Result pullAndPushImage(@RequestParam(required = true, value = "imageName") String imageName) {

        pullAndPushDockerService.pullAndTagAndPushImage(imageName);
        return Result.ok("正在发布...");
//        imageName = "shc0itacrhub01-registry-vpc.cn-shanghai.cr.aliyuncs.com/clp-uat/" + imageName;
//        boolean pullResult = dockerServiceAli.pullImage(imageName);
//        if (!pullResult) {
//            return Result.fail("拉取镜像：" + imageName + "，失败！");
//        } else {
//            log.info("拉取镜像：" + imageName + "，成功！");
//        }
//        String[] imageNameSplit = imageName.split("/");
//        String newImageName = "swr.cn-east-3.myhuaweicloud.com" + "/clp-prd/" + imageNameSplit[2];
//        boolean tagResult = dockerServiceHuawei.tagImage(imageName, newImageName);
//        if (!tagResult) {
//            return Result.fail("原镜像：" + imageName + "打tag" + "，失败！");
//        } else {
//            log.info("新镜像：" + newImageName + "打tag" + "，成功！");
//        }
//
//        boolean pushResult = dockerServiceHuawei.pushImage(newImageName);
//        if (!pushResult) {
//            return Result.fail("push镜像：" + newImageName + "，失败！");
//        } else {
//            log.info("push镜像：" + newImageName + "，成功！");
//        }
//
//        return Result.ok("push镜像：" + newImageName + "，成功！");
    }

}
