package com.chaincat.pay.service.impl;

import com.chaincat.pay.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 通知Service
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

    @Override
    public String handlePay(String productName, HttpServletRequest request) {
        return null;
    }

    @Override
    public String handleRefund(String productName, HttpServletRequest request) {
        return null;
    }
}
