package com.itheima.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.pojo.Order;

@Service
public class MQService {
	
	private final Logger log = LoggerFactory.getLogger(MQService.class);
	
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private RabbitTemplate rabbitTemplate;


	// 回执函数
	@PostConstruct
	public void callbacksetup() {
		//消息发送完毕后，则会回掉此方法，ack代表是否发送成功
		rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
			@Override
			public void confirm(CorrelationData correlationData, boolean ack, String cause) {
				// ack为true，代表MQ已经准确的收到了消息
				if(!ack) {
					return ;
				}
				
				try {
					// 如果正确的收到消息以后，修改本地消息的状态为：“已发送”，删除，修改状态
					String sql = "update table_dispatch_message set msg_status = 1 where unique_id = ?";
					int count = jdbcTemplate.update(sql,correlationData.getId());

					if(count!=1) {
						log.warn("警告：本地消息的状态修改失败!");
					}

					System.out.println("收到消息的回执......................");

				} catch (Exception e) {
					log.warn("警告：本地消息的状态修改出现异常!");
					e.printStackTrace();
				}
			}
		});
	}


	// 发送MQ消息，修改本地消息表的状态
	public void sendMsg(Order order) throws Exception {
		// 发送MQ消息，
		// CorrelationData 收到消息回执后。会附带上这个参数
        System.out.println("CorrelationData 收到消息回执后。会附带上这个参数");
		ObjectMapper objectMapper = new ObjectMapper();
		rabbitTemplate.convertAndSend("createOrderExchange", "",objectMapper.writeValueAsString(order),
				new CorrelationData(order.getOrderId()+""));
	}

}