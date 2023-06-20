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

import java.util.List;

/**
 * Defines the performance of the game
 */
public class Performance implements Comparable<Performance> {
    private static final double WORST_PERFORMANCE = 0;
    private static final double EPSILON_TIME = 1e-3;
    private static final Performance NONE = new Performance(0, 0, 0, 0, 0, 0, 0, 0);

    public static Performance elapsed(double dt) {
        return new Performance(0, 0, 0, 0, 0, 0, 0, dt);
    }

    /**
     * Returns the none performance
     */
    public static Performance none() {
        return NONE;
    }

    /**
     * Returns the running train performance
     *
     * @param dt time interval (s)
     * @param ds distance interval (m)
     */
    public static Performance running(double dt, double ds) {
        return new Performance(0, 0, 0, dt, ds, 0, 0, dt);
    }

    /**
     * Returns the sum of the performances
     *
     * @param performances the performances
     */
    public static Performance sum(Performance... performances) {
        return sumIterable(List.of(performances));
    }

    /**
     * Returns the sum of the performances
     *
     * @param performances the performances
     */
    public static Performance sumIterable(Iterable<Performance> performances) {
        int trainIncomingNumber = 0;
        int trainRightOutgoingNumber = 0;
        int trainWrongOutgoingNumber = 0;
        double totalTime = 0;
        double traveledDistance = 0;
        int trainStopNumber = 0;
        double trainWaitingTime = 0;
        double elapsedTime = 0;
        for (Performance p : performances) {
            trainIncomingNumber += p.getTrainIncomingNumber();
            trainRightOutgoingNumber += p.getTrainRightOutgoingNumber();
            trainWrongOutgoingNumber += p.getTrainWrongOutgoingNumber();
            totalTime += p.getTotalTime();
            traveledDistance += p.getTraveledDistance();
            trainStopNumber += p.getTrainStopNumber();
            elapsedTime += p.getElapsedTime();
        }
        return new Performance(trainIncomingNumber, trainRightOutgoingNumber, trainWrongOutgoingNumber, totalTime,
                traveledDistance, trainStopNumber, trainWaitingTime, elapsedTime);
    }

    /**
     * Returns the train waiting performance
     *
     * @param dt the time interval
     */
    public static Performance waiting(double dt) {
        return new Performance(0, 0, 0, dt, 0, 0, dt, dt);
    }

    private final int trainIncomingNumber;
    private final int trainRightOutgoingNumber;
    private final int trainWrongOutgoingNumber;
    private final double totalTime;
    private final double traveledDistance;
    private final int trainStopNumber;
    private final double trainWaitingTime;
    private final double elapsedTime;

    /**
     * Creates the performance record
     *
     * @param trainIncomingNumber      the number of incoming trains
     * @param trainRightOutgoingNumber the number of right outgoing trains
     * @param trainWrongOutgoingNumber the number of wrong outgoing trains
     * @param totalTime                the total train time (s)
     * @param traveledDistance         the distance traveled (m)
     * @param trainStopNumber          the number of train stops
     * @param trainWaitingTime         the total train waiting time
     * @param elapsedTime              the elapsed time (s)
     */
    protected Performance(int trainIncomingNumber, int trainRightOutgoingNumber, int trainWrongOutgoingNumber,
                          double totalTime, double traveledDistance,
                          int trainStopNumber, double trainWaitingTime,
                          double elapsedTime) {
        this.trainIncomingNumber = trainIncomingNumber;
        this.trainRightOutgoingNumber = trainRightOutgoingNumber;
        this.trainWrongOutgoingNumber = trainWrongOutgoingNumber;
        this.totalTime = totalTime;
        this.traveledDistance = traveledDistance;
        this.trainStopNumber = trainStopNumber;
        this.trainWaitingTime = trainWaitingTime;
        this.elapsedTime = elapsedTime;
    }

    public Performance add(Performance performance) {
        return sum(this, performance);
    }

    /**
     * Returns the performance with new elapsed time
     *
     * @param time adding elapsed time (s)
     */
    public Performance addElapsedTime(double time) {
        return new Performance(trainIncomingNumber, trainRightOutgoingNumber, trainWrongOutgoingNumber, totalTime, traveledDistance, trainStopNumber, trainWaitingTime, elapsedTime + time);
    }

    /**
     * Returns the performance with new total time
     *
     * @param time the adding time (s)
     */
    public Performance addTotalTime(double time) {
        return new Performance(trainIncomingNumber, trainRightOutgoingNumber, trainWrongOutgoingNumber, totalTime + time, traveledDistance, trainStopNumber, trainWaitingTime, elapsedTime);
    }

    /**
     * Returns the performance with new incoming trains
     *
     * @param trainNumber the number of new trains
     */
    public Performance addTrainIncomingNumber(int trainNumber) {
        return new Performance(trainIncomingNumber + trainNumber, trainRightOutgoingNumber, trainWrongOutgoingNumber, totalTime, traveledDistance, trainStopNumber, trainWaitingTime, elapsedTime);
    }

