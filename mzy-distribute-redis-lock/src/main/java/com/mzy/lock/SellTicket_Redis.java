package com.mzy.lock;

import redis.clients.jedis.Jedis;

public class SellTicket_Redis {

	/**
	 * 获取锁(简单)
	 * 
	 * @param lockName 锁名
	 * @param tryNum   重试次数
	 * @return
	 * @throws InterruptedException
	 * 
	 * A key="lock.lock" uuid
	 * B key="lock.lock" uuid
	 * 
	 * 	 */
	public synchronized static String acquire_lock(Jedis redis, String lockName, int tryNum) {
		String uuid = System.currentTimeMillis() + "";
		for (int i = 0; i < tryNum; i++) {
			Long n = redis.setnx("lock:" + lockName, uuid);//0
			//1
			if (n == 1) {
				return uuid;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 释放锁
	 * 
	 * @param lockName  锁名
	 * @param lockValue 锁内容
	 */
	public static void release_lock(Jedis redis, String lockName, String lockValue) {

		String lockname = "lock:" + lockName;
		boolean chek = lockValue.equals(redis.get(lockname));
		if (chek) {
			redis.del(lockname);
		}
	}

    
}
