/**
 * TODO木子鱼系统平台<br/>
 * com.mzy.lock<br/>
 * SellTicket.java<br/>
 *  创建人:mofeng <br/>
 * 时间：2019年1月14日-下午4:39:43 <br/>
 * 2019木子鱼公司-版权所有<br/>
 */
package com.mzy.lock;

import java.util.concurrent.TimeUnit;

import com.mzy.lock2.zookeeper.ZookeeperDistributeLock;

/**
 * 
 * 模拟多线程环境下资源竞争的问题
 * SellTicket<br/>
 * 创建人:mofeng<br/>
 * 时间：2019年1月14日-下午4:39:43 <br/>
 * @version 1.0.0<br/>
 * 
 */
public class SellTicket_ZookeeperLock implements Runnable{
	
		//定义锁
		private  ZookeeperDistributeLock myLock = null;
		//票数
		private int ticket = 100;
		
		@Override
		public void run() {
			while(ticket > 0 ) {
				myLock = ZookeeperDistributeLock.initZk();
				try {
					if (myLock.lock(3,TimeUnit.SECONDS)) {
						try {
							if(ticket > 0 ) {
								System.out.println(Thread.currentThread().getName()+"正在出售第："+ticket--+"张票!");
							}
							
						} finally {
							myLock.unLock();//一定要释放锁
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
}
