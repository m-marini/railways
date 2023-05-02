/*
 * Copyright (c) 2023  Marco Marini, marco.marini@mmarini.org
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

package org.mmarini.railways2.model.trains;

import org.mmarini.NotImplementedException;
import org.mmarini.railways2.model.RailwayConstants;
import org.mmarini.railways2.model.SimulationContext;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.OrientedLocation;
import org.mmarini.railways2.model.route.Entry;
import org.mmarini.railways2.model.route.Exit;
import org.mmarini.railways2.model.route.RouteDirection;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiFunction;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static org.mmarini.railways2.model.RailwayConstants.*;

/**
 * Tracks the train in the map
 */
public class Train {

    /**
     * Returns the entering train
     *
     * @param id          the train identifier
     * @param length      the length (# coaches)
     * @param arrival     the arrival entry
     * @param destination the destination exit
     */
    public static Train create(String id, int length, Entry arrival, Exit destination) {
        return new Train(id, length, arrival, destination, false, MAX_SPEED, State.ENTERING_STATE, null, null, ENTRY_TIMEOUT, ENTRY_TIMEOUT);
    }

    /**
     * @param id          the train identifier
     * @param length      the length (# coaches)
     * @param arrival     the arrival entry
     * @param destination the destination exit
     * @param time        the time instant
     */
    public static Train create(String id, int length, Entry arrival, Exit destination, double time) {
        return new Train(id, length, arrival, destination, false, MAX_SPEED, State.ENTERING_STATE, null, null, time + ENTRY_TIMEOUT, 0);
    }

    private final String id;
    private final boolean loaded;
    private final double speed;
    private final int length;
    private final OrientedLocation location;
    private final Entry arrival;
    private final Exit destination;
    private final Exit exitingNode;
    private final State state;
    private final double arrivalTime;
    private final double loadedTime;

    /**
     * Creates the train
     *
     * @param id          the train identifier
     * @param length      length (# coaches)
     * @param arrival     the entry node
     * @param destination the exit node
     * @param loaded      true if arrived
     * @param speed       speed (m/s)
     * @param state       the train state
     * @param location    the train location
     * @param exitingNode the exiting node
     * @param arrivalTime the arrival time (instant of train at entry)
     * @param loadedTime  the loaded time instant
     */
    protected Train(String id, int length, Entry arrival, Exit destination,
                    boolean loaded, double speed,
                    State state,
                    OrientedLocation location, Exit exitingNode,
                    double arrivalTime, double loadedTime) {
        this.id = requireNonNull(id);
        this.arrival = requireNonNull(arrival);
        this.destination = requireNonNull(destination);
        this.loaded = loaded;
        this.speed = speed;
        this.length = length;
        this.state = requireNonNull(state);
        this.location = location;
        this.exitingNode = exitingNode;
        this.arrivalTime = arrivalTime;
        this.loadedTime = loadedTime;
    }

    Optional<Train> braking(SimulationContext context) {
        throw new NotImplementedException();
    }

