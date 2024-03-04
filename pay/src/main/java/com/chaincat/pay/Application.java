package com.chaincat.pay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动类
 *
 * @author chenhaizhuang
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
// 通过注入对应
@ComponentScan(value = {"com.chaincat.pay", "com.chaincat.product.wechat", "com.chaincat.product.alipay"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        log.info("支付系统启动==========");
    }
}
