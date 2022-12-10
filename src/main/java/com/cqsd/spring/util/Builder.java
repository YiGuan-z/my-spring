package com.cqsd.spring.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author caseycheng
 * @date 2022/12/6-17:10
 **/
public class Builder<T> {
	private final Supplier<T> constuctor;
	private Consumer<T> head = t -> {};
	
	private Builder(Supplier<T> constuctor) {
		this.constuctor = constuctor;
	}
	
	public static <T> Builder<T> builder(Supplier<T> constuctor) {
		return new Builder<>(constuctor);
	}
	
	public <P> Builder<T> with(BiConsumer<T, P> consumer, P p) {
		Consumer<T> c = instance -> consumer.accept(instance, p);
		this.head = this.head.andThen(c);
		return this;
	}
	
	public <P> Builder<T> with(boolean exporess, BiConsumer<T, P> consumer, P p) {
		return exporess ? with(consumer, p) : this;
	}
	
	public T build() {
		final var instance = constuctor.get();
		head.accept(instance);
		return instance;
	}
}
