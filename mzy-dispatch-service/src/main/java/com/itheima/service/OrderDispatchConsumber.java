package com.itheima.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OrderDispatchConsumber {

	
	private final Logger log = LoggerFactory.getLogger(OrderDispatchConsumber.class);
	
	
	@Autowired
	private DispatchService dispatchService;
	
	
	@RabbitListener(queues = "orderDispatchQueue")
	public void messageConsumber(String message,com.rabbitmq.client.Channel channel,@Header(AmqpHeaders.DELIVERY_TAG)long tag) throws Exception {
		try {

			// mq的数据转成json
			ObjectMapper objectMapper = new ObjectMapper();
			Map<?, ?> map = objectMapper.readValue(message, Map.class);
			
			log.warn("收到MQ的消息是：" + map.toString());
			
			
			Thread.sleep(5000L);
			
			// 执行业务操作，同一个数据不能处理两次，根据具体的业务去重，保证幂等性。
			String orderId = String.valueOf(map.get("orderId"));
			// 在这里分配一个外卖小哥
			dispatchService.dispatch(orderId);
			
			// ACK-- 手动确定消息，告诉MQ我已经收到消息了
			channel.basicAck(tag, false);
		} catch (Exception e) {
			
			// 异常情况，根据需求要去重发或者丢弃
			// 重发一定次数后，丢弃，日志警告或者短信通知等相关处理。
			channel.basicNack(tag,false,false); // requeue参数如果为false,代表不用重复，true代表需要重发。
			// 系统关键数据，永远是有人工干预。
			//e.printStackTrace();
		}
		
		
		// 如果不给回复，就等这个consumber断开后，mq-server会继续推送
		
	}
	
	
	
	
	
	
	
	
	
}
