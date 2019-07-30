package com.mzy;

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mzy.service.TicketService;
import com.mzy.service.TicketService2;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MzyRedisBuyTicketStampingApplicationTests {

	long timed = 0L;
	
	@Before
	public void starter() {
		System.out.println("==========测试开始===========");
		timed = System.currentTimeMillis();
	}
	
	@After
	public void end() {
		System.out.println("==========测试结束===========消耗时长是："+(System.currentTimeMillis() - timed)+"ms");
	}
	
	
	
	@Autowired
	TicketService ticketService;
	
	//车次
	private static final String TICKET_SEQ = "G296";
	//模拟请求的数量
	private static final int THREAD_NUM = 1000;
	
	//倒计数器：juc包常用工具类
	private CountDownLatch countDownLatch= new CountDownLatch(THREAD_NUM);
	
	@Test
	public void benchmark() {
		//创建，并不是马上发起请求，模拟并发请求
		Thread[] threads = new Thread[THREAD_NUM];
		for (int i = 0; i < THREAD_NUM; i++) {
			
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					//代码在这里等待，阻塞，等待countDownLatch变成0，代表所有线程都start,在运行后续的代码
					try {
						countDownLatch.await();
						//http请求，实际上就是多线程调用这个方法
						ticketService.queryTicketStock(TICKET_SEQ);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			
			threads[i] = thread;
			thread.start();
			//启动后，倒计时计数器减一，代表有一个线程准备就绪了。
			countDownLatch.countDown();//0
		}
		
		//ticketService.queryTicketStock(TICKET_SEQ);
	}

}

