package com.mzy;

import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mzy.service.TicketService;

@SpringBootApplication
public class MzyRedisBuyTicketStampingApplication  implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(MzyRedisBuyTicketStampingApplication.class, args);
	}
	
	
	@Autowired
	TicketService ticketService;
	//@Autowired
	//TicketService2 ticketService;
	//@Autowired
	//TicketService3 ticketService;
	//车次
	private static final String TICKET_SEQ = "G296";
	//模拟请求的数量
	private static final int THREAD_NUM = 1000;
	//倒计数器：juc包常用工具类
	private CountDownLatch countDownLatch= new CountDownLatch(THREAD_NUM);
	
	public void benchmark() {
		//创建，并不是马上发起请求，模拟并发请求
		Thread[] threads = new Thread[THREAD_NUM];
		for (int i = 0; i < THREAD_NUM; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					//代码在这里等待，阻塞，等待countDownLatch变成0，代表所有线程都start,在运行后续的代码
					try {
						countDownLatch.await();//1000
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
			countDownLatch.countDown();
		}
	}
	
	
	@Override
	public void run(String... args) throws Exception {
		benchmark();
	}


}

