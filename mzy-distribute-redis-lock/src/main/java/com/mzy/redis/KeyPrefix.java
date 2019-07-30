package com.mzy.redis;

public interface KeyPrefix {

	public int expireSeconds();

	public String getPrefix();

}
