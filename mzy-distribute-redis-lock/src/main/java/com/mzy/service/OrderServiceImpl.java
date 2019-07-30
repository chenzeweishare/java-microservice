package com.mzy.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.mzy.lock.RedisDistributeLock;

public class OrderServiceImpl {

	@Autowired
	private RedisDistributeLock redisDistributeLock;
	
	public void makeOrder(Long userId,Long itemId) {
		redisDistributeLock.lock();
		//查询商品的库存
//		long storenum = 0;
//		if(storenum<=0) {
//			redisDistributeLock.unlock();
//			return ServerResponse.createError("库存不足");
//		}
		//如果库存充足，就去下单，如果不足提示用户库存不足
		//下订单(RabbitMQ)
		
		redisDistributeLock.unlock();
	}
}
