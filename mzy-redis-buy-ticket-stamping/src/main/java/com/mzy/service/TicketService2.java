package com.mzy.service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class TicketService2 {
	
	private static final Logger logger = LoggerFactory.getLogger(TicketService2.class);
	
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	
	@Autowired
	DataBaseService databaseService;
	
	//同步互斥锁
	Lock lock = new ReentrantLock();
	
	
	//@Cacheable() 不管你是用注解的来实现还是用redistemplate来实现都会实现 1 2 3 的步骤。
	//10000
	public Object queryTicketStock(final String ticketSeq) throws InterruptedException {
		
		//1:先从redis中读取余票信息
		String value = stringRedisTemplate.opsForValue().get(ticketSeq);
		if(value!=null) {
			System.out.println(Thread.currentThread().getName()+" 缓存中获取余票数据是："+value);
			return value;
		}
		
		// cpu 单核-- 
		lock.lock(); //这个时候2000个人并发请求进来，只有一个人进来操作。1999个人在外面等待阻塞，始终都会又去数据库去查询和重建步骤，这个时候我们要给1999个做缓存补偿
		//lock.tryLock();
		//boolean flag = lock.tryLock(3, TimeUnit.SECONDS);
		
		try {
			
			// 缓存补偿
			value = stringRedisTemplate.opsForValue().get(ticketSeq);
			if(value!=null) {
				System.out.println(Thread.currentThread().getName()+" 缓存中获取余票数据是："+value);
				return value;
			}
			
			// 2: 缓存没有从数据库中获取
			value = databaseService.queryForDatabase(ticketSeq);//0.02 0.5 3秒
			System.out.println(Thread.currentThread().getName()+" 从数据库获取："+value);
			
			//塞到redis缓存中120秒过期时间 (缓存时间)
			final String v = value;
			stringRedisTemplate.execute(new RedisCallback<Boolean>() {
				@Override
				public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
					return connection.setEx(ticketSeq.getBytes(), 120, v.getBytes());
				}
			});  
			
		} finally {
			lock.unlock();//释放锁
		}
		
		return value;
	}
}
