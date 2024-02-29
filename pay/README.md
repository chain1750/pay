# 支付系统

## 一、准备

### 配置

- 运行时添加参数：`nacos.address={Nacos地址};environment={环境}`
- Nacos中配置MySQL、Redis、RocketMQ和jasypt，配置文件名称与`bootstrap.properties`一致，Nacos的命名空间与`environment`一致
- 配置支付和退款通知地址，格式为：`https域名 + 对外服务接口 + {产品名称}`

> 通知地址说明：
>
> 1. 有些支付渠道要求https，统一将通知地址都设置为https
> 2. 支付系统不对外暴露，所以需要一个对外服务提供接口来转发支付系统的通知接口
>
> 举例：
>
> https://a.com/api/callback/notify/pay/{productName} ，转发到支付系统的/pay/notify/pay/{productName}接口
>
> https://a.com/api/callback/notify/refund/{productName} ，转发到支付系统的/pay/notify/refund/{productName}接口
>
> 最终目的是能够正常转发给支付系统对应的接口即可。

- 配置产品名称与实现类映射，示例：

```yaml
product:
  pay-notify-url: '支付通知地址，格式：接口 + /{}'
  refund-notify-url: '退款通知地址，格式：接口 + /{}'
  name1:
    bean-name: weChatAppPayService
    notify-return-data: '通知返回数据'
  name2:
    bean-name: weChatH5PayService
    notify-return-data: '通知返回数据'
  # ......
```

> 产品名称：系统分离了底层服务，采用策略模式来执行具体的支付方式（指微信、支付宝等），产品名称用于决定采用什么策略。
>
> 比如：项目中需要微信小程序支付、微信APP支付、支付宝小程序支付、支付宝APP支付，甚至更大的项目中存在微信小程序A需要支付，微信小程序B需要支付的情况。
> 那么可以定义：
> - 产品名称 -> 支付实现类
> - WECHAT_MP_A -> WechatJSAPIPayServiceImpl
> - WECHAT_MP_B -> WechatJSAPIPayServiceImpl
> - WECHAT_APP_A -> WechatAPPPayServiceImpl
> - ALIPAY_APP_A -> AlipayAPPPayServiceImpl

### 基础数据表

```mysql
CREATE TABLE `pay_order`
(
    `id`               BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_ip`          VARCHAR(50)    NOT NULL COMMENT '用户IP，当前下单用户所在IP',
    `user_id`          VARCHAR(50)    NOT NULL COMMENT '用户ID，当前下单用户在系统中的唯一ID',
    `order_id`         VARCHAR(32)    NOT NULL COMMENT '订单ID，表唯一键，固定32位',
    `order_state`      VARCHAR(10)    NOT NULL COMMENT '订单状态：NOT_PAY-未支付，SUCCESS-已支付，CLOSED-已关闭',
    `order_amount`     DECIMAL(10, 2) NOT NULL COMMENT '订单金额，单位元',
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
    `order_attach`     TEXT           NOT NULL DEFAULT '' COMMENT '订单附加信息，针对不同支付渠道所需参数的差异，采用json字符串格式传参',

    PRIMARY KEY (`id`),
    UNIQUE KEY (`order_id`)
) COMMENT = '订单';

