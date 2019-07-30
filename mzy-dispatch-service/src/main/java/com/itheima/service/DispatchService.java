package com.itheima.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class DispatchService {

	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	
	public void dispatch(String orderId) throws Exception {
		// 定义保存sql
		String sqlString = "insert into table_dispatch(order_id,dispatch_seq,dispatch_status,dispatch_connection)values(?,?,?,?)";
		
		// 添加运动记录
		int count = jdbcTemplate.update(sqlString,orderId,UUID.randomUUID().toString(),0,"木子鱼");
		
		if(count!=1) {
			throw new Exception("订单创建失败，原因[数据库操作失败]");
		}
		
	}

	
}
