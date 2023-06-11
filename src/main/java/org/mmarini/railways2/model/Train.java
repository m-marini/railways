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

package org.mmarini.railways2.model;

import org.mmarini.Function3;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Route;

import java.util.Objects;
import java.util.Optional;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static org.mmarini.railways2.model.RailwayConstants.*;

/**
 * Identifies the train, locates it, moves it in the dispatched routes
 */
public class Train {
    public static final State EXITING_STATE = new State("EXITING", Train::exiting);
    public static final State WAITING_FOR_RUN_STATE = new State("WAITING_FOR_RUN", Train::waitingForRun);
    public static final State LOADING_STATE = new State("LOADING_STATE", Train::loading);

    /**
     * Returns the train
     *
     * @param id          the train identifier
     * @param numCoaches  the numCoaches
     * @param arrival     the arrival entry
     * @param destination the destination exit
     */
    public static Train create(String id, int numCoaches, Entry arrival, Exit destination) {
        return new Train(id, numCoaches, arrival, destination,
                ENTERING_STATE, ENTRY_TIMEOUT, null, MAX_SPEED, false, 0,
                null, 0);
    }

    private final String id;
    private final int numCoaches;
    private final Entry arrival;
    private final Exit destination;
    private final State state;
    private final EdgeLocation location;
    private final double arrivalTime;
    private final double speed;
    private final boolean loaded;
    private final double loadedTime;
    private final Exit exitingNode;
    private final double exitDistance;

    /**
     * Creates the train
     *
     * @param id           the train identifier
     * @param numCoaches   number of coaches
     * @param arrival      the entry node
     * @param destination  the exit node
     * @param state        the train state
     * @param arrivalTime  the entry timeout instant (when the train will arrive at entry point) (s)
     * @param location     the train head location
     * @param speed        the train speed (m/s)
     * @param loaded       true if train is loaded
     * @param loadedTime   the loaded time
     * @param exitingNode  the exiting node
     * @param exitDistance the exit distance
     */
    protected Train(String id, int numCoaches, Entry arrival, Exit destination, State state, double arrivalTime, EdgeLocation location, double speed, boolean loaded, double loadedTime, Exit exitingNode, double exitDistance) {
        this.id = requireNonNull(id);
        this.arrival = requireNonNull(arrival);
        this.destination = requireNonNull(destination);
        this.numCoaches = numCoaches;
        this.state = requireNonNull(state);
        this.location = location;
        this.arrivalTime = arrivalTime;
        this.speed = speed;
        this.loaded = loaded;
        this.loadedTime = loadedTime;
        this.exitingNode = exitingNode;
        this.exitDistance = exitDistance;
    }

    /**
     * Returns the braking train status after simulating a time interval
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval (s)
     */
    Optional<Tuple2<Train, Double>> braking(SimulationContext context, double dt) {
        return running(context, dt, 0)
                .map(tuple -> (tuple._1.getState().equals(Train.WAITING_FOR_SIGNAL_STATE)) ?
                        tuple.setV1(tuple._1.stop())
                        : tuple);
    }

    /**
     * Returns the train status after simulating a time interval
     * and the remaining time interval
     *
     * @param ctx the simulation context
     * @param dt  the time interval (s)
     */
    Optional<Tuple2<Train, Double>> changeState(SimulationContext ctx, double dt) {
        return state.apply(this, ctx, dt);
    }    public static final State RUNNING_STATE = new State("RUNNING", Train::running);

    /**
     * Returns the simulated entering train
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval
     */
    Optional<Tuple2<Train, Double>> entering(SimulationContext context, double dt) {
        double timeToArrive = context.getTimeTo(arrivalTime);
        if (timeToArrive > dt) {
            // Train not yet arrived
            return Optional.of(Tuple2.of(this, 0d));
        }
        if (!context.isEntryClear(this)) {
            // Train must stop at entry (entry busy)
            return Optional.of(Tuple2.of(setSpeed(0), 0d));
        }
        // Enters the edge
        Direction dir = arrival.getValidExits().iterator().next();
        EdgeLocation location = new EdgeLocation(dir, dir.getEdge().getLength());
        return Optional.of(Tuple2.of(setLocation(location)
                // Checks if train has arrived and stopped
                .setSpeed(arrivalTime <= 0 ? 0 : MAX_SPEED)
                .run(), dt));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Train train = (Train) o;
        return id.equals(train.id);
    }

