package com.mzy.lock;

import java.util.Collections;

import com.mzy.util.RedisPool;

import redis.clients.jedis.Jedis;

public class RedisDistributeLock {

	
	private final Long RELEASE_SUCCESS = 1L;
	//获取锁时，睡眠等待5毫秒中
	private long SLEEP_PER = 5;
	private final String key = "lock.key"; 
	private final String value = "lock.value"; 
	private final int expireTime = 5 * 1000; 
    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
	
	private boolean tryGetDistributeLock(Jedis jedis,String key ,String value) {
		String result = jedis.set(key, value, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
	}
	
	public boolean tryLock() {
		try(Jedis jedis = RedisPool.getJedis();){
			return tryGetDistributeLock(jedis, key, value);
		}
	}
	
	public void lock() {
		try(Jedis jedis = RedisPool.getJedis();){
			 while(!tryGetDistributeLock(jedis, key, value)) {
				try {
					Thread.sleep(SLEEP_PER);
				} catch (Exception e) {
				} 
			 }
		}
	}
	
	private boolean unLock(Jedis jedis,String key ,String value) {
		String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(key), Collections.singletonList(value));

        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;

	}
	
	public void unlock() {
		try(Jedis jedis = RedisPool.getJedis();){
			unLock(jedis, key, value);
		}
	}
    
}
