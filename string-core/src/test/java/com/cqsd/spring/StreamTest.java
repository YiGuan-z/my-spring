package com.cqsd.spring;

import org.junit.jupiter.api.Test;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author caseycheng
 * @date 2023/1/5-11:02
 **/
class StreamTest {
    public static final Deque<String> TRACE = new ConcurrentLinkedDeque<>();

    static class IntGenerator implements Supplier<Integer> {
        private final AtomicInteger current = new AtomicInteger();

        /**
         * Gets a result.
         *
         * @return a result
         */
        @Override
        public Integer get() {
            TRACE.add("当前值为" + current.get() + ":" + Thread.currentThread().getName());
            return current.getAndIncrement();
        }
    }

    @Test
    void testGenerate() {

        final var integers = Stream.generate(new IntGenerator())
                .limit(10)
                .parallel()
                .toList();
        System.out.println(integers);
        System.out.println();
        int i = 0;
        for (String s : TRACE) {
            System.out.println(i++ + "号操作\t" + s);
        }
    }

    @Test
    void testExec() {
        final var service = Executors.newSingleThreadExecutor();
        //提交一个任务并返回一个任务回执
        final Future<?> submit = service.submit(() -> {
        });
        service.close();
    }

    static class Machina {
        public enum State {
            START, ONE, TOW, THREE, END;

            State step() {
                if (equals(END)) return END;
                return values()[ordinal() + 1];
            }
        }

        private State state = State.START;
        private final int id;

        public Machina(int id) {
            this.id = id;
        }

        public static Machina work(Machina m) {
            if (!m.state.equals(State.END)) {
                m.state = m.state.step();
            }
            System.out.println(m);
            return m;
        }

        public static boolean isEnd(Machina m) {
            return m.state.equals(State.END);
        }

        @Override
        public String toString() {
            return "Machina" + id + ':' + (state.equals(State.END) ? "complete" : state);
        }
    }

    @Test
    void testMachina() {
        final CompletableFuture<Machina> completableFuture = CompletableFuture.completedFuture(new Machina(0));
        //start
        final CompletableFuture<Machina>[] c = new CompletableFuture[]{completableFuture.thenApply(Machina::work)};
        IntStream.range(0, 3).forEach(value -> {
            c[0] = c[0].thenApply(Machina::work);
        });
    }

}