    /**
     * Returns the train in exiting state through the given exit at the given distance
     *
     * @param exit         the exit route
     * @param exitDistance the exit distance
     */
    Train exit(Exit exit, double exitDistance) {
        return setState(EXITING_STATE).setExitingNode(exit).setExitDistance(exitDistance);
    }

    /**
     * Returns the exiting train status after simulating a time interval
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval
     */
    Optional<Tuple2<Train, Double>> exiting(SimulationContext context, double dt) {
        double newSpeed = speedPhysics(MAX_SPEED, dt);
        double newExitDistance = exitDistance + speed * dt;
        return newExitDistance >= EXIT_DISTANCE + getLength() ?
                Optional.empty() :
                Optional.of(Tuple2.of(setExitDistance(newExitDistance).setSpeed(newSpeed), 0d));
    }

    /**
     * Returns the arrival entry
     */
    public Entry getArrival() {
        return arrival;
    }

    /**
     * Returns the arrival time instant (s)
     */
    public double getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Returns the train with arrival time set (when the train will arrive at entry point)
     *
     * @param arrivalTime the arrival time (s)
     */
    public Train setArrivalTime(double arrivalTime) {
        return this.arrivalTime == arrivalTime ? this :
                new Train(id, numCoaches, arrival, destination, state, arrivalTime, location, speed, loaded, loadedTime, exitingNode, exitDistance);
    }

    /**
     * Returns the destination of train
     */
    public Exit getDestination() {
        return destination;
    }

    /**
     * Returns the exit distance
     */
    double getExitDistance() {
        return exitDistance;
    }

    private Train setExitDistance(double exitDistance) {
        return this.exitDistance == exitDistance ? this :
                new Train(id, numCoaches, arrival, destination, state, arrivalTime, location, speed, loaded, loadedTime, exitingNode, exitDistance);
    }

    /**
     * Returns the exiting node
     */
    public Exit getExitingNode() {
        return exitingNode;
    }

    public Train setExitingNode(Exit exitingNode) {
        return exitingNode.equals(this.exitingNode) ? this :
                new Train(id, numCoaches, arrival, destination, state, arrivalTime, location, speed, loaded, loadedTime, exitingNode, exitDistance);
    }

    /**
     * Returns the train identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the length of train (m)
     */
    public double getLength() {
        return numCoaches * COACH_LENGTH;
    }

    /**
     * Returns the location of train head
     */
    public Optional<EdgeLocation> getLocation() {
        return Optional.ofNullable(location);
    }

    /**
     * Returns the train with location set
     *
     * @param location the location
     */
    public Train setLocation(EdgeLocation location) {
        return location == this.location
                || location != null && location.equals(this.location)
                || this.location != null && this.location.equals(location) ? this :
                new Train(id, numCoaches, arrival, destination, state, arrivalTime, location, speed, loaded, loadedTime, exitingNode, exitDistance);
    }    public static final State WAITING_FOR_SIGNAL_STATE = new State("WAITING_FOR_SIGNAL", Train::waitingForSignal);

    /**
     * Returns the number of coaches
     */
    public int getNumCoaches() {
        return numCoaches;
    }

    /**
     * Returns the train speed (m/s)
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Returns the train with the given speed
     *
     * @param speed the speed
     */
    public Train setSpeed(double speed) {
        return speed == this.speed ? this :
                new Train(id, numCoaches, arrival, destination, state, arrivalTime, location, speed, loaded, loadedTime, exitingNode, exitDistance);
    }

    /**
     * Returns the train state
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the train in the given state
     *
     * @param state the state
     */
    public Train setState(State state) {
        return this.state.equals(state) ? this :
                new Train(id, numCoaches, arrival, destination, state, arrivalTime, location, speed, loaded, loadedTime, exitingNode, exitDistance);
    }

