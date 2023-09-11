package com.colipu.utils;

public class AliyunConnect {

    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static com.aliyun.cr20181201.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // Endpoint 请参考 https://api.aliyun.com/product/cr
        config.endpoint = "cr.cn-shanghai.aliyuncs.com";
        return new com.aliyun.cr20181201.Client(config);
    }

    /**
     * 使用STS鉴权方式初始化账号Client，推荐此方式。
     * @param accessKeyId
     * @param accessKeySecret
     * @param securityToken
     * @return Client
     * @throws Exception
     */
    public static com.aliyun.cr20181201.Client createClientWithSTS(String accessKeyId, String accessKeySecret, String securityToken) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret)
                // 必填，您的 Security Token
                .setSecurityToken(securityToken)
                // 必填，表明使用 STS 方式
                .setType("sts");
        // Endpoint 请参考 https://api.aliyun.com/product/cr
        config.endpoint = "cr.cn-shanghai.aliyuncs.com";
        return new com.aliyun.cr20181201.Client(config);
    }
}