CREATE TABLE `pay_refund`
(
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id`          VARCHAR(32)    NOT NULL COMMENT '订单ID，关联pay_order',
    `refund_id`         VARCHAR(32)    NOT NULL COMMENT '退款ID，表唯一键，固定32位',
    `refund_state`      VARCHAR(10)    NOT NULL COMMENT '退款状态：PROCESSING-处理中，SUCCESS-成功，FAIL-失败',
    `refund_amount`     DECIMAL(10, 2) NOT NULL COMMENT '退款金额，不能大于订单金额，单位元',
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

## 二、接口说明

### 支付接口

#### 订单预支付

> POST /pay/order/prepay

> 请求参数

| name          | type   | desc                                    |
|---------------|--------|-----------------------------------------|
| userIp        | string | 用户IP，当前下单用户所在IP                         |
| userId        | string | 用户ID，当前下单用户在系统中的唯一ID                    |
| orderAmount   | number | 订单金额，单位元，最小0.01元                        |
| description   | string | 商品描述，简略描述，详细描述在业务方存储                    |
| productName   | string | 产品名称，用于避免不同渠道上应用ID重复，命名格式：渠道_产品名称（大写字母） |
| productAppId  | string | 产品应用ID                                  |
| productOpenId | string | 产品应用用户OpenId                            |
| expireTime    | string | 过期时间                                    |
| bizName       | string | 业务名称，用于避免不同业务的业务ID重复，命名格式：模块_业务（大写字母）   |
| bizId         | string | 业务ID                                    |
| bizTopic      | string | 业务消息队列主题，支付回调时使用消息队列通知业务方，需要业务方做好消费动作   |
| bizAttach     | string | 业务附加信息，通知业务方时返回，若所需附加信息过长，建议存储在业务方      |
| orderAttach   | string | 订单附加信息，针对不同支付渠道所需参数的差异，采用json字符串格式传参    |

> 返回参数

| name    | type   | desc                                          |
|---------|--------|-----------------------------------------------|
| orderId | string | 订单ID                                          |
| prepay  | object | 预支付，由于业务方无需知晓预支付信息，且不同支付渠道之间预支付信息不同，所以采用Map返回 |

#### 关闭订单

> 业务方需要取消订单，同时需要将支付渠道上的订单一同关闭

> POST /pay/order/close

> 请求参数

| name    | type   | desc |
|---------|--------|------|
| orderId | string | 订单ID |

#### 查询订单

> 业务方自身需要轮询订单状态，通过该接口获取

> POST /pay/order/query

> 请求参数

| name    | type   | desc |
|---------|--------|------|
| orderId | string | 订单ID |

> 返回参数

| name           | type   | desc                                    |
|----------------|--------|-----------------------------------------|
| orderId        | string | 订单ID                                    |
| orderState     | string | 订单状态：NOT_PAY-未支付，SUCCESS-已支付，CLOSED-已关闭 |
| productOrderId | string | 产品订单ID                                  |
| payTime        | string | 支付时间                                    |
| bizName        | string | 业务名称，用于避免不同业务的业务ID重复，命名格式：模块_业务（大写字母）   |
| bizId          | string | 业务ID                                    |
| bizTopic       | string | 业务消息队列主题，支付回调时使用消息队列通知业务方，需要业务方做好消费动作   |
| bizAttach      | string | 业务附加信息，通知业务方时返回，若所需附加信息过长，建议存储在业务方      |

#### 退款

> POST /pay/refund

> 请求参数

| name         | type   | desc                                 |
|--------------|--------|--------------------------------------|
| orderId      | string | 订单ID，关联pay_order                     |
| refundAmount | number | 退款金额，不能大于订单金额，单位元，最小0.01元            |
| refundReason | string | 退款原因，简略描述，详细描述在业务方存储                 |
| refundAttach | string | 退款附加信息，针对不同支付渠道所需参数的差异，采用json字符串格式传参 |

> 返回参数

| name     | type   | desc |
|----------|--------|------|
| refundId | string | 退款ID |

### 通知接口

预支付返回给到前端拉起支付弹窗，用户在支付渠道上完成支付，之后支付渠道会通知到这里完成对系统订单的状态更新。

### 轮询接口

由于通知接口可能会存在支付渠道或者系统其中一个出现挂机等情况使得通知失败，
为了保证订单能正常变更状态，需要通过定时任务来主动像支付渠道查询当前订单的状态从而进行更新。

## 三、支付渠道实现

### 微信支付

#### 配置

```yaml
pay:
  channel:
    wechat:
      merchant-id: ''
      privateKey-path: ''
      serial-number: ''
      api-v3-key: ''
```

#### 实现类

- weChatApp
- weChatH5
- weChatJsApi
- weChatNative

### 支付宝支付

#### 配置

```yaml
pay:
  channel:
    alipay:
      server-url: ''
      public-key: ''
      seller-id: ''
      apps:
        appId1:
          private-key: ''
```

#### 实现类

- alipayApp
- alipayJsApi
- alipayPage
- alipayWap

### 抖音支付 TODO

### 钱包余额支付 TODO

## 四、时序图

### 下单&支付

![下单&支付](https://minio.chaincat.top/public/2024022501.jpg)

### 支付系统 & 业务服务轮询 TODO
