package com.cqsd.ps;

import com.cqsd.ps.interfaces.Subscriber;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * @author caseycheng
 * @date 2022/12/29-11:15
 **/
public class Publisher implements com.cqsd.ps.interfaces.Publisher {
    private final Lock m = new ReentrantLock();
    private final Map<Subscriber, Predicate<?>> subscribers = new ConcurrentHashMap<>();

    private int buffer = 5;
    private long timeOut = TimeUnit.MILLISECONDS.toMillis(1);

    @Override
    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    @Override
    public int getBuffer() {
        return this.buffer;
    }

    @Override
    public void setTimeOut(Long time, TimeUnit unit) {
        this.timeOut = unit.convert(time, unit);
    }

    @Override
    public long getTimeOut() {
        return this.timeOut;
    }

    private Publisher(int buffer, long timeOut) {
        this.buffer = buffer;
        this.timeOut = timeOut;
    }

    public static com.cqsd.ps.interfaces.Publisher newPublisher(int buffer, long timeOut) {
        return new Publisher(buffer, timeOut);
    }

    public Subscriber subscribeTopic(Predicate<?> topic) {
        final var subscriberQuque = new Subscriber(buffer);
        m.lock();
        subscribers.put(subscriberQuque, topic);
        m.unlock();
        return subscriberQuque;
    }

    public void evict(Subscriber subscriber) {
        m.lock();
        subscribers.remove(subscriber);
        subscriber.getValue().clear();
        m.unlock();
    }
}
