# 搜索Nacos与共享目录配置接口

- **搜索Nacos配置**

    - 请求路径，如：http://matchconfigapi.colipu.com/Config/searchNacos?nameSpaceId=5dd37175-b149-4a53-8799-ebcb704d0ce0&targetSubstring=TCP_HOST
        - 请求参数：

          | 名称            | 类型    | 描述                   | 必填 | 示例                                      |
          | :-------------- | :------ | :---------------------- | ---- | :---------------------------------------- |
          | nameSpaceId     | String  | Nacos的命名空间ID。     | 是   | 200                                       |
          | targetSubString | Integer | 想搜索的配置关键字。    | 是   | 12                                        |
          | instanceId      | String  | Nacos实例IdID。         | 否   | 默认值：mse_prepaid_public_cn-2r42fir9y0f |
          | pageNum         | Integer | 查看第pageNum页的数据。 | 否   | 默认值：1                                 |
          | pageSize        | Integer | 每页有pageSize个数据。  | 否   | 默认值：500                               |

        - 响应参数：
        
          | 名称      | 类型    | 描述                                         | 示例                                 |
          | :-------- | :------ | :------------------------------------------- | :----------------------------------- |
          | success   | Boolean | 请求结果。true：请求成功 。false：请求失败。 | true                                 |
          | errorMsg  | String  | 错误信息。                                   |                                      |
          | data      |         |                                              |                                      |
          | - group   | String  | 配置文件所属组。                             | tr                                   |
          | - dataId  | String  | 配置文件的dataID。                           | spring_public_java.yml               |
          | - content | String  | 配置文件中匹配到的那一行数据。               | port: ${MANAGEMENT_SERVER_PORT:8888} |
          | total     | Long    | 匹配到的配置文件总数。                       | 1                                    |



- **搜索共享目录配置**
  
    - 请求路径，如：http://matchconfigapi.colipu.com/Config/searchShared?ip=10.10.18.109&targetSubstring=TCP_HOST
        - 请求参数：
    
          | 名称            | 类型    | 描述                 | 必填 | 示例         |
          | :-------------- | :------ | :------------------- | ---- | :----------- |
          | ip              | String  | 共享目录ip。         | 是   | 10.10.112.21 |
          | targetSubString | Integer | 想搜索的配置关键字。 | 是   | port         |
        
        - 响应参数：
        
          | 名称      | 类型    | 描述                                         | 示例                                                      |
          | :-------- | :------ | :------------------------------------------- | :-------------------------------------------------------- |
          | success   | Boolean | 请求结果。true：请求成功 。false：请求失败。 | true                                                      |
          | errorMsg  | String  | 错误信息。                                   | 12                                                        |
          | data      |         |                                              |                                                           |
          | - group   | null    | null                                         | null                                                      |
          | - dataId  | String  | 配置文件所在路径。                           | 10.10.112.21/moveinconfig/iis/InvoiceBasic_API/Web.config |
          | - content | String  | 配置文件中匹配到的那一行数据。               | <transport clientCredentialType=\"None\"/>                |
          | total     | Long    | 匹配到的配置文件总数。                       | 1                                                         |

        


- **搜索结果导出成Excel**
    
    - 请求路径，如：http://matchconfigapi.colipu.com/Config/download
        - 请求体：
    
          | 名称       | 类型   | 描述                           | 示例                                                         |
          | :--------- | :----- | :----------------------------- | :----------------------------------------------------------- |
          | configList |        |                                |                                                              |
          | - group    | String | 配置文件所属组。               | tr或null                                                     |
          | - dataId   | String | 配置文件的dataID。             | spring_public_java.yml 或10.10.112.21/moveinconfig/iis/InvoiceBasic_API/Web.config |
          | - content  | String | 配置文件中匹配到的那一行数据。 | port: ${MANAGEMENT_SERVER_PORT:8888}                         |


- **host文件更改**

    需要在本机的hosts文件中，配置ip与域名的映射。
   
    阿里云上配置的ip地址与域名的映射为10.101.103.237	matchconfigapi.colipu.com，所以本机的hosts文件也要改成这样。