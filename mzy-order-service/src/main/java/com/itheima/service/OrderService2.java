package com.itheima.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itheima.pojo.Order;

@Service
public class OrderService2 {

	@Autowired
	private OrderDataBaseService orderDataBaseService;
	
	@Autowired
	private MQService mQService;
	
	
	// 创建订单
	public void createOrder(Order order) throws Exception {
		
		// 1: 订单信息--插入丁订单系统，订单数据库事务
		orderDataBaseService.saveOrder(order);

		// 2：发送数据到mq
		mQService.sendMsg(order);
		
	}

	
}
