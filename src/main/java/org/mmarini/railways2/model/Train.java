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

import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Route;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

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
        return new Train(id, numCoaches, arrival, destination, ENTERING_STATE,
                ENTRY_TIMEOUT, null, MAX_SPEED, false, 0,
                null, 0);
    }
    private final String id;
    private final int numCoaches;
    private final Entry arrival;
    private final Exit destination;
    private final State state;    public static final State BRAKING_STATE = new State("BRAKING_STATE", Train::braking);
    private final EdgeLocation location;
    private final double arrivalTime;
    private final double speed;
    private final boolean loaded;
    private final double loadedTime;    public static final State WAITING_FOR_SIGNAL_STATE = new State("WAITING_FOR_SIGNAL", Train::waitingForSignal);
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
     *
     * @param context the simulation context
     */
    Optional<Train> braking(SimulationContext context) {
        return running(context, 0)
                .map(t ->
                        (t.getState().equals(Train.WAITING_FOR_SIGNAL_STATE)) ?
                                t.stop() : t);
    }

    /**
     * Returns the simulated entering train
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
            // Checks if train has arrived and stopped
            boolean stopped = arrivalTime <= 0;
            // Computes the speed of train
            double speed = stopped ? 0 : RailwayConstants.MAX_SPEED;
            // Computes the distance in the entry edge
            double movement = context.getDt() * speed;
            // Computes the train location
            Direction dir = arrival.getValidExits().iterator().next();
            EdgeLocation location = new EdgeLocation(dir, dir.getEdge().getLength() - movement);
            return Optional.of(setSpeed(speed).setLocation(location).run());
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
     *
     * @param context the simulation context
     */
    Optional<Train> exiting(SimulationContext context) {
        double dt = context.getDt();
        double newSpeed = speedPhysics(MAX_SPEED, dt);
        double newExitDistance = exitDistance + speed * dt;
        return newExitDistance >= EXIT_DISTANCE + getLength() ?
                Optional.empty() :
                Optional.of(setExitDistance(newExitDistance).setSpeed(newSpeed));
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
     * Returns the length of train (m)
     */
    public double getLength() {
        return numCoaches * COACH_LENGTH;
    }

    /**
     * Returns the location of train head
     */
    public EdgeLocation getLocation() {
        return location;
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
    }

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
    Train setSpeed(double speed) {
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
    Train setState(State state) {
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
        return state.equals(ENTERING_STATE);
    }

    /**
     * Returns true if train is exiting
     */
    public boolean isExiting() {
        return EXITING_STATE.equals(state);
    }

    /**
     * Returns true if the train has loaded the passengers
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Returns the train in load status.
     * Stop the train and set the trainState to wait for loaded
     *
     * @param time the time
     */
    Train load(double time) {
        return setSpeed(0)
                .setLoadedTime(time + LOADING_TIME)
                .setState(Train.LOADING_STATE);
    }

    /**
     * Returns the loading train status after simulating a time interval
     *
     * @param context the simulation context
     */
    Optional<Train> loading(SimulationContext context) {
        return Optional.of(context.getNextTime() < loadedTime ? this : stop().setLoaded());
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
    Optional<Train> running(SimulationContext context) {
        return running(context, MAX_SPEED);
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
     * @param targetSpeed the target speed (m/s)
     */
    Optional<Train> running(SimulationContext context, double targetSpeed) {
        boolean clearTrack = context.isNextTracksClear(this);
        if (clearTrack) {
            // Move head
            double movement = speed * context.getDt();
            // Computes the new location in the edge
            double newDistance = location.getDistance() - movement;
            // Computes the new speed
            double newSpeed = speedPhysics(targetSpeed, context.getDt());
            if (newDistance < 0) {
                // end of edge reached
                Node destination1 = location.getDirection().getDestination();
                Route route = context.getRoute(destination1);
                if (route instanceof Exit) {
                    return Optional.of(setState(EXITING_STATE)
                            .setLocation(null)
                            .setExitingNode((Exit) route)
                            .setExitDistance(-newDistance));
                } else {
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
            } else {
                // In track
                EdgeLocation newLocation = location.setDistance(newDistance);
                if (targetSpeed > APPROACH_SPEED || newSpeed > APPROACH_SPEED) {
                    return Optional.of(setLocation(newLocation).setSpeed(max(newSpeed, APPROACH_SPEED)));
                } else {
                    // Train is braking
                    return Optional.of(setLocation(newLocation).setSpeed(newSpeed).setState(WAITING_FOR_RUN_STATE));
                }
            }
        } else {
            // brake train
            double newDistance = location.getDistance() - context.getDt() * speed;
            if (newDistance < 0) {
                // Stop train
                EdgeLocation newLocation = location.setDistance(0);
                return Optional.of((location.getDirection().getEdge() instanceof Platform && !loaded) ?
                        setSpeed(0).setState(Train.LOADING_STATE).setLocation(newLocation) :
                        setSpeed(0).setState(Train.WAITING_FOR_SIGNAL_STATE).setLocation(newLocation));
            } else {
                // decelerate train
                double newSpeed = max(speedPhysics(0, context.getDt()), APPROACH_SPEED);
                EdgeLocation newLocation = location.setDistance(newDistance);
                return Optional.of(setLocation(newLocation).setSpeed(newSpeed));
            }
        }
    }

    /**
     * Returns the train in loaded state
     */
    public Train setLoaded() {
        return loaded ? this :
                new Train(id, numCoaches, arrival, destination, state, arrivalTime, location, speed, true, loadedTime, exitingNode, exitDistance);
    }

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
    }    public static final State RUNNING_STATE = new State("RUNNING", Train::running);

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
     * @param context the simulation context
     */
    public Optional<Train> tick(SimulationContext context) {
        return state.apply(this, context);
    }

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
     *
     * @param context the simulation context
     */
    Optional<Train> waitingForRun(SimulationContext context) {
        return Optional.of(setSpeed(0));
    }

    /**
     * Returns the waiting for clear signal train status after simulating a time interval
     *
     * @param context the simulation context
     */
    Optional<Train> waitingForSignal(SimulationContext context) {
        return Optional.of(context.isNextRouteClear(location.getDirection()) ? start() : this);
    }

    protected static class State {

        private final String id;
        private final BiFunction<Train, SimulationContext, Optional<Train>> function;

        /**
         * Creates the trainState
         *
         * @param id       the trainState id
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








    public static final State ENTERING_STATE = new State("ENTERING", Train::entering);


}
