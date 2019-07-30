package com.mzy.web;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mzy.spring.RedisLockImpl;

@RestController
@RequestMapping("/distribution/redis")
public class RedisLockController {

	private static final String LOCK_NO = "redis_distribution_lock_no_";

	private static int num = 0;

	private ExecutorService service;

	@Autowired
	private StringRedisTemplate redisTemplate;

	/**
	 * 	模拟1000个线程同时执行业务，修改资源
	 * 	使用线程池定义了20个线程
	 *  todo: http://localhost:8080//distribution/redis/lock1
	 */
	@GetMapping("lock1")
	public void testRedisDistributionLock1() {
		num = 0;
		service = Executors.newFixedThreadPool(20);
		for (int i = 0; i < 1000; i++) {
			service.execute(new Runnable() {
				@Override
				public void run() {
					task(Thread.currentThread().getName());
				}
			});
		}
	}

	@GetMapping("/{key}")
	public String getValue(@PathVariable("key") String key) {
		Serializable result = redisTemplate.opsForValue().get(key);
		return result.toString();
	}

	private void task(String name) {
		// 创建一个redis分布式锁
		 RedisLockImpl redisLock = new RedisLockImpl(redisTemplate); 
		// 加锁时间
		Long lockTime;
		if ((lockTime = redisLock.lock((LOCK_NO + 1) + "", name)) != null) {
			// 开始执行任务
			System.out.println(name + "任务执行中" + (num++));
			// 任务执行完毕 关闭锁
			redisLock.unlock((LOCK_NO + 1) + "", lockTime, name);
		}
	}
}