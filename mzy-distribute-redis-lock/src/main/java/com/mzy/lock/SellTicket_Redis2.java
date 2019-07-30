package com.mzy.lock;

import redis.clients.jedis.Jedis;

public class SellTicket_Redis2 {
	/**
	 * set nx px 进行设置锁 nx当不存在时才设置 xx为存在时才设置，px为毫秒 ex为秒 这个有什么坏处呢？：
	 * 1、删除锁时如果这个锁已过期了页，而过期期间锁已被其它线程拿到，之后当前线程处理完了，del锁时已经删除的不是自己的锁了。
	 * 如下：A客户端拿到对象锁，但在因为一些原因被阻塞导致无法及时释放锁。 因为过期时间已到，Redis中的锁对象被删除。 B客户端请求获取锁成功。
	 * A客户端此时阻塞操作完成，删除key释放锁。 C客户端请求获取锁成功。 这时B、C都拿到了锁，因此分布式锁失效。
	 * 2、要避免1中的情况发生，就要保证key的值是唯一的，且每一个拿到该key锁的值不一样，只有拿到锁的客户端才能进行删除。
	 * 基于这个原因，普通的del命令是不能满足要求的，我们需要一个能判断客户端传过来的value和锁对象的value是否一样的命令。Redis并没有这样的原子命令，这时可以通过Lua脚本来完成：
	 * 
	 * @param redis
	 * @param lockName
	 * @param tryNum
	 * @param lock_timeout
	 * @return
	 */
	public static boolean acquire_loca_nxpx(Jedis redis, String lockName, String value, int tryNum, int lock_timeout) {
		for (int i = 0; i < tryNum; i++) {
			String valuel = redis.set(lockName, value, "NX", "PX", lock_timeout);
			if ("OK".equals(valuel)) {
				return true;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
     * lua的一个获取锁的方法 效果与相同当然弊端也一样;
     * @see #acquire_loca_nxpx(Jedis, String, String, int, int)
     * @param redis
     * @param lockName
     * @param value
     * @param tryNum
     * @param lock_timeout
     * @return
     */
    public static boolean acquire_loca_lua(Jedis redis, String lockName, String value, int tryNum, int lock_timeout) {
        String script = "local key = KEYS[1] \n" + 
                " local value = ARGV[1] \n" + 
                " local outTime = ARGV[2] \n" + 
                " local num = redis.call(\"setnx\",key,value)" + 
                " if num == 1 then \n" +  
                "  redis.call(\"expire\",key,outTime) \n"+ 
                " end \n" + 
                " return num ";
        for (int i = 0; i < tryNum; i++) {
            Long num = (Long) redis.eval(script, 1, lockName,value,lock_timeout+"");
            System.err.println("acquire_loca_lua:"+num);
            if(num.intValue()==1) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
	/**
	 * 释放锁
	 * 
	 * @param redis
	 * @param lockName
	 * @param value
	 * @return
	 */
	public static long del_lock(Jedis redis, String lockName, String value) {
		String script = "local key =KEYS[1]; local value = ARGV[1] \n" + "  if redis.call(\"get\",key) == value then \n"
				+ "   return redis.call(\"del\",key)\n" + " else \n" + "   return 0 \n" + " end";
		Object result = redis.eval(script, 1, lockName, value);
		return (long) result;
	}
	
	 /**
     * @param redis
     * @param ip 限制的IP
     * @param limit_time 在某一个时间段（秒） 如：10 秒限制3次  ip_limit(xxx,"127.0.0.1",10,3)
     * @param limit_count 在这个时间内限制访问多少次
     * @return
     */
   public static Object ip_limit(Jedis redis,String ip,int limit_time,int limit_count) {
        String script = "local times = redis.call('incr',KEYS[1]) \n"+ 
                " if times == 1 then \n"
                + "redis.call('expire',KEYS[1],ARGV[1]) \n"
                + "end \n"
                + "if times> tonumber(ARGV[2]) then \n"
                + " return 0 \n"
                + "end \n"
                + "return 1";
        Object result = redis.eval(script, 1, ip,limit_time+"",limit_count+"");
        return  result;
    }

}
