# 支付系统

## 准备

- 运行时添加参数：`nacos.address={Nacos地址};environment={环境}`
- Nacos中配置MySQL、Redis、RocketMQ和jasypt，配置文件名称与`bootstrap.properties`一致，Nacos的命名空间与`environment`一致

## 基础数据表

```mysql
CREATE TABLE `pay_order`
(
    `id`               BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_ip`          VARCHAR(50)    NOT NULL COMMENT '用户IP，当前下单用户所在IP',
    `user_id`          VARCHAR(50)    NOT NULL COMMENT '用户ID，当前下单用户在系统中的唯一ID',
    `order_id`         VARCHAR(32)    NOT NULL COMMENT '订单ID，表唯一键，固定32位',
    `order_state`      VARCHAR(10)    NOT NULL COMMENT '订单状态：NOT_PAY-未支付，SUCCESS-已支付，CLOSED-已关闭',
    `order_amount`     DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    `description`      VARCHAR(30)    NOT NULL COMMENT '商品描述，简略描述，详细描述在业务方存储',
    `product_name`     VARCHAR(30)    NOT NULL COMMENT '产品名称，用于避免不同渠道上应用ID重复，命名格式：渠道_产品名称（大写字母）',
    `product_app_id`   VARCHAR(50)    NOT NULL COMMENT '产品应用ID',
    `product_order_id` VARCHAR(50)    NOT NULL DEFAULT '' COMMENT '产品订单ID',
    `product_open_id`  VARCHAR(50)    NOT NULL COMMENT '产品应用用户OpenId',
    `create_time`      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time`      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    `pay_time`         DATETIME(3)    NULL COMMENT '支付时间',
    `expire_time`      DATETIME(3)    NOT NULL COMMENT '过期时间',
    `biz_name`         VARCHAR(30)    NOT NULL COMMENT '业务名称，用于避免不同业务的业务ID重复，命名格式：模块_业务（大写字母）',
    `biz_id`           VARCHAR(50)    NOT NULL COMMENT '业务ID',
    `biz_topic`        VARCHAR(50)    NOT NULL COMMENT '业务消息队列主题，支付回调时使用消息队列通知业务方，需要业务方做好消费动作',
    `biz_attach`       VARCHAR(1000)  NOT NULL DEFAULT '' COMMENT '业务附加信息，通知业务方时返回，若所需附加信息过长，建议存储在业务方',
    `pay_attach`       TEXT           NOT NULL DEFAULT '' COMMENT '支付附加信息，针对不同支付渠道所需参数的差异，采用json字符串格式传参',

    PRIMARY KEY (`id`),
    UNIQUE KEY (`order_id`)
) COMMENT = '订单';

CREATE TABLE `pay_refund`
(
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id`          VARCHAR(32)    NOT NULL COMMENT '订单ID，关联pay_order',
    `refund_id`         VARCHAR(32)    NOT NULL COMMENT '退款ID，表唯一键，固定32位',
    `refund_state`      VARCHAR(10)    NOT NULL COMMENT '退款状态：PROCESSING-处理中，SUCCESS-成功，FAIL-失败',
    `refund_amount`     DECIMAL(10, 2) NOT NULL COMMENT '退款金额，不能大于订单金额',
    `refund_reason`     VARCHAR(30)    NOT NULL COMMENT '退款原因，简略描述，详细描述在业务方存储',
    `create_time`       DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time`       DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    `product_refund_id` VARCHAR(50)    NOT NULL DEFAULT '' COMMENT '产品退款ID',
    `refund_time`       DATETIME(3)    NULL COMMENT '退款时间',
    `refund_fail_desc`  VARCHAR(100)   NOT NULL DEFAULT '' COMMENT '退款失败描述',
    `refund_attach`     TEXT           NOT NULL DEFAULT '' COMMENT '退款附加信息，针对不同支付渠道所需参数的差异，采用json字符串格式传参',

    PRIMARY KEY (`id`),
    UNIQUE KEY (`refund_id`)
) COMMENT = '退款';
```