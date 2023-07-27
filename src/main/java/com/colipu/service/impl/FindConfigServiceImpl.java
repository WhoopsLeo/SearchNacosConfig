package com.colipu.service.impl;

import com.aliyun.mse20190531.Client;
import com.aliyun.mse20190531.models.ListNacosConfigsRequest;
import com.aliyun.mse20190531.models.ListNacosConfigsResponse;
import com.aliyun.mse20190531.models.ListNacosConfigsResponseBody;
import com.aliyun.mse20190531.models.ListNacosConfigsResponseBody.ListNacosConfigsResponseBodyConfigurations;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.colipu.dto.ConfigurationDto;
import com.colipu.dto.Result;
import com.colipu.service.IFindConfigService;
import com.colipu.utils.GetNacosConfigCallable;
import com.colipu.utils.SmbUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@Slf4j
@Service
public class FindConfigServiceImpl implements IFindConfigService {


    /**
     * 查询Nacos配置中是否包含目标子串
     *
     * @param instanceId
     * @param nameSpaceId
     * @param pageNum
     * @param pageSize
     * @param targetSubString
     * @return
     */
    @Override
    public Result findConfig(String instanceId, String nameSpaceId, Integer pageNum, Integer pageSize, String targetSubString) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                30,
                50,
                500,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(1000),
                new ThreadPoolExecutor.AbortPolicy());

        ArrayList<ConfigurationDto> resultList = new ArrayList<>();

        try {

            Client client = createClient(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"), System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));

            List<ListNacosConfigsResponseBodyConfigurations> nacosConfigsList = getNacosConfigsList(client, instanceId, nameSpaceId, pageNum, pageSize);

            if (nacosConfigsList == null) {
                log.error("调阿里云的接口，获取的Nacos配置列表为null");
                return Result.fail("调阿里云的接口，获取的Nacos配置列表为null");
            }

            int numTasks = nacosConfigsList.size();
            // 利用CountDownLatch，确保线程池关闭时，所有配置文件都搜索过了
            CountDownLatch latch = new CountDownLatch(numTasks);
            List<Future<ConfigurationDto>> futures = new ArrayList<>();

            for (ListNacosConfigsResponseBodyConfigurations nacosConfig : nacosConfigsList) {

                GetNacosConfigCallable nacosConfigCallable = new GetNacosConfigCallable(nacosConfig, client, instanceId, nameSpaceId, nacosConfig.getDataId(), nacosConfig.getGroup(), targetSubString, latch);
                Future<ConfigurationDto> future = executor.submit(nacosConfigCallable);
                futures.add(future);
            }
            // 等待CountDownLatch减为0后，在执行后面的代码
            latch.await(20, TimeUnit.SECONDS);


            for (Future<ConfigurationDto> future : futures) {
                if (future.get() != null) {
                    ConfigurationDto configurationDto = future.get();
                    resultList.add(configurationDto);
                }

            }
            // 关闭线程池
            executor.shutdown();

        } catch (Exception e) {
            log.error("配置文件中查询子串异常", e);
            return Result.fail("配置文件中查询子串异常");
        }

        if (resultList.size() == 0) {
            log.error("配置文件中没有该内容");
            return Result.fail("配置文件中没有该内容");
        }

        return Result.ok(resultList, (long) resultList.size());
    }

    /**
     * 查询公盘文件中的配置是否包含目标子串
     *
     * @param targetSubString
     * @return
     */
    @Override
    public Result findSharedFileConfig(String ip,String targetSubString) {
        log.info("获取公盘文件===>> 开始");
        String domain ="colipu";
        String user = "xuwenjie";
//        String user = System.getenv("SHARE_USERNAME");
        String password = "Asdf19971017";
//        String password = System.getenv("SHARE_PASSWORD");

        String invoiceGroupReceiversLocalSharePath= "smb://"+domain+";"+user+":"+password+"@"+ ip +"/moveinconfig/";
        SmbUtil.ConnectionState(ip,domain,user,password);


        return SmbUtil.getSharedFileList(invoiceGroupReceiversLocalSharePath,targetSubString);
    }

    /**
     * 使用AK&SK初始化账号Client
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public  Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // Endpoint 请参考 https://api.aliyun.com/product/mse
        config.endpoint = "mse.cn-shanghai.aliyuncs.com";
        return new Client(config);
    }

    /**
     * 获取Nacos配置列表
     * @param client
     * @param instanceId
     * @param nameSpaceId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public  List<ListNacosConfigsResponseBodyConfigurations> getNacosConfigsList(
            Client client,
            String instanceId,
            String nameSpaceId,
            Integer pageNum,
            Integer pageSize) {
        ListNacosConfigsRequest listNacosConfigsRequest = new ListNacosConfigsRequest()
                .setInstanceId(instanceId)
                .setNamespaceId(nameSpaceId)
                .setPageNum(pageNum)
                .setPageSize(pageSize);
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            ListNacosConfigsResponse listNacosConfigsResponse = client.listNacosConfigsWithOptions(listNacosConfigsRequest, runtime);
            ListNacosConfigsResponseBody body = listNacosConfigsResponse.getBody();
            return body.getConfigurations();
        } catch (TeaException error) {
            log.error("获取阿里云Nacos配置列表出错", error);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            log.error("获取阿里云Nacos配置列表出错", error);
        }
        return null;
    }
}
