package net.vansen.nexelarena.consumer;

/**
 * A functional interface that represents a quad consumer.
 * It takes four arguments and performs an operation on them.
 *
 * @param <T> the type of the first argument
 * @param <U> the type of the second argument
 * @param <V> the type of the third argument
 * @param <W> the type of the fourth argument
 */
@FunctionalInterface
public interface QuadConsumer<T, U, V, W> {

    /**
     * Performs the operation on the given arguments.
     *
     * @param t the first argument
     * @param u the second argument
     * @param v the third argument
     * @param w the fourth argument
     */
    void accept(T t, U u, V v, W w);
}
