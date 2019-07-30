package com.mzy;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mzy.lock.SellTicket_Redis;
import com.mzy.lock.SellTicket_Redis2;
import com.mzy.util.RedisPool;

import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MofengRedisLockApplicationTests {
	
	public static ExecutorService executor = Executors.newFixedThreadPool(15) ;

	@Test
    public void testRedisLock() throws InterruptedException {
        int thread_Num = 15;
        CountDownLatch countDownLatch = new CountDownLatch(thread_Num);
        String lockName = Thread.currentThread().getName();
        for (int i = 0; i < thread_Num; i++) {
            executor.execute(new Runnable() {
                Jedis redis = RedisPool.getJedis();
                @Override
                public void run() {
                    try {
                        String lockValue = null;
                        try {
                        	System.out.println(Thread.currentThread()+"==="+lockName+"获取到了锁....");
                            lockValue = SellTicket_Redis.acquire_lock(redis, lockName, 5);
                        } finally {
                            if (lockValue != null) {
                            	SellTicket_Redis.release_lock(redis, lockName, lockValue);
                            	System.out.println(Thread.currentThread()+"==="+lockName+"释放到了锁....");
                            }
                        }

                    } finally {
                        countDownLatch.countDown();
                        RedisPool.returnResource(redis);
                    }
                }
            });

        }
        countDownLatch.await();
        executor.shutdown();
    }
	
	
	@Test
    public void testLuaRedisLock() throws InterruptedException {
        int thread_Num = 15;
        CountDownLatch countDownLatch = new CountDownLatch(thread_Num);
        String lockName = Thread.currentThread().getName();
        for (int i = 0; i < thread_Num; i++) {
            executor.execute(new Runnable() {
                Jedis redis = RedisPool.getJedis();
                @Override
                public void run() {
                    try {
                        boolean lockValue = false;
                        String value = UUID.randomUUID().toString();
                        try {
                        	System.out.println(Thread.currentThread()+"==="+lockName+"获取到了锁....");
                            lockValue = SellTicket_Redis2.acquire_loca_lua(redis,lockName,value,5,3000);
                        } finally {
                            if (lockValue) {
                            	SellTicket_Redis2.del_lock(redis,lockName,value);
                            	System.out.println(Thread.currentThread()+"==="+lockName+"释放到了锁....");
                            }
                        }

                    } finally {
                        countDownLatch.countDown();
                        RedisPool.returnResource(redis);
                    }
                }
            });

        }
        countDownLatch.await();
        executor.shutdown();
    }
	
	@Test
    public void testLuaRedisLockIP() throws InterruptedException {
        int thread_Num = 15;
        CountDownLatch countDownLatch = new CountDownLatch(thread_Num);
        for (int i = 0; i < thread_Num; i++) {
            executor.execute(new Runnable() {
                Jedis redis = RedisPool.getJedis();
                @Override
                public void run() {
                    try {
                        Object lockValue = SellTicket_Redis2.ip_limit(redis,"127.0.0.1",10,5);
                        System.out.println(lockValue);
                    } finally {
                        countDownLatch.countDown();
                        RedisPool.returnResource(redis);
                    }
                }
            });

        }
        countDownLatch.await();
        executor.shutdown();
    }

}

