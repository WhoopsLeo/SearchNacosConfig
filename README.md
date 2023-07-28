# 搜索Nacos与共享目录配置接口

- **搜索Nacos配置**
    - 请求路径，如：http://matchconfigapi.colipu.com/Config/searchNacos?nameSpaceId=5dd37175-b149-4a53-8799-ebcb704d0ce0&targetSubstring=TCP_HOST
        - 请求参数：
          1. （必填）nameSpaceId: Nacos的命名空间Id
          2. （必填）targetSubString: 想搜索的配置字段 
          3. （选填）instanceId：Nacos实例Id，默认值：mse_prepaid_public_cn-2r42fir9y0f
          4. （选填）pageNum: 查看第pageNum页，默认值：1
          5. （选填）pageSize: 每页pageSize个数据，默认值：500



- **搜索共享目录配置**
  - 请求路径，如：http://matchconfigapi.colipu.com/Config/searchShared?ip=10.10.18.109&targetSubstring=TCP_HOST
    - 请求参数：
      1. （必填）ip: 共享目录ip
      2. （必填）targetSubString: 想搜索的配置字段