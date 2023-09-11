package com.colipu.service.impl;

import com.colipu.service.IDockerService;
import com.colipu.utils.HuaWeiCloudDockerManager;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service("HuaWeiCloud")
public class DockerServiceHuaWeimpl implements IDockerService {

    @Resource
    private HuaWeiCloudDockerManager dockerManager;


    /**
     * 拉取docker镜像
     */
    public boolean pullImage(String imagePath) {
//        String imagePath = outerRegistry + "/" + registryPrefix + "/" + groupName + "/" + projectName + ":" + originVersion;
        try {
            dockerManager.downloadDockerImage(imagePath, 90);
            return true;
        } catch (NotFoundException | InternalServerErrorException e) {
            if (dockerManager.dockerImageExistsLocally(imagePath)) {
                log.info("Docker image '{}' exists locally", imagePath);
                return false;
            } else {
                log.error("Error on Pulling '{}' image.{}", imagePath,e);
                log.error("报错信息：",e);
            }
        } catch (DockerClientException e) {
            log.error("Error on Pulling '{}' image. Probably because the user has stopped the execution {}", imagePath,e);
            log.error("报错信息：",e);
        } catch (InterruptedException e) {
            log.error("Error on Pulling '{}' image. Thread was interrupted: {}", imagePath, e);
            log.error("报错信息：",e);
        }
        return false;
    }

    /**
     * 镜像推送到远程仓库
     */
    public boolean pushImage(String newImageName) {
        String newTag = newImageName.split(":")[1];
        return dockerManager.pushImage(newImageName, newTag);
    }

    /**
     * 给镜像打标签
     * @param oldImageName
     * @param newImageNameWithTag
     *
     */
    public boolean tagImage(String oldImageName, String newImageNameWithTag) {
        String newImageName = newImageNameWithTag.split(":")[0];
        String newTag = newImageNameWithTag.split(":")[1];
        dockerManager.tagImage(oldImageName, newImageName, newTag);
        return dockerManager.dockerImageExistsLocally(newImageName +":"+newTag);
    }

}
