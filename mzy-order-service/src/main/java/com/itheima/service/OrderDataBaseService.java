package com.itheima.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itheima.pojo.Order;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrderDataBaseService {

	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	
	/**
	 * 保存订单记录
	 * @param order
	 */
	
	public void saveOrder(Order order) throws Exception{
		// 定义保存sql
		String sqlString = "insert into table_order(order_id,user_id,order_content)values(?,?,?)";
		
		// 1：添加运动记录
		int count = jdbcTemplate.update(sqlString,order.getOrderId(),order.getUserId(),order.getOrderContent());
		
		if(count!=1) {
			throw new Exception("订单创建失败，原因[数据库操作失败]");
		}
		
		// 2：保存本地消息
		saveLocalMessage(order);
	}
	
	
	/**
	 * 保存信息到本地
	 * @param order
	 */
	public  void saveLocalMessage(Order order) throws Exception{
		// 定义保存sql
		String sqlString = "insert into table_dispatch_message(unique_id,msg_content,msg_status)values(?,?,?)";
		
		// 添加运动记录
		int count = jdbcTemplate.update(sqlString,order.getOrderId(),order.toString(),0);
		
		if(count!=1) {
			throw new Exception("出现异常，原因[数据库操作失败]");
		}
	}
}
