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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mmarini.Function4;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Route;
import org.mmarini.railways2.model.routes.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mmarini.railways2.model.RailwayConstants.*;
import static org.mmarini.railways2.model.SoundEvent.ARRIVED;
import static org.mmarini.railways2.model.SoundEvent.STOPPED;
import static org.mmarini.yaml.Utils.objectMapper;


/**
 * Identifies the train, locates it, moves it in the dispatched routes
 */
public class Train {

    public static final State STATE_EXITING = new State("EXITING", Train::exiting);
    public static final State STATE_WAITING_FOR_RUN = new State("WAITING_FOR_RUN", Train::waitingForRun);
    public static final State STATE_LOADING = new State("LOADING", Train::loading);
    private static final int MAX_ITERATIONS = 5;

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
                STATE_ENTERING, ENTRY_TIMEOUT, null, MAX_SPEED, false, 0,
                null, 0);
    }

    /**
     * Returns the train state from the identifier
     *
     * @param id the identifier
     */
    public static State getState(String id) {
        Optional<State> result = STATES.stream().filter(s -> s.getId().equals(id))
                .findAny();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(format("State %s not found", id));
        }
        return result.orElseThrow();
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
     * Returns the braking train
     */
    public Train brake() {
        return setState(Train.STATE_BRAKING);
    }

    /**
     * Returns the braking train status after simulating a time interval
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval (s)
     */
    Tuple2<Optional<Train>, Performance> braking(SimulationContext context, double t0, double dt) {
        Tuple2<Optional<Train>, Performance> result = running(context, t0, dt, 0);
        Optional<Train> newTrain = result._1
                .map(train -> {
                    if (train.getState().equals(Train.STATE_WAITING_FOR_SIGNAL)) {
                        context.play(STOPPED);
                        return train.stop();
                    } else {
                        return train;
                    }
                });
        return result.setV1(newTrain);
    }

    /**
     * Returns the train status after simulating a time interval
     * and the remaining time interval
     *
     * @param ctx the simulation context
     * @param t0  the current time instant (s)
     * @param dt  the time interval (s)
     */
    Tuple2<Optional<Train>, Performance> changeState(SimulationContext ctx, double t0, double dt) {
        return state.apply(this, ctx, t0, dt);
    }

    /**
     * Returns the simulated entering train
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval
     */
    Tuple2<Optional<Train>, Performance> entering(SimulationContext context, double t0, double dt) {
        double timeToArrive = arrivalTime - t0;
        if (timeToArrive > 0 && timeToArrive < MIN_TIME_INTERVAL) {
            timeToArrive = MIN_TIME_INTERVAL;
        }
        if (timeToArrive > dt) {
            // Train not yet arrived
            return Tuple2.of(Optional.of(this), Performance.elapsed(dt));
        }
        if (!context.isEntryClear(this)) {
            // entry busy
            return timeToArrive >= 0 || speed != 0
                    ? Tuple2.of(Optional.of(setSpeed(0)),
                    Performance.elapsed(timeToArrive).addTrainStopNumber(1))
                    : Tuple2.of(Optional.of(this),
                    Performance.waiting(dt));
        }
        // Entry clear and this is the first train in the queue
        // Enters the edge
        Direction dir = arrival.getValidExits().iterator().next();
        EdgeLocation location = new EdgeLocation(dir, dir.getEdge().getLength());
        return Tuple2.of(
                Optional.of(setLocation(location).run()),
                Performance.elapsed(max(0, timeToArrive)));
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
     * @param exit the exit route
     */
    Train exit(Exit exit) {
        return setState(STATE_EXITING).setExitingNode(exit).setExitDistance(37.5);
    }

    /**
     * Returns the exiting train status after simulating a time interval
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval
     */
    Tuple2<Optional<Train>, Performance> exiting(SimulationContext context, double t0, double dt) {
        double newSpeed = speedPhysics(MAX_SPEED, dt);
        double ds = speed * dt;
        double distanceToExit = EXIT_DISTANCE + getLength() - exitDistance;
        boolean left = ds >= distanceToExit;
        if (left) {
            double exitTime = distanceToExit / speed;
            if (exitingNode.equals(destination)) {
                return Tuple2.of(Optional.empty(),
                        Performance.running(exitTime, distanceToExit).addTrainRightOutgoingNumber(1));
            } else {
                return Tuple2.of(Optional.empty(),
                        Performance.running(exitTime, distanceToExit).addTrainWrongOutgoingNumber(1));
            }
        }
        double newExitDistance = exitDistance + ds;
        return Tuple2.of(Optional.of(setExitDistance(newExitDistance).setSpeed(newSpeed)),
                Performance.running(dt, ds));
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
    }    public static final State STATE_RUNNING = new State("RUNNING", Train::running);

    public Train setExitDistance(double exitDistance) {
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
     * Returns the dump json node
     */
    public JsonNode getJson() {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", this.id);
        result.put("numCoaches", this.numCoaches);
        result.put("arrival", this.arrival.getId());
        result.put("destination", this.destination.getId());
        result.put("state", state.getId());
        result.put("arrivalTime", this.arrivalTime);
        result.put("loadedTime", this.loadedTime);
        result.put("loaded", this.loaded);
        result.set("location", Optional.ofNullable(location).map(EdgeLocation::getJsonDump).orElse(null));
        result.put("speed", this.speed);
        result.put("exitingNode", Optional.ofNullable(exitingNode).map(Exit::getId).orElse(null));
        result.put("exitDistance", this.exitDistance);
        return result;
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
        return STATE_ENTERING.equals(state);
    }

    /**
     * Returns true if train is exiting
     */
    public boolean isExiting() {
        return STATE_EXITING.equals(state);
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
                .setState(Train.STATE_LOADING);
    }

    /**
     * Returns the loading train status after simulating a time interval
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval
     */
    Tuple2<Optional<Train>, Performance> loading(SimulationContext context, double t0, double dt) {
        double timeToLoad = loadedTime - t0;
        if (timeToLoad > 0 && timeToLoad < MIN_TIME_INTERVAL) {
            timeToLoad = MIN_TIME_INTERVAL;
        }
        if (dt <= timeToLoad) {
            // Wait for loaded
            return Tuple2.of(Optional.of(this), Performance.waiting(dt));

        } else {
            // Load completed
            context.play(STOPPED);
            return Tuple2.of(Optional.of(stop().setLoaded()),
                    Performance.waiting(timeToLoad));
        }
    }

    /**
     * Returns the train in running state
     */
    Train run() {
        return setState(STATE_RUNNING);
    }

    /**
     * Returns the running fast train status after simulating a time interval
     *
     * @param context the simulation context
     */
    Tuple2<Optional<Train>, Performance> running(SimulationContext context, double t0, double dt) {
        return running(context, t0, dt, MAX_SPEED);
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
     * @param dt          the time interval
     * @param targetSpeed the target speed (m/s)
     */
    Tuple2<Optional<Train>, Performance> running(SimulationContext context, double t0, double dt, double targetSpeed) {
        boolean clearTrack = context.isNextTracksClear(this);
        double newSpeed = clearTrack ?
                speedPhysics(targetSpeed, dt)
                : max(speedPhysics(0, dt), APPROACH_SPEED);
        if (speed == 0) {
            if (targetSpeed > APPROACH_SPEED || newSpeed > APPROACH_SPEED) {
                return Tuple2.of(Optional.of(
                                setSpeed(max(APPROACH_SPEED, newSpeed))),
                        Performance.running(dt, 0));
            } else {
                context.play(STOPPED);
                return Tuple2.of(Optional.of(stop()),
                        Performance.none().addTrainStopNumber(1));
            }
        }

        double timeToEndEdge = location.getDistance() / speed;
        Direction direction = location.getDirection();
        Edge edge = direction.getEdge();
        Node destination = direction.getDestination();
        Route route = context.getRoute(destination);
        double movement = speed * dt;
        double newDistance = location.getDistance() - movement;

        if (dt < timeToEndEdge) {
            // Move ahead in the current edge
            EdgeLocation newLocation = location.setDistance(newDistance);
            if (targetSpeed > APPROACH_SPEED || newSpeed > APPROACH_SPEED) {
                return Tuple2.of(Optional.of(
                                setLocation(newLocation)
                                        .setSpeed(max(newSpeed, APPROACH_SPEED))),
                        Performance.running(dt, movement));
            } else {
                // Train is braking
                context.play(STOPPED);
                return Tuple2.of(Optional.of(
                                setLocation(newLocation).stop()),
                        Performance.running(dt, movement).addTrainStopNumber(1));
            }
        }

        // end of edge reached
        if (route instanceof Exit) {
            // exit node reached
            return Tuple2.of(Optional.of(
                            setState(STATE_EXITING)
                                    .setLocation(null)
                                    .setExitingNode((Exit) route)
                                    .setExitDistance(0)),
                    Performance.running(timeToEndEdge, location.getDistance()));
        }
        if (edge instanceof Platform && !loaded) {
            // platform reached and train not loaded
            context.play(ARRIVED);
            return Tuple2.of(Optional.of(
                            setState(STATE_LOADING)
                                    .setLocation(location.setDistance(0))
                                    .load(t0 + timeToEndEdge)),
                    Performance.running(timeToEndEdge, location.getDistance()).addTrainStopNumber(1));
        }
        if (!context.isNextRouteClear(direction)) {
            // next route is not clear (stop signal)
            return Tuple2.of(Optional.of(setState(STATE_WAITING_FOR_SIGNAL)
                            .setLocation(location.setDistance(0))
                            .setSpeed(0)),
                    Performance.running(timeToEndEdge, location.getDistance())
                            .addTrainStopNumber(1));
        }
        // Get the new direction
        Optional<Direction> routeDirection = context.getNextExit(location.getDirection());
        return routeDirection.map(newDir -> {
            Edge newEdge = newDir.getEdge();
            // Computes the new location
            EdgeLocation newLocation = new EdgeLocation(newDir, newEdge.getLength());
            // Lock signals of new section
            if (route instanceof Signal && context.isAutolock()) {
                context.lockSignals(newDir);
            }
            return Tuple2.of(Optional.of(setSpeed(newSpeed).setLocation(newLocation)),
                    Performance.running(timeToEndEdge, location.getDistance()));
        }).orElseThrow();
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
    }    public static final State STATE_ENTERING = new State("ENTERING", Train::entering);

    /**
     * Returns the train started
     */
    private Train start() {
        return setState(Train.STATE_RUNNING);
    }

    /**
     * Returns the stopped train
     */
    Train stop() {
        return setSpeed(0).setState(Train.STATE_WAITING_FOR_RUN);
    }

    /**
     * Returns the train status after simulating a time interval
     *
     * @param ctx the simulation context
     * @param dt  the time interval (s)
     */
    public Tuple2<Optional<Train>, Performance> tick(SimulationContext ctx, double dt) {
        Train train = this;
        List<Performance> performances = new ArrayList<>();
        int n = 0;
        double t0 = ctx.getTime();
        do {
            ++n;
            // Computes the next transition
            Tuple2<Optional<Train>, Performance> transition = train.changeState(ctx, t0, dt);
            train = transition._1.orElse(null);
            Performance performance = transition._2;
            performances.add(performance);
            double elapsedTime = performance.getElapsedTime();
            dt -= elapsedTime;
            t0 += dt;
            if (n > MAX_ITERATIONS) {
                throw new IllegalStateException(
                        format("Too iterations n=%d elapsedTime=%g",
                                n, elapsedTime));
            }
        } while (train != null && dt > 0);
        return Tuple2.of(Optional.ofNullable(train), Performance.sumIterable(performances));
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
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval
     */
    Tuple2<Optional<Train>, Performance> waitingForRun(SimulationContext context, double t0, double dt) {
        return Tuple2.of(
                Optional.of(setSpeed(0)),
                Performance.waiting(dt)
        );
    }

    /**
     * Returns the waiting for clear signal train status after simulating a time interval
     * and the remaining time interval
     *
     * @param context the simulation context
     * @param dt      the time interval
     */
    Tuple2<Optional<Train>, Performance> waitingForSignal(SimulationContext context, double t0, double dt) {
        if (context.isNextRouteClear(location.getDirection())) {
            return Tuple2.of(
                    Optional.of(start()),
                    Performance.none()
            );
        } else {
            return Tuple2.of(Optional.of(this), Performance.waiting(dt));
        }
    }

    public static class State {
        private final String id;
        private final Function4<Train, SimulationContext, Double, Double, Tuple2<Optional<Train>, Performance>> function;

        /**
         * Creates the trainState
         *
         * @param id       the trainState id
         * @param function the simulation function (train, context, t0, dt)
         */
        protected State(String id, Function4<Train, SimulationContext, Double, Double, Tuple2<Optional<Train>, Performance>> function) {
            this.id = requireNonNull(id);
            this.function = requireNonNull(function);
        }

        /**
         * Returns the new train after simulation interval and the performance indicators
         *
         * @param train   the train
         * @param context the simulation context
         * @param t0      the t0 instant (s)
         * @param dt      the time interval (s)
         */
        Tuple2<Optional<Train>, Performance> apply(Train train, SimulationContext context, double t0, double dt) {
            return function.apply(train, context, t0, dt);
        }

        /**
         * Returns the state identifier
         */
        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return id;
        }
    }






    public static final State STATE_WAITING_FOR_SIGNAL = new State("WAITING_FOR_SIGNAL", Train::waitingForSignal);


    public static final State STATE_BRAKING = new State("BRAKING", Train::braking);

    public static final List<State> STATES = List.of(
            STATE_ENTERING,
            STATE_RUNNING,
            STATE_LOADING,
            STATE_EXITING,
            STATE_BRAKING,
            STATE_WAITING_FOR_RUN,
            STATE_WAITING_FOR_SIGNAL);

}