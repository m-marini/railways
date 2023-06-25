/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
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

import io.reactivex.rxjava3.core.Single;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.UnaryOperator;

/**
 * The simulator generates the evolution of the state of a system over time.
 * <p>
 * Starting from the initial state, it generates the new state with a function of the current state and the simulation
 * time spent. <br>
 * It is possible to set the relative simulation speed (time simulation / real time).<br>
 * The simulator also handles time-independent state change requests that are queued and processed synchronously
 * from the engine.<br>
 * The simulator emits state change events at regular intervals independent of the state processing speed,
 * this functionality is managed by an emission function that generates an event (read only event) from a state.
 * </p>
 * <p>
 * Two functions are used for the simulation:
 * <ul>
 *     <li>
 *         the state generation function with two parameters: current status and expected simulation time.<br>
 *         the function returns the new status and the actual simulation time which can be different from the expected one.
 *     </li>
 *     <li>
 *          the event-generating function that is called on each state change
 *     </li>
 * </ul>
 * </p>
 *
 * @param <T> the event type
 * @param <S> the seed (status) type
 */
public interface SimulatorEngine<T, S> {
    /**
     * Returns true if the simulator is active
     */
    boolean isActive();

    /**
     * Returns the seed after pushing the new seed in the flow
     * <p>
     * Example:
     * <code>
     * <pre>
     * engine.pushSeed(seed).subscribe(seed->{
     *     ... ; // the function is called at completion of seed change and the parameter is the changed seed
     * }) ;
     * </pre>
     * </code>
     * </p>
     *
     * @param seed the new seed
     */
    Single<S> pushSeed(final S seed);

    /**
     * Returns the seed after the transition.
     * <p>
     * Example:
     * <code>
     * <pre>
     * engine.request(seed ->{
     *     S newSeed= ... ; // change the seed
     *     return newSeed;
     * }).subscribe(seed->{
     *     ... ; // the function is called at completion of seed change and the parameter is the changed seed
     * }) ;
     * </pre>
     * </code>
     * </p>
     *
     * @param transition the transition
     */
    Single<S> request(final UnaryOperator<S> transition);

    /**
     * Returns the engine after setting the interval of event emission
     *
     * @param interval the interval to set
     */
    SimulatorEngine<T, S> setEventInterval(Duration interval);

    /**
     * Returns the engine after setting event consumer
     * <p>
     * Example:
     * <code>
     * <pre>
     * engine.setOnEvent(event->{
     *    ... // consumes event
     * });
     * </pre>
     * </code>
     * </p>
     *
     * @param onEvent the event consumer
     */
    SimulatorEngine<T, S> setOnEvent(Consumer<T> onEvent);

    /**
     * Returns the engine after setting event consumer
     *
     * @param onSpeed the speed consumer
     */
    SimulatorEngine<T, S> setOnSpeed(DoubleConsumer onSpeed);

    /**
     * Returns the seed when changing the simulation speed
     *
     * @param speed the simulation speed
     */
    Single<S> setSpeed(final double speed);

    /**
     * Returns the simulator shut down
     */
    SimulatorEngine shutdown();

    /**
     * Returns the first seed after the simulation stopping.
     * <p>
     * Example:
     * <code>
     * <pre>
     * engine.start(initialSeed).subscribe(seed->{
     *     ... ; // the function is called at completion of start and the parameter is the initial seed
     * }) ;
     * </pre>
     * </code>
     * </p>
     *
     * @param initialSeed the initial seed
     */
    Single<S> start(S initialSeed);

    /**
     * Returns the last seed after the simulation stopping
     * <p>
     * Example:
     * <code>
     * <pre>
     * engine.stop().subscribe(seed->{
     *     ... ; // the function is called at completion of stop and the parameter is the last seed
     * }) ;
     * </pre>
     * </code>
     * </p>
     */
    Single<S> stop();
}
