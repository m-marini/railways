package org.mmarini;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Tuple2<T1, T2> {

    /**
     * Returns the pair of value
     *
     * @param v1   first value
     * @param v2   second value
     * @param <T1> first value type
     * @param <T2> second value type
     */
    public static <T1, T2> Tuple2<T1, T2> of(T1 v1, T2 v2) {
        return new Tuple2<>(v1, v2);
    }

    /**
     * Returns the entries stream of a map
     *
     * @param map the map
     * @param <K> the key type
     * @param <V> the value type
     */
    public static <K, V> Stream<Tuple2<K, V>> stream(Map<K, V> map) {
        return map.entrySet().stream().map(entry -> new Tuple2<>(entry.getKey(), entry.getValue()));
    }

    /**
     * Returns the function swapping the value of tuple2
     *
     * @param <T1> the type of first parameter
     * @param <T2> the type of second parameter
     */
    public static <T1, T2> Function<Tuple2<T1, T2>, Tuple2<T2, T1>> swap() {
        return t -> Tuple2.of(t._2, t._1);
    }

    /**
     * Returns the collector to Map
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    public static <K, V> Collector<Tuple2<K, V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Tuple2<K, V>::getV1, Tuple2<K, V>::getV2);
    }

    public final T1 _1;
    public final T2 _2;

    /**
     * Creates a Tuple2
     *
     * @param v1 first value
     * @param v2 second value
     */
    public Tuple2(T1 v1, T2 v2) {
        this._1 = requireNonNull(v1);
        this._2 = requireNonNull(v2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(_1, tuple2._1) && Objects.equals(_2, tuple2._2);
    }

    /**
     * Returns first value
     */
    public T1 getV1() {
        return _1;
    }

    /**
     * Returns a tuple with changed first value
     *
     * @param v1  the first value
     * @param <R> the type of first value
     */
    public <R> Tuple2<R, T2> setV1(R v1) {
        return new Tuple2<>(v1, _2);
    }

    /**
     * Returns second value
     */
    public T2 getV2() {
        return _2;
    }

    /**
     * Returns a tuple with changed second value
     *
     * @param v2  the second value
     * @param <R> the type of second value
     */
    public <R> Tuple2<T1, R> setV2(R v2) {
        return new Tuple2<>(_1, v2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1, _2);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "(", ")")
                .add(String.valueOf(_1))
                .add(String.valueOf(_2))
                .toString();
    }
}
