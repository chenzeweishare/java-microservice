package com.mzy.service;

import java.util.concurrent.ConcurrentHashMap;
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
public class TicketService3 {
	
	private static final Logger logger = LoggerFactory.getLogger(TicketService3.class);
	
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	//@Autowired
	//private StringRedisTemplate bakRedisTemplate;
	
	
	@Autowired
	DataBaseService databaseService;
	
	//同步互斥锁
	Lock lock = new ReentrantLock();
	
	//增加并发Map保存车次和每个车次对应的锁的状态
	ConcurrentHashMap<String, String> mapLock = new ConcurrentHashMap<>();
	
	
	
	//1：数据库就是一个分布式锁
	
	
	//@Cacheable() 不管你是用注解的来实现还是用redistemplate来实现都会实现 1 2 3 的步骤。
	public Object queryTicketStock(final String ticketSeq) {
		
		//1:先从redis中读取余票信息
		String value = stringRedisTemplate.opsForValue().get(ticketSeq);
		if(value!=null) {
			System.out.println(Thread.currentThread().getName()+" 缓存中获取余票数据是："+value);
			return value;
		}
		
		//获取锁的状态
		boolean lock = false;
		try {
			//类型与redis的 setnx操作，互斥锁,可以达到分布式锁
			lock = mapLock.putIfAbsent(ticketSeq, "oooxxxx") == null;
			if(lock) { //2000个线程，一个抢到锁，如果有多个车次在抢锁的话，会把每个锁锁到对应的每个车次上，这样锁的粒度就减小了，大大的提升并发量。
				
				//缓存补偿
				value = stringRedisTemplate.opsForValue().get(ticketSeq);
				if(value!=null) {
					System.out.println(Thread.currentThread().getName()+" 缓存中获取余票数据是："+value);
					return value;
				}
				
				//缓存重建
				//2:缓存没有从数据库中获取
				value = databaseService.queryForDatabase(ticketSeq);
				System.out.println(Thread.currentThread().getName()+" 从数据库获取："+value);
				
				//塞到redis缓存中120秒过期时间 (缓存时间)
				final String v = value;
				stringRedisTemplate.execute(new RedisCallback<Boolean>() {
					@Override
					public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
						return connection.setEx(ticketSeq.getBytes(), 120, v.getBytes());
					}
				});  
				
				//双写，写入备份缓存中。
				//bakRedisTemplate.opsForValue().set(ticketSeq,value);
			}else {
				//这种方案：交缓存降级。比如双十一双十二，比如说付款不会提示你失败，恨不得你马上付钱，就算是在高峰的情况可能很多付了款，也会给予你一个良好的提示，就是降级处理了。
				//就好比在朋友带早餐：肉包子>菜包子>馒头>.............这样就不断的降低预期的目标。
				//没有拿到锁怎么处理呢？
				//1：重试获取锁,但是要控制重试的次数
				//2：直接返回空或者指定的值。
				//3：备用缓存
//				value = bakRedisTemplate.opsForValue().get(ticketSeq);
//				if(value!=null) {
//					System.out.println(Thread.currentThread().getName()+" 缓存中获取余票数据是："+value);
//					return value;
//				}else {
					value = "0";
					System.out.println(Thread.currentThread().getName()+" 缓存降级 返回固定值是："+value);
//				}
			}
		} finally {
			mapLock.remove(ticketSeq);
		}
			
		return value;
	}
}
