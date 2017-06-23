# magic-database
一个高性能、高可用、可扩展、强一致的key-value存储，数据落盘，写比读快。

目前全部使用java实现，底层使用bitcask策略存储。整个系统分为server和gateway，gateway负责路由请求、分片、根据NWR策略的具体配置可以实现读写强一致。

# 部署方案
部署时gateway可以和server都部署在服务端，这也是大量部署时的推荐方案；gateway也可以直接用作client，这种情况比较适合小规模部署。

# 部署时请注意
部署gateway的机器系统时间需要尽量一致

# 客户端
* java: [github.com/yinwenhao/merlin-client](https://github.com/yinwenhao/merlin-client)
* go: [github.com/yinwenhao/merlin-client-go](https://github.com/yinwenhao/merlin-client-go)

有问题请联系：yinwenhao_89@hotmail.com
