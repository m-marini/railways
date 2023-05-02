/*
 *
 * Copyright (c) 2021 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini;

import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.sqrt;

public interface ArgumentsGenerator<T> {

    int DEFAULT_NUM_TESTS = 100;

    /**
     * @param gen1
     * @param gen2
     * @param mapper
     * @param <T1>
     * @param <T2>
     * @param <R>
     */
    static <T1, T2, R> ArgumentsGenerator<R> combine(ArgumentsGenerator<T1> gen1, ArgumentsGenerator<T2> gen2, BiFunction<T1, T2, R> mapper) {
        return createArgumentGenerator((i, random) -> mapper.apply(gen1.apply(i, random), gen2.apply(i, random)));
    }

    static <T1, T2, T3, R> ArgumentsGenerator<R> combine(ArgumentsGenerator<T1> gen1,
                                                         ArgumentsGenerator<T2> gen2,
                                                         ArgumentsGenerator<T3> gen3,
                                                         Function3<T1, T2, T3, R> mapper) {
        return createArgumentGenerator((i, random) -> mapper.apply(gen1.apply(i, random), gen2.apply(i, random), gen3.apply(i, random)));
    }

    static <T1, T2, T3, T4, R> ArgumentsGenerator<R> combine(ArgumentsGenerator<T1> gen1,
                                                             ArgumentsGenerator<T2> gen2,
                                                             ArgumentsGenerator<T3> gen3,
                                                             ArgumentsGenerator<T4> gen4,
                                                             Function4<T1, T2, T3, T4, R> mapper) {
        return createArgumentGenerator((i, random) -> mapper.apply(gen1.apply(i, random),
                gen2.apply(i, random),
                gen3.apply(i, random),
                gen4.apply(i, random)));
    }

    static <T1, T2, T3, T4, T5, R> ArgumentsGenerator<R> combine(ArgumentsGenerator<T1> gen1,
                                                                 ArgumentsGenerator<T2> gen2,
                                                                 ArgumentsGenerator<T3> gen3,
                                                                 ArgumentsGenerator<T4> gen4,
                                                                 ArgumentsGenerator<T5> gen5,
                                                                 Function5<T1, T2, T3, T4, T5, R> mapper) {
        return createArgumentGenerator((i, random) -> mapper.apply(gen1.apply(i, random),
                gen2.apply(i, random),
                gen3.apply(i, random),
                gen4.apply(i, random),
                gen5.apply(i, random)));
    }

    /**
     * @param generator
     */
    static <T> Generator<T> createArgumentGenerator(BiFunction<Integer, Random, T> generator) {
        return new Generator<T>(generator);
    }

    /**
     * @param generator
     */
    static <T> Generator<T> createArgumentGenerator(Function<Random, T> generator) {
        return new Generator<T>((i, r) -> generator.apply(r));
    }

    /**
     * @param generator
     * @param specialValues
     */
    @SafeVarargs
    static <T> ArgumentsGenerator<T> createArgumentGenerator(Function<Random, T> generator, T... specialValues) {
        return new SpecialValueGenerator<T>(generator, List.of(specialValues));
    }

    /**
     * @param n
     * @param random
     * @param generators
     */
    static Stream<Arguments> createStream(int n, Random random, ArgumentsGenerator<?>... generators) {
        return IntStream.range(0, n).mapToObj(i ->
                Arguments.of(Arrays.stream(generators)
                        .map(gen -> gen.apply(i, random))
                        .toArray()));
    }

    /**
     * @param n
     * @param seed
     * @param generators
     */
    static Stream<Arguments> createStream(int n, long seed, ArgumentsGenerator<?>... generators) {
        return createStream(n, new Random(seed), generators);
    }

    /**
     * @param seed
     * @param generators
     */
    static Stream<Arguments> createStream(long seed, ArgumentsGenerator<?>... generators) {
        return createStream(DEFAULT_NUM_TESTS, seed, generators);
    }

    /**
     * @param n
     * @param random
     * @param generator
     */
    static Stream<Arguments> createStreamFromGenerator(int n, Random random, ArgumentsGenerator<Object[]> generator) {
        return IntStream.range(0, n).mapToObj(i -> Arguments.of(generator.apply(i, random)));
    }

    /**
     * @param n
     * @param seed
     * @param generator
     */
    static Stream<Arguments> createStreamFromGenerator(int n, long seed, ArgumentsGenerator<Object[]> generator) {
        return createStreamFromGenerator(n, new Random(seed), generator);
    }

    /**
     * @param seed
     * @param generator
     */
    static Stream<Arguments> createStreamFromGenerator(long seed, ArgumentsGenerator<Object[]> generator) {
        return createStreamFromGenerator(DEFAULT_NUM_TESTS, seed, generator);
    }

    /**
     * @param min
     * @param max
     */
    static ArgumentsGenerator<Double> exponential(double min, double max) {
        assert min > 0;
        assert max >= min;
        return createArgumentGenerator(random -> Math.exp(random.nextDouble() * Math.log(max / min)) * min, min, max, sqrt(min * max));
    }

    static ArgumentsGenerator<Float> exponential(float min, float max) {
        assert min > 0;
        assert max >= min;
        return createArgumentGenerator(random -> (float) (Math.exp(random.nextDouble() * Math.log(max / min)) * min), min, max, (float) sqrt(min * max));
    }

    static ArgumentsGenerator<Double> gaussian(double mean, double sigma) {
        assert sigma >= 0;
        return createArgumentGenerator(random -> random.nextGaussian() * sigma + mean, mean, mean - sigma, mean + sigma);
    }

    static ArgumentsGenerator<Float> gaussian(float mean, float sigma) {
        assert sigma >= 0;
        return createArgumentGenerator(random -> (float) random.nextGaussian() * sigma + mean, mean, mean - sigma, mean + sigma);
    }

    static ArgumentsGenerator<Boolean> probability(double p) {
        return createArgumentGenerator(random -> random.nextDouble() < p, false, true);
    }

    /**
     * @param min
     * @param max
     */
    static ArgumentsGenerator<Integer> uniform(int min, int max) {
        assert max >= min;
        return createArgumentGenerator(random -> random.nextInt(max - min) + min, min, max, (min + max) / 2);
    }

    /**
     * @param min
     * @param max
     */
    static ArgumentsGenerator<Float> uniform(float min, float max) {
        assert max >= min;
        return createArgumentGenerator(random -> (float) random.nextDouble() * (max - min) + min, min, max, (min + max) / 2);
    }

    /**
     * @param min
     * @param max
     */
    static ArgumentsGenerator<Double> uniform(double min, double max) {
        assert max >= min;
        return createArgumentGenerator(random -> random.nextDouble() * (max - min) + min, min, max, (min + max) / 2);
    }

    /**
     * @param i
     * @param random
     */
    T apply(int i, Random random);

    <R> ArgumentsGenerator<R> map(Function<T, R> f);

    class Generator<T> implements ArgumentsGenerator<T> {
        private final BiFunction<Integer, Random, T> generator;

        protected Generator(BiFunction<Integer, Random, T> generator) {
            this.generator = generator;
        }

        @Override
        public T apply(int i, Random random) {
            return generator.apply(i, random);
        }

        @Override
        public <R> ArgumentsGenerator<R> map(Function<T, R> f) {
            return new Generator<>((i, r) -> f.apply(this.apply(i, r)));
        }
    }

    class SpecialValueGenerator<T> extends Generator<T> {
        private final List<T> specialValues;

        protected SpecialValueGenerator(Function<Random, T> generator, List<T> specialValues) {
            super((i, r) -> generator.apply(r));
            this.specialValues = specialValues;
        }

        @Override
        public T apply(int i, Random random) {
            return i < specialValues.size() ? specialValues.get(i) : super.apply(i, random);
        }
    }
}