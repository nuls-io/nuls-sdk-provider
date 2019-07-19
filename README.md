# NULS2.0 SDK-Provider

**SDK模块，与核心模块连接，依赖离线SDK包，以HTTP接口作为服务，提供了`JSON-RPC`和`RESTFUL`的请求方式，
以此模块作为访问底层钱包数据的桥梁，用于在线和离线的交易组装、查询等功能**


- **`JSON-RPC`访问方式**

     添加请求头 Content-Type: application/json;charset=UTF-8
     
     HttpMethod: POST
     
     URL: http://${ip}:${port}/jsonrpc 
     
        示例: http://127.0.0.1:9898/jsonrpc
     
     请求数据格式: 
     
     ```json
     {
       "jsonrpc":"2.0",
       "method":"methodCMD", //接口名称
       "params":[],          //所有接口的参数，都以数组方式传递，且参数顺序不能变，若参数是非必填，也必须填入null占位
       "id":1234
     }
     ```

- **`RESTFUL`访问方式**

     添加请求头 Content-Type: application/json;charset=UTF-8
     
     其余请参考 [RESTFUL 接口文档](https://github.com/nuls-io/nuls-sdk-provider/blob/master/documents/nuls-sdk-provider_RESTFUL.md)

## HttpServer默认IP和Port

- 默认值

    `ip: 0.0.0.0`
    
    `port : 9898`

- 如何更改

    编辑项目根路径下的module.ncf文件 `${project}/module.ncf`
    
    找到组`[nuls-sdk-provider]`
    
    更改`server_ip`和`server_port`
    
    ```ncf
    [nuls-sdk-provider]
    #dependent=smart-contract,cross-chain
    moduleDynamicDependent=smart-contract,cross-chain
    # httpServer的启动ip
    server_ip=0.0.0.0
    # httpServer的启动port
    server_port=9898
    ```


## 接口文档

我们提供了两种请求方式`JSON-RPC`和`RESTFUL`的文档

[JSON-RPC 接口文档](https://github.com/nuls-io/nuls-sdk-provider/blob/master/documents/nuls-sdk-provider_JSONRPC.md)

[RESTFUL 接口文档](https://github.com/nuls-io/nuls-sdk-provider/blob/master/documents/nuls-sdk-provider_RESTFUL.md)

## 接口调试

我们提供了`Postman`接口调式工具的导入文件(`JSON-RPC`和`RESTFUL`)，导入后，即可调试接口

[JSON-PRC 接口调试-POSTMAN导入文件](https://github.com/nuls-io/nuls-sdk-provider/blob/master/documents/nuls-sdk-provider_Postman_JSONRPC.json)

[RESTFUL 接口调试-POSTMAN导入文件](https://github.com/nuls-io/nuls-sdk-provider/blob/master/documents/nuls-sdk-provider_Postman_RESTFUL.json)