    /**
     * Returns the performance with new right outgoing trains
     *
     * @param trainNumber the number of new trains
     */
    public Performance addTrainRightOutgoingNumber(int trainNumber) {
        return new Performance(trainIncomingNumber, trainRightOutgoingNumber + trainNumber, trainWrongOutgoingNumber, totalTime, traveledDistance, trainStopNumber, trainWaitingTime, elapsedTime);
    }

    /**
     * Returns the performance with new train stops
     *
     * @param stops number of new stops
     */
    public Performance addTrainStopNumber(int stops) {
        return new Performance(trainIncomingNumber, trainRightOutgoingNumber, trainWrongOutgoingNumber, totalTime, traveledDistance, trainStopNumber + stops, trainWaitingTime, elapsedTime);
    }

    /**
     * Returns the performance with new train waiting time
     *
     * @param time adding waiting time (s)
     */
    public Performance addTrainWaitingTime(double time) {
        return new Performance(trainIncomingNumber, trainRightOutgoingNumber, trainWrongOutgoingNumber, totalTime, traveledDistance, trainStopNumber, trainWaitingTime + time, elapsedTime);
    }

    /**
     * Returns the performance with new wrong outgoing trains
     *
     * @param trainNumber the number of new trains
     */
    public Performance addTrainWrongOutgoingNumber(int trainNumber) {
        return new Performance(trainIncomingNumber, trainRightOutgoingNumber, trainWrongOutgoingNumber + trainNumber, totalTime, traveledDistance, trainStopNumber, trainWaitingTime, elapsedTime);
    }

    /**
     * Returns the performance with new traveled distance
     *
     * @param distance the adding distance (m)
     */
    public Performance addTraveledDistance(double distance) {
        return new Performance(trainIncomingNumber, trainRightOutgoingNumber, trainWrongOutgoingNumber, totalTime, traveledDistance + distance, trainStopNumber, trainWaitingTime, elapsedTime);
    }

    @Override
    public int compareTo(Performance other) {
        double perf1 = getPerformance();
        double perf2 = other.getPerformance();
        if (perf1 < perf2)
            return 1;
        if (perf1 > perf2)
            return -1;
        double freq1 = getFrequency();
        double freq2 = other.getFrequency();
        return Double.compare(freq1, freq2);
    }

    /**
     * Returns the elapsed time (s)
     */
    public double getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Returns the performance with set elapsed time
     *
     * @param elapsedTime elapsed time (s)
     */
    public Performance setElapsedTime(double elapsedTime) {
        return elapsedTime != this.elapsedTime
                ? new Performance(trainIncomingNumber, trainRightOutgoingNumber, trainWrongOutgoingNumber, totalTime, traveledDistance, trainStopNumber, trainWaitingTime, elapsedTime)
                : this;

    }

    /**
     * Returns the frequency of train (#/s)
     */
    public double getFrequency() {
        return trainIncomingNumber / totalTime;
    }

    /**
     * Returns the performance kpi (#/s)
     */
    public double getPerformance() {
        return (totalTime < EPSILON_TIME)
                ? WORST_PERFORMANCE
                : trainRightOutgoingNumber / totalTime;
    }

    /**
     * Returns the number of train in station
     */
    public int getStationTrainCount() {
        return trainIncomingNumber - trainWrongOutgoingNumber
                - trainRightOutgoingNumber;
    }

    /**
     * Returns the total train time (s)
     */
    public double getTotalTime() {
        return totalTime;
    }

    /**
     * Returns the number of incoming trains
     */
    public int getTrainIncomingNumber() {
        return trainIncomingNumber;
    }

    /**
     * Returns the number of right outgoing trains
     */
    public int getTrainRightOutgoingNumber() {
        return trainRightOutgoingNumber;
    }

    /**
     * Returns the number of train stops
     */
    public int getTrainStopNumber() {
        return trainStopNumber;
    }

    /**
     * Returns the total train waiting time (s)
     */
    public double getTrainWaitingTime() {
        return trainWaitingTime;
    }

    /**
     * Returns the number of wrong outgoing trains
     */
    public int getTrainWrongOutgoingNumber() {
        return trainWrongOutgoingNumber;
    }

    /**
     * Returns the distance traveled (m)
     */
    public double getTraveledDistance() {
        return traveledDistance;
    }

    /**
     * Returns the sum of performance
     *
     * @param value the peroformance
     */
    public Performance sumIterable(Performance value) {
        return new Performance(trainIncomingNumber + value.trainIncomingNumber,
                trainRightOutgoingNumber + value.trainRightOutgoingNumber,
                trainWrongOutgoingNumber + value.trainWrongOutgoingNumber,
                totalTime + value.totalTime,
                traveledDistance + value.traveledDistance,
                trainStopNumber + value.trainStopNumber,
                trainWaitingTime + value.trainWaitingTime,
                elapsedTime + value.elapsedTime);
    }
}
