package com.colipu.utils;

import com.aliyun.mse20190531.Client;
import com.aliyun.mse20190531.models.GetNacosConfigRequest;
import com.aliyun.mse20190531.models.GetNacosConfigResponse;
import com.aliyun.mse20190531.models.ListNacosConfigsResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.colipu.dto.ConfigurationDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class GetNacosConfigCallable implements Callable<List<ConfigurationDto>> {

    private ListNacosConfigsResponseBody.ListNacosConfigsResponseBodyConfigurations nacosConfig;

    private Client client;

    private String instanceId;

    private String nameSpaceId;

    private String dataId;

    private String group;

    private String targetSubString;

    private CountDownLatch latch;


    public GetNacosConfigCallable() {
    }

    public GetNacosConfigCallable(ListNacosConfigsResponseBody.ListNacosConfigsResponseBodyConfigurations nacosConfig, Client client, String instanceId, String nameSpaceId, String dataId, String group, String targetSubString, CountDownLatch latch) {
        this.nacosConfig = nacosConfig;
        this.client = client;
        this.instanceId = instanceId;
        this.nameSpaceId = nameSpaceId;
        this.dataId = dataId;
        this.group = group;
        this.targetSubString = targetSubString;
        this.latch = latch;
    }


    public ListNacosConfigsResponseBody.ListNacosConfigsResponseBodyConfigurations getNacosConfig() {
        return nacosConfig;
    }

    public void setNacosConfig(ListNacosConfigsResponseBody.ListNacosConfigsResponseBodyConfigurations nacosConfig) {
        this.nacosConfig = nacosConfig;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getNameSpaceId() {
        return nameSpaceId;
    }

    public void setNameSpaceId(String nameSpaceId) {
        this.nameSpaceId = nameSpaceId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTargetSubString() {
        return targetSubString;
    }

    public void setTargetSubString(String targetSubString) {
        this.targetSubString = targetSubString;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    /**
     * 获取Nacos配置文件的内容
     * @param client
     * @param instanceId
     * @param nameSpaceId
     * @param dataId
     * @param group
     * @return
     */
    public String getNacosConfig(
            Client client,
            String instanceId,
            String nameSpaceId,
            String dataId,
            String group) {
        GetNacosConfigRequest getNacosConfigRequest = new GetNacosConfigRequest()
                .setInstanceId(instanceId)
                .setDataId(dataId)
                .setGroup(group)
                .setNamespaceId(nameSpaceId);
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            GetNacosConfigResponse nacosConfigWithOptions = client.getNacosConfigWithOptions(getNacosConfigRequest, runtime);
            // 获得配置文件的具体内容
            String content = nacosConfigWithOptions.getBody().getConfiguration().getContent();
            return content;
        } catch (TeaException error) {
            System.out.println(Common.assertAsString(error.message));
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            System.out.println(Common.assertAsString(error.message));
        }
        return null;
    }

    /**
     * 搜索配置文件是否包含目标字符串
     * @param dataId
     * @param content
     * @param targetSubString
     * @return 目标字符串所在的一行的字符串
     */
    public List<String> matchSubString(String dataId, String content, String targetSubString) {
        if (!content.contains(targetSubString)) {
            return null;
        }
//        StringBuffer sb = new StringBuffer();// 拼接字符串解锁这个
        List<String> matchedLine = new ArrayList<>();
        String[] linesArray = content.split("\n");
//        sb.append("DataId: " + dataId + " ");
        for (int i = 0; i < linesArray.length; i++) {
            String line = linesArray[i];
            if (line.contains(targetSubString)) {
//                sb.append("Config_Content:");
//                if (i - 1 >= 0) {
//                    sb.append(linesArray[i - 1]);
//                }
//                sb.append(line);// 拼接字符串解锁这个
                matchedLine.add(line);
//                if (i + 1 < linesArray.length) {
//                    sb.append(linesArray[i + 1]);
//                }
            }
        }
        return matchedLine;
    }


    @Override
    public List<ConfigurationDto> call() {
        try{

            String dataId = nacosConfig.getDataId();
            String group = nacosConfig.getGroup();
            String content;
            content = getNacosConfig(client, instanceId, nameSpaceId, dataId, group);
            if (content == null) {
                System.out.println("调阿里云接口，获取的Nacos配置文件为null");
                return null;
            }
            List<String> matchedLines = matchSubString(dataId, content, targetSubString);
            if(matchedLines == null){
                return null;
            }
            ArrayList<ConfigurationDto> result = new ArrayList<>();
            for (String mathedLine : matchedLines) {
                ConfigurationDto configurationDto = new ConfigurationDto();
                configurationDto.setGroup(group);
                configurationDto.setDataId(dataId);
                configurationDto.setContent(mathedLine);
                result.add(configurationDto);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            latch.countDown();
        }
    }
}
