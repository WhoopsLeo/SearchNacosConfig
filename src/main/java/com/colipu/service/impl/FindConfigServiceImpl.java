package com.colipu.service.impl;

import com.aliyun.mse20190531.Client;
import com.aliyun.mse20190531.models.ListNacosConfigsRequest;
import com.aliyun.mse20190531.models.ListNacosConfigsResponse;
import com.aliyun.mse20190531.models.ListNacosConfigsResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.colipu.dto.NacosConfigurationDto;
import com.colipu.dto.Result;
import com.colipu.service.IFindConfigService;
import com.colipu.utils.GetNacosConfigCallable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class FindConfigServiceImpl implements IFindConfigService {


    @Override
    public Result findConfig(String instanceId, String nameSpaceId, Integer pageNum, Integer pageSize,String targetSubString) {
        SimpleDateFormat sdf = null;// 格式化时间
        ArrayList<NacosConfigurationDto> resultList = null;
        try {
            sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
            System.out.println("开始检索：" + sdf.format(new Date()));

            resultList = new ArrayList<>();
            // 请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID 和 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
            // 工程代码泄露可能会导致 AccessKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
            Client client = createClient(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"), System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));

            List<ListNacosConfigsResponseBody.ListNacosConfigsResponseBodyConfigurations> nacosConfigsList = getNacosConfigsList(client, instanceId, nameSpaceId, pageNum, pageSize);
            if (nacosConfigsList == null) {
                return Result.fail("调阿里云，获取的Nacos配置列表为null");
            }

            // 创建线程池
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    30,
                    40,
                    1L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingDeque<Runnable>(),
                    new ThreadPoolExecutor.AbortPolicy());


            int numTasks = nacosConfigsList.size();
            CountDownLatch latch = new CountDownLatch(numTasks);
            List<Future<NacosConfigurationDto>> futures = new ArrayList<>();

            for (ListNacosConfigsResponseBody.ListNacosConfigsResponseBodyConfigurations nacosConfig : nacosConfigsList) {

                GetNacosConfigCallable nacosConfigCallable = new GetNacosConfigCallable(nacosConfig, client, instanceId, nameSpaceId, nacosConfig.getDataId(), nacosConfig.getGroup(), targetSubString, latch);
                Future<NacosConfigurationDto> future = executor.submit(nacosConfigCallable);
                    futures.add(future);
            }
            latch.await(20, TimeUnit.SECONDS);


            for (Future<NacosConfigurationDto> future : futures) {
                if (future.get() != null) {
                    NacosConfigurationDto nacosConfigurationDto = future.get();
                    resultList.add(nacosConfigurationDto);
                }

            }

            executor.shutdown();
        } catch (Exception e) {
            log.error("查询异常",e);
            return Result.fail("查询异常");
        }
        System.out.println("检索完成：" + sdf.format(new Date()));

        if (resultList.size() == 0) {
            return Result.fail("没有该内容");
        }

        String result = resultList.toString();
        System.out.print(result);
        return Result.ok(resultList, (long) resultList.size());
    }

    /**
     * 使用AK&SK初始化账号Client
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // Endpoint 请参考 https://api.aliyun.com/product/mse
        config.endpoint = "mse.cn-shanghai.aliyuncs.com";
        return new Client(config);
    }

    public static List<ListNacosConfigsResponseBody.ListNacosConfigsResponseBodyConfigurations> getNacosConfigsList(
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
            // 复制代码运行请自行打印 API 的返回值
            ListNacosConfigsResponse listNacosConfigsResponse = client.listNacosConfigsWithOptions(listNacosConfigsRequest, runtime);
            // 获取响应体
            ListNacosConfigsResponseBody body = listNacosConfigsResponse.getBody();
            // 获取配置类
            return body.getConfigurations();
        } catch (TeaException error) {
            // 如有需要，请打印 error
            System.out.println(Common.assertAsString(error.message));
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 如有需要，请打印 error
            log.error("获取阿里云Nacos配置列表出错",error);
        }
        return null;
    }
}
