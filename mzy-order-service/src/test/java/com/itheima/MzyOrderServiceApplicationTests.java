package com.itheima;

import com.itheima.pojo.Order;
import com.itheima.service.OrderService;
import com.itheima.service.OrderService2;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MzyOrderServiceApplicationTests {
	
	
	
	@Before
	public void before() {
		System.out.println("开始进行测试*********************************");
	}
	
	
	@After
	public void after() {
		System.out.println("结束*********************************");
	}
	
	
	@Autowired
	public OrderService orderService;

	@Autowired
    private OrderService2 orderService2;
	

	@Test
	public void orderCreated() throws Exception {
		//订单生成
		String orderId = "1000001";
		Order orderInfo = new Order();
		orderInfo.setOrderId(orderId);
		orderInfo.setUserId(1);
		orderInfo.setOrderContent("买了一个方便面2");
        orderService2.createOrder(orderInfo);
		
		System.out.println("订单创建成功.......");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
