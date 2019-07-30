/**
 * TODO木子鱼系统平台<br/>
 * com.mzy.lock<br/>
 * SellTicketTest.java<br/>
 *  创建人:mofeng <br/>
 * 时间：2019年1月14日-下午4:43:24 <br/>
 * 2019木子鱼公司-版权所有<br/>
 */
package com.mzy.lock;

/**
 * 
 * SellTicketTest<br/>
 * 创建人:mofeng<br/>
 * 时间：2019年1月14日-下午4:43:24 <br/>
 * @version 1.0.0<br/>
 * 
 */
public class SellTicketTest {

	public static void main(String[] args) {
		
		SellTicket sellTicket = new SellTicket();
		//使用Synchronized锁来解决线程安全问题
		//SellTicket_Sync sellTicket = new SellTicket_Sync();
		//使用Lock来保证线程安装 juc--Future
		//TOMCAT 小---catalina-nio-thread-01---spring-/miaosha
		//TOMCAT fly---catalina-nio-thread-02---spring-/miaosha
		//TOMCAT ---catalina-nio-thread-03---spring-/miaosha
		//TOMCAT ---catalina-nio-thread-04---spring-/miaosha
		//TOMCAT ---catalina-nio-thread-05---spring-/miaosha
		//SellTicket_Lock  sellTicket = new SellTicket_Lock();
		//SellTicket_RedisLock  sellTicket = new SellTicket_RedisLock();
		//SellTicket_ZookeeperLock  sellTicket = new SellTicket_ZookeeperLock();
		//SellTicket_RedissonLock sellTicket = new SellTicket_RedissonLock();
		//模拟三个窗口售票
		for (int i = 1; i <= 3; i++) {
			new Thread(sellTicket).start();
		}
	}
}
