/*
 * Copyright (c) 2019-2023  Marco Marini, marco.marini@mmarini.org
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

package org.mmarini.railways2.model;

import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import org.mmarini.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.*;

import static java.lang.Math.floor;
import static java.util.Objects.requireNonNull;

/**
 * The concreat implementations of Simualtor engine
 * <p>
 * Examples:
 * <code>
 * <pre>
 * SimulatorEngine&lt;S,T> engine = new SimulatorEngineImpl.create(
 *     initialSeed,
 *     (seed, dt)->{
 *       S nextSeed = ... ;    // Computes the next seed after expected dt
 *       double actualDt = dt; // Computes the real simulation time interval applied (should be dt)
 *       return Tuple.of(nextSeed, actualDt);
 *     },
 *     seed->{
 *         T event = ...  ; // Creates the event (read only) from seed
 *         return event;
 *     });
 * engine.setEventInterval(...)
 * engine.setOnEvent(event->{
 *    ... // consumes event
 * });
 * engine.start();
 * </pre>
 * </code>
 * <p>
 *
 * @param <T> the event type
 * @param <S> the seed type
 */
public class SimulatorEngineImpl<T, S> implements SimulatorEngine<T, S> {
    public static final long MIN_SLEEP_TIME = 10L;
    public static final long NANOSPS = 1000000000L;
    private static final Logger logger = LoggerFactory.getLogger(SimulatorEngineImpl.class);

    /**
     * Returns a simulator.
     * <p>
     * The simulator is bound to a new dedicated thread worker that serializes the
     * activities
     * </p>
     *
     * @param <T>         the event type
     * @param <S>         the seed type
     * @param initialSeed the initial seed
     * @param nextSeed    the function returning next seed applying a seed and the time interval
     * @param emit        the function returning the event applying a seed
     */
    public static <T, S> SimulatorEngineImpl<T, S> create(S initialSeed,
                                                          BiFunction<S, Double, Tuple2<S, Double>> nextSeed,
                                                          Function<S, T> emit) {
        return new SimulatorEngineImpl<>(Schedulers.newThread().createWorker(),
                initialSeed, nextSeed, emit);
    }

    private final Worker worker;
    private final Deque<ProcessRequest> queue; // The process request queue
    private final BiFunction<S, Double, Tuple2<S, Double>> nextSeed; // the status generator function
    private final Function<S, T> emit; // the event generator function
    private S seed; // Current status
    private DoubleConsumer onSpeed;
    private Consumer<T> onEvent;
    private double speed; // relative speed
    private Status status; // the current simulation status
    private long eventInterval; // the interval between static change event

    /**
     * Creates the simulator.
     *
     * @param worker      the assigned worker
     * @param initialSeed the initial seed
     * @param nextSeed    the function returning next seed applying a seed and the time interval
     * @param emit        the function returning the event applying a seed
     */
    protected SimulatorEngineImpl(Worker worker,
                                  S initialSeed,
                                  BiFunction<S, Double, Tuple2<S, Double>> nextSeed,
                                  Function<S, T> emit) {
        requireNonNull(worker);
        requireNonNull(initialSeed);
        requireNonNull(nextSeed);
        requireNonNull(emit);
        this.nextSeed = nextSeed;
        this.emit = emit;
        this.worker = worker;
        this.queue = new ConcurrentLinkedDeque<>();
        this.speed = 1;
        this.seed = initialSeed;
        this.status = Status.IDLE;
    }

    /**
     * Deque the queue
     */
    private void deque() {
        for (; ; ) {
            ProcessRequest request = queue.poll();
            if (request != null) {
                seed = request.transition.apply(seed);
                request.result.onSuccess(seed);
            } else {
                break;
            }
        }
    }

    private void emitEvent(T event) {
        if (onEvent != null) {
            onEvent.accept(event);
        }
    }

    private void emitSpeed(double speed) {
        if (onSpeed != null) {
            onSpeed.accept(speed);
        }
    }

