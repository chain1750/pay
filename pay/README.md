# 支付系统

## 一、准备

### 配置

- 运行时添加参数：`nacos.address={Nacos地址};environment={环境}`
- Nacos中配置MySQL、Redis、RocketMQ和jasypt，配置文件名称与`bootstrap.properties`一致，Nacos的命名空间与`environment`一致
- 配置支付和退款通知地址，格式为：`https域名 + 对外服务接口 + {产品名称}`

> 通知地址说明：
>
> 1. 有些支付第三方要求https，统一将通知地址都设置为https
> 2. 支付系统不对外暴露，所以需要一个对外服务提供接口来转发支付系统的通知接口
>
> 举例：
>
> https://a.com/api/callback/notify/pay/{payTbName} ，转发到支付系统的/pay/notify/pay/{payTbName}接口
>
> https://a.com/api/callback/notify/refund/{payTbName} ，转发到支付系统的/pay/notify/refund/{payTbName}接口
>
> 最终目的是能够正常转发给支付系统对应的接口即可。

- 配置支付第三方名称与实现类映射，示例：

```yaml
pay:
  tp:
    pay-notify-url: '支付通知地址，格式：接口 + /{}'
    refund-notify-url: '退款通知地址，格式：接口 + /{}'
    payTbName1:
      bean-name: weChatApp
      notify-return-data: '通知返回数据'
    payTbName2:
      bean-name: weChatH5
      notify-return-data: '通知返回数据'
    # ......
```

> 支付第三方名称：系统分离了底层服务，采用策略模式来执行具体的支付方式（指微信、支付宝等），支付第三方名称用于决定采用什么策略。
>
> 比如：项目中需要微信小程序支付、微信APP支付、支付宝小程序支付、支付宝APP支付，甚至更大的项目中存在微信小程序A需要支付，微信小程序B需要支付的情况。
> 那么可以定义：
> - 支付第三方名称 -> 支付实现类
> - WECHAT_MP_A -> WechatJSAPIPayServiceImpl
> - WECHAT_MP_B -> WechatJSAPIPayServiceImpl
> - WECHAT_APP_A -> WechatAPPPayServiceImpl
> - ALIPAY_APP_A -> AlipayAPPPayServiceImpl

### 基础数据表

```mysql
CREATE TABLE `pay_order`
(
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_ip`         VARCHAR(50)    NOT NULL COMMENT '用户IP',
    `user_id`         VARCHAR(50)    NOT NULL COMMENT '用户ID',
    `order_id`        VARCHAR(32)    NOT NULL COMMENT '订单ID，表唯一键',
    `order_state`     VARCHAR(10)    NOT NULL COMMENT '订单状态：NOT_PAY-未支付，SUCCESS-已支付，CLOSED-已关闭',
    `order_amount`    DECIMAL(10, 2) NOT NULL COMMENT '订单金额，单位元',
    `description`     VARCHAR(30)    NOT NULL COMMENT '商品描述，简略描述，详细描述在业务方存储',
    `pay_tp_name`     VARCHAR(30)    NOT NULL COMMENT '支付第三方名称，定义业务方所使用的支付方式与支付系统实现类映射',
    `pay_tp_app_id`   VARCHAR(50)    NOT NULL COMMENT '支付第三方应用ID',
    `pay_tp_order_id` VARCHAR(50)    NOT NULL DEFAULT '' COMMENT '支付第三方订单ID',
    `pay_tp_open_id`  VARCHAR(50)    NOT NULL COMMENT '支付第三方用户OpenID',
    `create_time`     DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time`     DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    `pay_time`        DATETIME(3)    NULL COMMENT '支付时间',
    `expire_time`     DATETIME(3)    NOT NULL COMMENT '过期时间',
    `biz_name`        VARCHAR(30)    NOT NULL COMMENT '业务名称，用于避免不同业务的业务ID重复',
    `biz_id`          VARCHAR(50)    NOT NULL COMMENT '业务ID',
    `biz_topic`       VARCHAR(50)    NOT NULL COMMENT '业务消息队列主题，支付回调时使用消息队列通知业务方，需要业务方做好消费动作',
    `biz_attach`      VARCHAR(1000)  NOT NULL DEFAULT '' COMMENT '业务附加信息，通知业务方时返回，若所需附加信息过长，建议存储在业务方',

    PRIMARY KEY (`id`),
    UNIQUE KEY (`order_id`)
) COMMENT = '支付订单';

CREATE TABLE `pay_refund`
(
    `id`               BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id`         VARCHAR(32)    NOT NULL COMMENT '订单ID，关联pay_order',
    `refund_id`        VARCHAR(32)    NOT NULL COMMENT '退款ID，表唯一键',
    `refund_state`     VARCHAR(10)    NOT NULL COMMENT '退款状态：PROCESSING-处理中，SUCCESS-成功，FAIL-失败',
    `refund_amount`    DECIMAL(10, 2) NOT NULL COMMENT '退款金额，不能大于订单金额，单位元',
    `refund_reason`    VARCHAR(30)    NOT NULL COMMENT '退款原因，简略描述，详细描述在业务方存储',
    `create_time`      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time`      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    `pay_tp_refund_id` VARCHAR(50)    NOT NULL DEFAULT '' COMMENT '产品退款ID',
    `refund_time`      DATETIME(3)    NULL COMMENT '退款时间',
    `refund_fail_desc` VARCHAR(100)   NOT NULL DEFAULT '' COMMENT '退款失败描述',

    PRIMARY KEY (`id`),
    UNIQUE KEY (`refund_id`)
) COMMENT = '支付退款';
```