    /**
     * Returns the distance to stop the train (m)
     */
    public double getStopDistance() {
        /*
         * a = v/t => t = v/a s = 1/2 a t^2 = 1/2 a v^2/a^2 = 1/2 v^2/a
         */
        return speed * speed / DEACCELERATION * -0.5;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns true if train is entering (state == ENTERING)
     */
    public boolean isEntering() {
        return ENTERING_STATE.equals(state);
    }

    /**
     * Returns true if train is exiting
     */
    public boolean isExiting() {
        return EXITING_STATE.equals(state);
    }

    /**
     * Returns true if the train has not yet loaded the passengers
     */
    public boolean isUnloaded() {
        return !loaded;
    }

    /**
     * Returns the train in load status.
     * Stop the train and set the trainState to wait for loaded
     *
     * @param time the time
     */
    public Train load(double time) {
        return setSpeed(0)
                .setLoadedTime(time + LOADING_TIME)
                .setState(Train.LOADING_STATE);
    }

    /**
     * Returns the loading train status after simulating a time interval
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval
     */
    Optional<Tuple2<Train, Double>> loading(SimulationContext context, double dt) {
        double timeToLoad = context.getTimeTo(loadedTime);
        return (dt < timeToLoad) ?
                // Wait for loaded
                Optional.of(Tuple2.of(this, 0d)) :
                // Load completed
                Optional.of(Tuple2.of(
                        stop().setLoaded(),
                        dt - timeToLoad));
    }

    /**
     * Returns the train in running state
     */
    Train run() {
        return setState(RUNNING_STATE);
    }

    /**
     * Returns the running fast train status after simulating a time interval
     *
     * @param context the simulation context
     */
    Optional<Tuple2<Train, Double>> running(SimulationContext context, double dt) {
        return running(context, dt, MAX_SPEED);
    }

    /**
     * Returns the running train to speed status after simulating a time interval.
     * The train will change status to:
     * <ul>
     * <li>WAITING_FOR_RUN in case of speed 0 or entry node reached</li>
     * <li>WAITING_FOR SIGNAL in case of not clear signal reached</li>
     * <li>LOADING in case of platform reached</li>
     * </ul>
     *
     * @param context     the simulation context
     * @param dt          the time tinterval
     * @param targetSpeed the target speed (m/s)
     */
    Optional<Tuple2<Train, Double>> running(SimulationContext context, double dt, double targetSpeed) {
        double timeToEndEdge = location.getDistance() / speed;
        boolean clearTrack = context.isNextTracksClear(this);
        Direction direction = location.getDirection();
        Edge edge = direction.getEdge();
        Node destination = direction.getDestination();
        Route route = context.getRoute(destination);
        double newSpeed = clearTrack ?
                speedPhysics(targetSpeed, dt)
                : max(speedPhysics(0, dt), APPROACH_SPEED);
        double movement = speed * dt;
        double newDistance = location.getDistance() - movement;

        if (dt < timeToEndEdge) {
            // Move head in the current edge
            EdgeLocation newLocation = location.setDistance(newDistance);
            if (targetSpeed > APPROACH_SPEED || newSpeed > APPROACH_SPEED) {
                return Optional.of(Tuple2.of(
                        setLocation(newLocation).setSpeed(max(newSpeed, APPROACH_SPEED)),
                        0d));
            } else {
                // Train is braking
                return Optional.of(Tuple2.of(
                        setLocation(newLocation).setSpeed(newSpeed).setState(WAITING_FOR_RUN_STATE),
                        0d));
            }
        }

        double remainingTime = dt - timeToEndEdge;
        // end of edge reached
        if (route instanceof Exit) {
            // exit node reached
            return Optional.of(Tuple2.of(
                    setState(EXITING_STATE)
                            .setLocation(null)
                            .setExitingNode((Exit) route)
                            .setExitDistance(0),
                    remainingTime));
        }
        if (edge instanceof Platform && !loaded) {
            // platform reached and train not loaded
            return Optional.of(Tuple2.of(
                    setState(LOADING_STATE)
                            .setLocation(location.setDistance(0))
                            .load(context.getTimeAfter(timeToEndEdge)),
                    remainingTime));
        }
        if (!context.isNextRouteClear(direction)) {
            // next route is not clear (stop signal)
            return Optional.of(Tuple2.of(setState(WAITING_FOR_SIGNAL_STATE)
                            .setLocation(location.setDistance(0))
                            .setSpeed(0),
                    remainingTime));
        }
        // Get the new direction
        Optional<Direction> routeDirection = context.getNextExit(location.getDirection());
        return routeDirection.map(newDir -> {
                    Edge newEdge = newDir.getEdge();
                    // Computes the new location
                    EdgeLocation newLocation = new EdgeLocation(newDir, newEdge.getLength());
                    // Lock signals of new section
                    context.lockSignals(newDir);
                    return Tuple2.of(setSpeed(newSpeed).setLocation(newLocation), remainingTime);
                }
        ).or(() -> Optional.of(Tuple2.of(this, 0d)));
    }

    /**
     * Returns the train in loaded state
     */
    public Train setLoaded() {
        return loaded ? this :
                new Train(id, numCoaches, arrival, destination, state, arrivalTime, location, speed, true, loadedTime, exitingNode, exitDistance);
    }    public static final State BRAKING_STATE = new State("BRAKING_STATE", Train::braking);

    /**
     * Returns the train with loaded time set
     *
     * @param loadedTime the loaded time
     */
    private Train setLoadedTime(double loadedTime) {
        return this.loadedTime == loadedTime ? this :
                new Train(id, numCoaches, arrival, destination, state, arrivalTime, location, speed, loaded, loadedTime, exitingNode, exitDistance);
    }

    /**
     * Returns the real speed applying the physics constraints (m/s)
     *
     * @param targetSpeed target speed (m/s)
     * @param dt          the time interval
     */
    public double speedPhysics(double targetSpeed, double dt) {
        double acc = min(max((targetSpeed - speed) / dt, DEACCELERATION), ACCELERATION);
        return min(speed + acc * dt, MAX_SPEED);
    }

    /**
     * Returns the train started
     */
    private Train start() {
        return setState(Train.RUNNING_STATE);
    }

    /**
     * Returns the stopped train
     */
    Train stop() {
        return setSpeed(0).setState(Train.WAITING_FOR_RUN_STATE);
    }

    /**
     * Returns the train status after simulating a time interval
     *
     * @param ctx the simulation context
     * @param dt  the time interval (s)
     */
    public Optional<Train> tick(SimulationContext ctx, double dt) {
        Train train = this;
        do {
            // Computes the next transition
            Optional<Tuple2<Train, Double>> transition = train.changeState(ctx, dt);
            train = transition.map(Tuple2::getV1).orElse(null);
            dt = transition.map(Tuple2::getV2).orElse(0d);
        } while (train != null && dt > 0);
        return Optional.ofNullable(train);
    }

    /*
    Optional<Train> running1(SimulationContext context, double targetSpeed) {
        double movement = speed * context.getDt();
        boolean clearTrack = context.isNextTracksClear(this);
        Direction direction = location.getDirection();
        Edge edge = direction.getEdge();
        Node destination = direction.getDestination();
        Route route = context.getRoute(destination);
        double newDistance = location.getDistance() - movement;
        double newSpeed = clearTrack ?
                speedPhysics(targetSpeed, context.getDt())
                : max(speedPhysics(0, context.getDt()), APPROACH_SPEED);

        if (newDistance >= 0) {
            // Move head in the current edge
            EdgeLocation newLocation = location.setDistance(newDistance);
            if (targetSpeed > APPROACH_SPEED || newSpeed > APPROACH_SPEED) {
                return Optional.of(setLocation(newLocation).setSpeed(max(newSpeed, APPROACH_SPEED)));
            } else {
                // Train is braking
                return Optional.of(setLocation(newLocation).setSpeed(newSpeed).setState(WAITING_FOR_RUN_STATE));
            }
        }
        // end of edge reached
        if (route instanceof Exit) {
            // exit node reached
            return Optional.of(setState(EXITING_STATE)
                    .setLocation(null)
                    .setExitingNode((Exit) route)
                    .setExitDistance(-newDistance));
        }
        if (edge instanceof Platform && !loaded) {
            // platform reached and train not loaded
            return Optional.of(setState(LOADING_STATE)
                    .setLocation(location.setDistance(0))
                    .setSpeed(0));
        }
        if (!context.isNextRouteClear(direction)) {
            // next route is not clear (stop signal)
            return Optional.of(setState(WAITING_FOR_SIGNAL_STATE)
                    .setLocation(location.setDistance(0))
                    .setSpeed(0));
        }
        // Get the new direction
        Optional<Direction> routeDirection = context.getNextExit(location.getDirection());
        return routeDirection.map(newDir -> {
                    Edge newEdge = newDir.getEdge();
                    // Computes the new location
                    double newDistance1 = max(newEdge.getLength() + newDistance, 0);
                    EdgeLocation newLocation = new EdgeLocation(newDir, newDistance1);
                    // Lock signals of new section
                    context.lockSignals(newDir);
                    return setSpeed(newSpeed).setLocation(newLocation);
                }
        ).or(() -> Optional.of(this));
    }

    Optional<Tuple2<Train, Double>> running2(SimulationContext context, double targetSpeed, double movement) {
        boolean clearTrack = context.isNextTracksClear(this);
        Direction direction = location.getDirection();
        Edge edge = direction.getEdge();
        Node destination = direction.getDestination();
        Route route = context.getRoute(destination);
        double newDistance = location.getDistance() - movement;
        double newSpeed = clearTrack ?
                speedPhysics(targetSpeed, context.getDt())
                : max(speedPhysics(0, context.getDt()), APPROACH_SPEED);

        if (newDistance >= 0) {
            // Move head in the current edge
            EdgeLocation newLocation = location.setDistance(newDistance);
            if (targetSpeed > APPROACH_SPEED || newSpeed > APPROACH_SPEED) {
                return Optional.of(Tuple2.of(
                        setLocation(newLocation).setSpeed(max(newSpeed, APPROACH_SPEED)),
                        0d));
            } else {
                // Train is braking
                return Optional.of(Tuple2.of(
                        setLocation(newLocation).setSpeed(newSpeed).setState(WAITING_FOR_RUN_STATE),
                        0d));
            }
        }
        // end of edge reached
        if (route instanceof Exit) {
            // exit node reached
            return Optional.of(Tuple2.of(setState(EXITING_STATE)
                            .setLocation(null)
                            .setExitingNode((Exit) route)
                            .setExitDistance(-newDistance),
                    0d));
        }
        if (edge instanceof Platform && !loaded) {
            // platform reached and train not loaded
            return Optional.of(Tuple2.of(setState(LOADING_STATE)
                            .setLocation(location.setDistance(0))
                            .setSpeed(0),
                    0d));
        }
        if (!context.isNextRouteClear(direction)) {
            // next route is not clear (stop signal)
            return Optional.of(Tuple2.of(setState(WAITING_FOR_SIGNAL_STATE)
                            .setLocation(location.setDistance(0))
                            .setSpeed(0),
                    0d));
        }
        // Get the new direction
        Optional<Direction> routeDirection = context.getNextExit(location.getDirection());
        return routeDirection.map(newDir -> {
                    Edge newEdge = newDir.getEdge();
                    // Computes the new location
                    double newDistance1 = max(newEdge.getLength() + newDistance, 0);
                    EdgeLocation newLocation = new EdgeLocation(newDir, newDistance1);
                    // Lock signals of new section
                    context.lockSignals(newDir);
                    return Tuple2.of(setSpeed(newSpeed).setLocation(newLocation), 0d);
                }
        ).or(() -> Optional.of(Tuple2.of(this, 0d)));
    }


     */

    /*
    Optional<Train> runningOld(SimulationContext context, double targetSpeed) {
        double movement = speed * context.getDt();
        Train train = this;
        do {
            Tuple2<Train, Double> step = train.running2(context, targetSpeed, movement).orElse(null);
            if (step != null) {
                train = step._1;
                movement = step._2;
            }
        } while (train != null && movement > 0);
        return Optional.ofNullable(train);
    }

     */

    @Override
    public String toString() {
        return new StringBuilder(Train.class.getSimpleName())
                .append("[")
                .append(id)
                .append("]")
                .toString();
    }

    /**
     * Returns the waiting for run train status after simulating a time interval
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval
     */
    Optional<Tuple2<Train, Double>> waitingForRun(SimulationContext context, double dt) {
        return Optional.of(Tuple2.of(setSpeed(0), 0d));
    }

    /**
     * Returns the waiting for clear signal train status after simulating a time interval
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval
     */
    Optional<Tuple2<Train, Double>> waitingForSignal(SimulationContext context, double dt) {
        return context.isNextRouteClear(location.getDirection()) ?
                Optional.of(Tuple2.of(start(), dt)) :
                Optional.of(Tuple2.of(this, 0d));
    }

    public static class State {

        private final String id;
        private final Function3<Train, SimulationContext, Double, Optional<Tuple2<Train, Double>>> function;

        /**
         * Creates the trainState
         *
         * @param id       the trainState id
         * @param function the simulation function
         */
        protected State(String id, Function3<Train, SimulationContext, Double, Optional<Tuple2<Train, Double>>> function) {
            this.id = requireNonNull(id);
            this.function = requireNonNull(function);
        }

        Optional<Tuple2<Train, Double>> apply(Train train, SimulationContext context, double dt) {
            return function.apply(train, context, dt);
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return id;
        }
    }







    public static final State ENTERING_STATE = new State("ENTERING", Train::entering);


}