    /**
     * Simulation cycle
     */
    void processCycle() {
        // time instant od last event
        Instant lastEvent = Instant.now();
        // Simulated interval from last event
        double simulatedInterval = 0;
        while (status == Status.ACTIVE) {
            // Processes request queue
            deque();
            // Simulation interval
            double simInterval = eventInterval * speed / NANOSPS - simulatedInterval;
            if (simInterval > 0) {
                // Simulation interval positive => process step to the next event instant
                // Computes the next status
                Tuple2<S, Double> tuple = nextSeed.apply(seed, simInterval);
                seed = tuple._1;
                // Update simulation intervals
                simulatedInterval += tuple._2;
                // Computes the elapsed time of the process step
            } else {
                // Simulation is faster than elapsed time
                long sleepInterval = (long) floor(-simInterval / speed * 1000L / MIN_SLEEP_TIME) * MIN_SLEEP_TIME;
                if (sleepInterval > 0) {
                    try {
                        Thread.sleep(sleepInterval);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    Thread.yield();
                }
            }
            Instant now = Instant.now();
            // Check for event emission timeout
            long currentEventInterval = Duration.between(lastEvent, now).toNanos();
            if (currentEventInterval >= this.eventInterval) {
                // Event generation timeout
                double currentSpeed = simulatedInterval / currentEventInterval * NANOSPS;
                // Event time out
                emitEvent(emit.apply(seed));
                emitSpeed(currentSpeed);
                lastEvent = now;
                simulatedInterval = 0;
            }
        }
    }

    @Override
    public Single<S> pushSeed(S seed) {
        requireNonNull(seed);
        SingleSubject<S> result = SingleSubject.create();
        queue.offer(new ProcessRequest(e -> seed, result));
        if (status.equals(Status.IDLE)) {
            deque();
        }
        return result;
    }

    @Override
    public Single<S> request(UnaryOperator<S> transition) {
        requireNonNull(transition);
        SingleSubject<S> result = SingleSubject.create();
        queue.offer(new ProcessRequest(transition, result));
        if (status == Status.IDLE) {
            deque();
        }
        return result;
    }

    @Override
    public SimulatorEngineImpl<T, S> setEventInterval(Duration interval) {
        requireNonNull(interval);
        eventInterval = interval.toNanos();
        return this;
    }

    @Override
    public SimulatorEngineImpl<T, S> setOnEvent(Consumer<T> onEvent) {
        requireNonNull(onEvent);
        this.onEvent = onEvent;
        return this;
    }

    @Override
    public SimulatorEngineImpl<T, S> setOnSpeed(DoubleConsumer onSpeed) {
        requireNonNull(onSpeed);
        this.onSpeed = onSpeed;
        return this;
    }

    @Override
    public Single<S> setSpeed(double speed) {
        SingleSubject<S> result = SingleSubject.create();
        queue.offer(new ProcessRequest(e -> {
            this.speed = speed;
            return e;
        }, result));
        if (status == Status.IDLE) {
            deque();
        }
        return result;
    }

    @Override
    public Single<S> start() {
        logger.debug("Starting simulation ...");
        if (status == Status.IDLE) {
            SingleSubject<S> result = SingleSubject.create();
            queue.offer(new ProcessRequest(e -> {
                status = Status.ACTIVE;
                return e;
            }, result));
            worker.schedule(this::startProcess);
            return result;
        }
        return Single.error(new IllegalArgumentException("Simulator in wrong state: " + status));
    }

    void startProcess() {
        logger.debug("Simulation started.");
        // cumulative simulation time
        deque();
        // Last event instance
        processCycle();
    }

    @Override
    public Single<S> stop() {
        logger.debug("Stopping simulation ...");
        SingleSubject<S> result = SingleSubject.create();
        queue.offer(new ProcessRequest(e -> {
            status = Status.IDLE;
            return e;
        }
                , result));
        if (status == Status.IDLE) {
            deque();
        }
        return result;
    }

    /**
     * The simulator status.
     */
    enum Status {
        IDLE, ACTIVE
    }

    class ProcessRequest {
        final UnaryOperator<S> transition;
        final SingleSubject<S> result;

        ProcessRequest(UnaryOperator<S> transition, SingleSubject<S> result) {
            this.transition = transition;
            this.result = result;
        }
    }

}