## 二、接口说明

### 支付接口

#### 预支付

> POST /pay/prepay

> 请求参数

| name        | type   | desc                                  |
|-------------|--------|---------------------------------------|
| userIp      | string | 用户IP                                  |
| userId      | string | 用户ID                                  |
| orderAmount | number | 订单金额，单位元，最小0.01元                      |
| description | string | 商品描述，简略描述，详细描述在业务方存储                  |
| payTpName   | string | 支付第三方名称，定义业务方所使用的支付方式与支付系统实现类映射       |
| payTpAppId  | string | 支付第三方应用ID                             |
| payTpOpenId | string | 支付第三方用户OpenID                         |
| expireTime  | string | 过期时间                                  |
| bizName     | string | 业务名称，用于避免不同业务的业务ID重复                  |
| bizId       | string | 业务ID                                  |
| bizTopic    | string | 业务消息队列主题，支付回调时使用消息队列通知业务方，需要业务方做好消费动作 |
| bizAttach   | string | 业务附加信息，通知业务方时返回，若所需附加信息过长，建议存储在业务方    |

> 返回参数

| name    | type   | desc                                                     |
|---------|--------|----------------------------------------------------------|
| orderId | string | 订单ID                                                     |
| prepay  | object | 预支付信息，由于业务方无需知晓预支付信息，且不同支付第三方之间预支付信息不同，所以采用json string返回 |

#### 关闭支付

> 业务方需要取消支付，同时需要将支付第三方上的支付一同关闭

> POST /pay/closePay

> 请求参数

| name    | type   | desc |
|---------|--------|------|
| orderId | string | 订单ID |

#### 查询支付

> 业务方自身需要轮询支付状态，通过该接口获取信息

> POST /pay/queryPay

> 请求参数

| name    | type   | desc |
|---------|--------|------|
| orderId | string | 订单ID |

> 返回参数

| name         | type   | desc                                    |
|--------------|--------|-----------------------------------------|
| orderId      | string | 订单ID                                    |
| orderState   | string | 订单状态：NOT_PAY-未支付，SUCCESS-已支付，CLOSED-已关闭 |
| payTpOrderId | string | 支付第三方订单ID                               |
| payTime      | string | 支付时间                                    |
| bizName      | string | 业务名称，用于避免不同业务的业务ID重复                    |
| bizId        | string | 业务ID                                    |
| bizTopic     | string | 业务消息队列主题，支付回调时使用消息队列通知业务方，需要业务方做好消费动作   |
| bizAttach    | string | 业务附加信息，通知业务方时返回，若所需附加信息过长，建议存储在业务方      |

#### 退款

> POST /pay/refund

> 请求参数

| name         | type   | desc                      |
|--------------|--------|---------------------------|
| orderId      | string | 订单ID，关联pay_order          |
| refundAmount | number | 退款金额，不能大于订单金额，单位元，最小0.01元 |
| refundReason | string | 退款原因，简略描述，详细描述在业务方存储      |

> 返回参数

| name     | type   | desc |
|----------|--------|------|
| refundId | string | 退款ID |

### 通知接口

预支付返回给到前端拉起支付弹窗，用户在支付第三方上完成支付，之后支付第三方会通知到这里完成对支付订单的状态更新。

### 轮询接口

由于通知接口可能会存在支付第三方或者系统其中一个出现挂机等情况使得通知失败，
为了保证支付订单能正常变更状态，需要通过定时任务来主动像支付第三方查询当前支付订单的状态从而进行更新。

## 三、支付第三方实现

### 微信支付

#### 配置

```yaml
pay:
  tp:
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
  tp:
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