    /**
     * Returns the entering train status after simulating a time interval
     *
     * @param context the simulation context
     */
    Optional<Train> entering(SimulationContext context) {
        double t1 = context.getNextTime();
        if (t1 < arrivalTime) {
            // Train is arriving at entry
            return Optional.of(this);
        } else if (context.isEntryClear(this)) {
            // Enters the edge
            boolean stopped = arrivalTime <= 0;
            double speed = stopped ? 0 : RailwayConstants.MAX_SPEED;
            double distance = context.getDt() * speed;
            return arrival.getDirection(0)
                    .flatMap(RouteDirection::getLocation)
                    .map(e -> e.setDistance(distance))
                    .map(this::setLocation)
                    .map(train -> train.setSpeed(speed).runFast())
                    .or(() -> Optional.of(this));
        } else {
            // Train must stop at entry (entry busy)
            return Optional.of(setSpeed(0));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Train train = (Train) o;
        return id.equals(train.id);
    }

    /**
     * Returns the train in exit state
     *
     * @param exit exit node
     */
    public Train exit(Exit exit) {
        return setState(State.EXITING_STATE).setExitingNode(exit);
    }

    Optional<Train> exiting(SimulationContext simulationContext) {
        throw new NotImplementedException();
    }

    /**
     * Returns the arrival train entry
     */
    public Entry getArrival() {
        return arrival;
    }

    /**
     * Returns the entry timer (s)
     */
    public double getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Returns the destination train exit
     */
    public Exit getDestination() {
        return destination;
    }

    /**
     * Returns the exiting node
     */
    public Exit getExitingNode() {
        return exitingNode;
    }

    /**
     * Returns the train with exiting node set
     *
     * @param exitingNode the exiting node
     */
    public Train setExitingNode(Exit exitingNode) {
        return exitingNode.equals(this.exitingNode) ?
                this :
                new Train(id, length, arrival, destination, loaded, speed, state, location, exitingNode, arrivalTime, loadedTime);
    }

    /**
     * Returns the train identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the length of train (# coaches)
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the location of a train
     */
    public OrientedLocation getLocation() {
        return location;
    }

    /**
     * Returns the train with location set
     *
     * @param location the location
     */
    public Train setLocation(OrientedLocation location) {
        return location.equals(this.location) ?
                this :
                new Train(id, length, arrival, destination, loaded, speed, state, location, exitingNode, arrivalTime, loadedTime);
    }

    /**
     * Returns the speed (m/s)
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Returns the train with speed set
     *
     * @param speed the speed (m/s)
     */
    public Train setSpeed(double speed) {
        return speed != this.speed ? new Train(id, length, arrival, destination, loaded, speed, state, location, exitingNode, arrivalTime, loadedTime) : this;
    }

    /**
     * Returns the state of train
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the train with state set
     *
     * @param state the state
     */
    public Train setState(State state) {
        return state.equals(this.state) ?
                this :
                new Train(id, length, arrival, destination, loaded, speed, state, location, exitingNode, arrivalTime, loadedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns true if train is entering (state == ENTERING_STATE)
     */
    public boolean isEntering() {
        return state.equals(State.ENTERING_STATE);
    }

    /**
     * Returns true if the train is in EXITING or LEAVING state
     */
    public boolean isExiting() {
        return state.equals(State.LEAVING_STATE) || state.equals(State.EXITING_STATE);
    }

    /**
     * Returns true if arrived
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Returns the train with arrived state set
     *
     * @param loaded true if arrived
     */
    Train setLoaded(boolean loaded) {
        return loaded == this.loaded ?
                this :
                new Train(id, length, arrival, destination, loaded, speed, state, location, exitingNode, arrivalTime, loadedTime);
    }

    /**
     * Returns the train in leaving state
     *
     * @param time the time
     */
    public Train leave(double time) {
        return setState(State.LEAVING_STATE);
    }

    private Optional<Train> leaving(SimulationContext simulationContext) {
        throw new NotImplementedException();
    }

    /**
     * Returns the train in load status.
     * Stop the train and set the state to wait for loaded
     *
     * @param time the time
     */
    Train load(double time) {
        return setSpeed(0)
                .setLoadedTime(time + LOADING_TIME)
                .setState(State.LOADING_STATE);
    }

    /**
     * Returns the loading train status after simulating a time interval
     *
     * @param context the simulation context
     */
    Optional<Train> loading(SimulationContext context) {
        return Optional.of(context.getTime() + context.getDt() < loadedTime ? this : stop().setLoaded(true));
    }

    /**
     * Returns the train in run fast state
     */
    private Train runFast() {
        return setState(State.RUNNING_FAST_STATE);
    }

    /**
     * Returns the running fast train status after simulating a time interval
     *
     * @param context the simulation context
     */
    Optional<Train> runningFast(SimulationContext context) {
        // Calcolare la distanza del prossimo segnale di stop o la fermata al binario
        boolean clearTrack = context.isNextTracksClear(getLocation(), stopDistance());
        if (clearTrack) {
            // Move head
            double movement = speed * context.getDt();
            double newDistance = location.getDistance() + movement;
            Edge edge = location.getEdge();
            double newSpeed = speedPhysics(MAX_SPEED, context.getDt());
            if (newDistance > edge.getLength()) {
                // end of edge reached
                double newDistance1 = newDistance - edge.getLength();
                Optional<OrientedLocation> newLocation = context.terminalDirection(location)
                        .flatMap(RouteDirection::connectedDirection)
                        .flatMap(RouteDirection::getLocation)
                        .map(ep -> ep.setDistance(newDistance1));
                return newLocation.map(this::setLocation)
                        .map(train -> train.setSpeed(newSpeed))
                        .or(() -> Optional.of(this));
            } else {
                OrientedLocation newLocation = location.setDistance(newDistance);
                return Optional.of(setLocation(newLocation).setSpeed(newSpeed));
            }
        } else {
            // Stop
            double newSpeed = speedPhysics(0, context.getDt());
            double newDistance = location.getDistance() + context.getDt() * speed;
            OrientedLocation newLocation = location.setDistance(newDistance);
            return Optional.of(setLocation(newLocation).setSpeed(newSpeed));
        }
    }

    private Optional<Train> runningMin(SimulationContext context) {
        throw new NotImplementedException();
    }

    /**
     * Returns the train with loaded time set
     *
     * @param loadedTime the loaded time
     */
    Train setLoadedTime(double loadedTime) {
        return loadedTime == this.loadedTime ?
                this :
                new Train(id, length, arrival, destination, loaded, speed, state, location, exitingNode, arrivalTime, loadedTime);
    }

    /**
     * Returns the speed constrained by the train physics for a given target speed
     *
     * @param targetSpeed the target speed (m/s)
     * @param dt          the time interval
     */
    double speedPhysics(double targetSpeed, double dt) {
        double acc = min(max((targetSpeed - speed) / dt, DEACCELERATION), ACCELERATION);
        return min(speed + acc * dt, MAX_SPEED);
    }

    /**
     * Returns the train stared
     */
    Train start() {
        return setState(State.RUNNING_FAST_STATE);
    }

    /**
     * Returns the stop train
     */
    Train stop() {
        return setSpeed(0).setState(State.WAITING_FOR_RUN_STATE);
    }

    /**
     * Returns the distance to stop the train (m)
     */
    public double stopDistance() {
        /*
         * a = v/t => t = v/a s = 1/2 a t^2 = 1/2 a v^2/a^2 = 1/2 v^2/a
         */
        return speed * speed / DEACCELERATION * -0.5;
    }

    /**
     * Returns the train status after simulating a time interval
     *
     * @param context the simulation context
     */
    public Optional<Train> tick(SimulationContext context) {
        return state.apply(this, context);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Train.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("location=" + location)
                .toString();
    }

    /**
     * Returns the waiting for run train status after simulating a time interval
     *
     * @param context the simulation context
     */
    Optional<Train> waitingForRun(SimulationContext context) {
        return Optional.of(setSpeed(0));
    }

    /**
     * Returns the waiting for semaphore train status after simulating a time interval
     *
     * @param context the simulation context
     */
    Optional<Train> waitingForSignal(SimulationContext context) {
        return Optional.of(context.isNextSignalClear(location) ? start() : this);
    }

    static class State {
        public static final State BRAKING_STATE = new State("BRAKEING", Train::braking);
        public static final State RUNNING_MIN_STATE = new State("RUNNING_MIN", Train::runningMin);
        public static final State RUNNING_FAST_STATE = new State("RUNNING_FAST", Train::runningFast);
        public static final State ENTERING_STATE = new State("ENTERING", Train::entering);
        public static final State EXITING_STATE = new State("EXITING", Train::exiting);
        public static final State LEAVING_STATE = new State("LEAVING", Train::leaving);
        public static final State WAITING_FOR_SIGNAL_STATE = new State("WAITING_FOR_SIGNAL", Train::waitingForSignal);
        public static final State WAITING_FOR_RUN_STATE = new State("WAITING_FOR_RUN", Train::waitingForRun);
        public static final State LOADING_STATE = new State("LOADING", Train::loading);

        private final String id;
        private final BiFunction<Train, SimulationContext, Optional<Train>> function;

        /**
         * Creates the state
         *
         * @param id       the state id
         * @param function the simulation function
         */
        protected State(String id, BiFunction<Train, SimulationContext, Optional<Train>> function) {
            this.id = requireNonNull(id);
            this.function = requireNonNull(function);
        }

        Optional<Train> apply(Train train, SimulationContext context) {
            return function.apply(train, context);
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
