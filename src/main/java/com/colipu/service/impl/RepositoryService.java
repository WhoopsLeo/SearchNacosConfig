package com.colipu.service.impl;

import com.aliyun.cr20181201.models.ListRepoTagResponse;
import com.aliyun.cr20181201.models.ListRepoTagResponseBody.ListRepoTagResponseBodyImages;
import com.aliyun.cr20181201.models.ListRepositoryResponseBody.ListRepositoryResponseBodyRepositories;
import com.colipu.service.IRepositoryService;
import com.colipu.utils.AliyunConnect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RepositoryService implements IRepositoryService {
    @Override
    public List<String> listRepository(String repoName,String instanceId, String repoNamespaceName,String repoStatus) {

        try {
            com.aliyun.cr20181201.Client client = AliyunConnect.createClient(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"), System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
            // 根据namespace获取repoId
            com.aliyun.cr20181201.models.ListRepositoryRequest listRepositoryRequest = new com.aliyun.cr20181201.models.ListRepositoryRequest()
                    .setInstanceId(instanceId)
                    .setRepoStatus(repoStatus)
                    .setRepoName(repoName)
                    .setRepoNamespaceName(repoNamespaceName);
            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
            com.aliyun.cr20181201.models.ListRepositoryResponse resp = client.listRepositoryWithOptions(listRepositoryRequest, runtime);
            List<ListRepositoryResponseBodyRepositories> repositories = resp.getBody().getRepositories();
            // crr-c9z19tpk3pe7pfge
            String repoId= repositories.get(0).getRepoId();

            // 通过repoId和instanceId获取image的tag
            com.aliyun.cr20181201.models.ListRepoTagRequest listRepoTagRequest = new com.aliyun.cr20181201.models.ListRepoTagRequest()
                    .setRepoId(repoId)
                    .setInstanceId(instanceId);
            ListRepoTagResponse listRepoTagResponse = client.listRepoTagWithOptions(listRepoTagRequest, runtime);
            List<ListRepoTagResponseBodyImages> images = listRepoTagResponse.getBody().getImages();
            List<String> imageTags = images.stream()
                    .map(image -> image.getTag())
                    .collect(Collectors.toList());
            return imageTags;
        } catch (Exception e) {
            log.error("查询阿里云镜像仓库列表异常,异常信息:", e);
            throw new RuntimeException(e);
        }
    }
}
