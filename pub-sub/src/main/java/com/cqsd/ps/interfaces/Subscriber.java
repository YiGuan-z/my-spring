package com.cqsd.ps.interfaces;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 订阅者
 */
public class Subscriber {
	protected Queue<Object> value;
	
	public Subscriber(int buffer) {
		this.value = new ArrayDeque<>(buffer);
	}
	
	public Queue<Object> getValue() {
		return value;
	}
}
