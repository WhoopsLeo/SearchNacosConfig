package com.colipu.utils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.ws.rs.ProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 本地docker管理类
 */
@Slf4j
@Component
public class HuaWeiCloudDockerManager {


    private DockerClient dockerClient;

    public HuaWeiCloudDockerManager() {
        this.init();
    }


    /**
     * 初始化dockerClient
     */
    public void init() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://10.101.101.77:2376")
                .withRegistryUrl("swr.cn-east-3.myhuaweicloud.com")
                .withRegistryUsername("cn-east-3@NSJUCVPWSEPOHTOEJZHA")
                .withRegistryPassword("d362f9fc6d276e6c8c04799dfeefbf77020f20c9f2e79b0c606f83c234912470")
                .build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
//                .sslConfig(config.getSSLConfig())
                .build();
        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    /**
     * 拉取docker镜像
     *
     * @param image
     * @param secondsOfWait
     * @throws Exception
     */
    public void downloadDockerImage(String image, int secondsOfWait) throws InterruptedException {
        // 拉取镜像
        this.dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion(secondsOfWait,
                TimeUnit.SECONDS);
        log.info("Pull Docker image '{}' success", image);
    }

    /**
     * docker镜像是否本地已存在
     *
     * @param image
     * @return
     * @throws ProcessingException
     */
    public boolean dockerImageExistsLocally(String image) throws ProcessingException {
        boolean imageExists = false;
        try {
            this.dockerClient.inspectImageCmd(image).exec();
            imageExists = true;
        } catch (NotFoundException nfe) {
            imageExists = false;
        } catch (ProcessingException e) {
            throw e;
        }
        return imageExists;
    }

    /**
     * 通过本地是否存在hello-world镜像判断docker是否已经安装且可用
     *
     * @throws
     */
    public void checkDockerEnabled() {
        try {
            this.dockerImageExistsLocally("hello-world");
            log.info("Docker is installed and enabled");
        } catch (ProcessingException exception) {
            exception.printStackTrace();
//			throw new
        }
    }


    /**
     *  创建并运行容器
     */
    public String runContainer(String image, String containerName, String user,
                               List<Volume> volumes, List<Bind> binds, String networkMode, List<String> envs, List<String> command,
                               Long shmSize, boolean privileged, Map<String, String> labels) throws Exception {

        CreateContainerCmd cmd = dockerClient.createContainerCmd(image).withEnv(envs);
        if (containerName != null) {
            cmd.withName(containerName);
        }

        if (user != null) {
            cmd.withUser(user);
        }

        HostConfig hostConfig = new HostConfig().withNetworkMode(networkMode).withPrivileged(privileged);
        if (shmSize != null) {
            hostConfig.withShmSize(shmSize);
        }
        if (volumes != null) {
            cmd.withVolumes(volumes);
        }
        if (binds != null) {
            hostConfig.withBinds(binds);
        }

        if (labels != null) {
            cmd.withLabels(labels);
        }

        if (command != null) {
            cmd.withCmd(command);
        }

        cmd.withHostConfig(hostConfig);

        CreateContainerResponse response = null;
        try {
            response = cmd.exec();
            dockerClient.startContainerCmd(response.getId()).exec();
            log.info("Container ID: {}", response.getId());
            return response.getId();
        } catch (ConflictException e) {
            log.error(
                    "The container name {} is already in use. Probably caused by a session with unique publisher re-publishing a stream",
                    containerName);
            throw e;
        } catch (NotFoundException e) {
            log.error("Docker image {} couldn't be found in docker host", image);
            throw e;
        }
    }


    /**
     * 删除容器
     */
    public void removeContainer(String containerId, boolean force) {
        dockerClient.removeContainerCmd(containerId).withForce(force).exec();
    }

    /**
     * 强制删除以xxx开始的容器
     *
     * @param imageName
     */
    public void cleanStrandedContainers(String imageName) {
        List<Container> existingContainers = this.dockerClient.listContainersCmd().withShowAll(true).exec();
        for (Container container : existingContainers) {
            if (container.getImage().startsWith(imageName)) {
                log.info("Stranded {} Docker container ({}) removed on startup", imageName, container.getId());
                this.dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
            }
        }
    }

    /**
     * 得到运行中的container
     *
     * @param fullImageName
     * @return
     */
    public List<String> getRunningContainers(String fullImageName) {
        List<String> containerIds = new ArrayList<>();
        List<Container> existingContainers = this.dockerClient.listContainersCmd().exec();
        for (Container container : existingContainers) {
            if (container.getImage().startsWith(fullImageName)) {
                containerIds.add(container.getId());
            } else if (container.getImageId().contains(fullImageName)) {
                containerIds.add(container.getId());
            }
        }
        return containerIds;
    }

    /**
     * 得到imageId
     *
     * @param fullImageName
     * @return
     */
    public String getImageId(String fullImageName) {
        InspectImageResponse imageResponse = this.dockerClient.inspectImageCmd(fullImageName).exec();
        return imageResponse.getId();
    }

    /**
     * 给镜像打tag
     *
     * @param oldImageName  带tag
     * @param newImageName  不带tag
     * @param tag new镜像
     * @return
     */
    public void tagImage(String oldImageName, String newImageName, String tag) {
        this.dockerClient.tagImageCmd(oldImageName, newImageName, tag).exec();
        log.info("tag Docker image '{}' success", newImageName+":"+tag);
    }

    /**
     * push镜像到远端的私有huber仓库
     * name 镜像名
     * tag
     **/
    public boolean pushImage(String name,String tagRes){
        PushImageCmd pushImageCmd = dockerClient.pushImageCmd(name).withTag(tagRes);
        PushImageResultCallback callback = new PushImageResultCallback() {
            @Override
            public void onNext(PushResponseItem item) {
                log.info(item.toString());
                super.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Failed to exec start:" + throwable.getMessage());
                super.onError(throwable);
            }
        };
        pushImageCmd.exec(callback).awaitSuccess();
        log.info("push Docker image '{}' success", name+":"+tagRes);
        return true;
    }

    /**
     * 获取远端仓库的所有镜像
     *
     * @return
     */
    public List<Image> listImages() {
        List<Image> images = this.dockerClient.listImagesCmd().exec();
        return images;
    }

    /**
     * 得到容器配置的标签
     *
     * @param containerId
     * @return
     */
    public Map<String, String> getLabels(String containerId) {
        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
        return containerInfo.getConfig().getLabels();
    }



    /**
     * 得到docker的基本信息
     */
    public void getInfo() {
        Info info = dockerClient.infoCmd().exec();
        System.out.println("docker的环境信息如下：=================");
        System.out.println(info);
    }

}


