package com.colipu.service.impl;

import com.colipu.model.dto.DeploymentsAndImages.DeploymentsAndImagesDTO;
import com.colipu.service.IKubernetesService;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class KubernetesServiceImpl implements IKubernetesService {
    @Override
    public List<DeploymentsAndImagesDTO> findDeploymentsAndImages() {

        try {
            //      file path to your KubeConfig
            Resource resource = new ClassPathResource("kubeconfig.yaml");
            InputStream inputStream = resource.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

//      loading the out-of-cluster config, a kubeconfig from file-system
            ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(inputStreamReader)).build();

            //      set the global default api-client to the in-cluster one from above
            Configuration.setDefaultApiClient(client);
        } catch (IOException e) {
            log.error("创建K8S客户端异常，异常信息为：" + e);
            return null;
        }


//       the CoreV1Api loads default api-client from global configuration.
//        CoreV1Api apiCore = new CoreV1Api();

        AppsV1Api apiApps = new AppsV1Api();

        List<DeploymentsAndImagesDTO> deploymentsAndImagesDTOs = new ArrayList<>();
        try {

            List<String> nameSpaces = new ArrayList<>(Arrays.asList("erp", "tr", "web"));
            for (String nameSpace : nameSpaces) {
                // 通过namespace获取deployment
                V1DeploymentList deployment = apiApps.listNamespacedDeployment(nameSpace, null, null, null, null, null, null, null, null, null, null);
                if (deployment.getItems().size() == 0){
                    continue;
                }
                for (V1Deployment item : deployment.getItems()) {
                    if (item.getMetadata() == null) {
                        break;
                    }
                    DeploymentsAndImagesDTO deploymentsAndImagesDTO = new DeploymentsAndImagesDTO();
                    String deploymentName = item.getMetadata().getName();
                    deploymentsAndImagesDTO.setDeploymentName(deploymentName);

                    // 通过deploymentName和nameSpace获得Deployment
                    V1Deployment v1Deployment = apiApps.readNamespacedDeployment(deploymentName, nameSpace, null);
                    if (v1Deployment.getSpec() == null) {
                        break;
                    }

                    // 通过deployment获取pod里的容器
                    List<V1Container> containers = v1Deployment.getSpec().getTemplate().getSpec().getContainers();
                    // 因为所有容器都来自于同一个镜像，所以这里只取一个
                    String imageName = containers.get(0).getImage();
                    deploymentsAndImagesDTO.setImageName(imageName);
                    String[] split = imageName.split("[/:]");
                    deploymentsAndImagesDTO.setImageRepoName(split[split.length - 2]);

                    log.info(deploymentsAndImagesDTO.toString());

                    deploymentsAndImagesDTOs.add(deploymentsAndImagesDTO);

                }

            }
        } catch (ApiException e) {
            log.error("获取K8S的Deployment对象异常，异常信息为：" + e);
            return null;
        }

        return deploymentsAndImagesDTOs;
    }
}
