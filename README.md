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

### 原理简介
拦截Spring事务的提交与回滚操作，将状态在Redis中共享，通过2pc协议协商最终达成一起提交或一起回滚

