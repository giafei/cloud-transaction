## 将Spring Boot的事务改造为分布式事务

### 依赖服务
- Redis
- Spring Cloud

### 使用方式
注意看 cloud-transaction-base 中 对Spring Cloud与Spring Boot的版本依赖是否与项目一致

1. 增加maven依赖
```xml
<dependency>
    <groupId>net.giafei.tools</groupId>
    <artifactId>cloud-transaction-2pc-starter</artifactId>
    <version>版本</version>
</dependency>
```

2. 增加配置项
```yaml
net:
  giafei:
    transaction:
      max-wait-time: 10000 #单位毫秒，这个值在所有的微服务必须一致
```

3. 增加包扫描
```java
@SpringBootApplication(scanBasePackages = "net.giafei")
```

4. 保证Redis可用
5. 按单点应用的方式在业务层用`@Transactional`注解使用Spring的事务即

### demo运行
1. 修改各项目中的数据库与Redis的配置项
2. 运行 storage-service/src/main/resource/create_table.sql与order-service/src/main/resource/create_table.sql
3. 启用 eureka-server 项目，等其彻底启动
4. 启动 storage-service与order-service项目
5. 稍等片刻让服务注册，浏览器打开 http://localhost:5501/swagger-ui.html

### 原理简介
拦截Spring事务的提交与回滚操作，将状态在Redis中共享，通过2pc协议协商最终达成一起提交或一起回滚
详见：[https://www.jianshu.com/p/f596da2606b1](https://www.jianshu.com/p/f596da2606b1)

