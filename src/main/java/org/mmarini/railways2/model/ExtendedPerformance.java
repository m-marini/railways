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
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;
import static org.mmarini.yaml.Utils.objectMapper;
import static org.mmarini.yaml.schema.Validator.*;

/**
 * Defines the performance of the game
 */
public class ExtendedPerformance extends Performance {
    /**
     * Json validator
     */
    public static final Validator VALIDATOR = objectPropertiesRequired(
            Map.ofEntries(
                    Map.entry("stationId", string()),
                    Map.entry("player", string()),
                    Map.entry("gameDuration", positiveNumber()),
                    Map.entry("timestamp", positiveInteger()),
                    Map.entry("incomingTrainNumber", nonNegativeInteger()),
                    Map.entry("rightOutgoingTrainNumber", nonNegativeInteger()),
                    Map.entry("wrongOutgoingTrainNumber", nonNegativeInteger()),
                    Map.entry("totalTrainTime", nonNegativeNumber()),
                    Map.entry("traveledDistance", nonNegativeNumber()),
                    Map.entry("trainStopNumber", nonNegativeInteger()),
                    Map.entry("trainWaitingTime", nonNegativeNumber()),
                    Map.entry("elapsedTime", nonNegativeNumber())),
            List.of("stationId",
                    "player",
                    "gameDuration",
                    "timestamp",
                    "incomingTrainNumber",
                    "rightOutgoingTrainNumber",
                    "wrongOutgoingTrainNumber",
                    "totalTrainTime",
                    "traveledDistance",
                    "trainStopNumber",
                    "trainWaitingTime",
                    "elapsedTime"
            ));

    /**
     * Returns the extended performance
     *
     * @param stationId    the station id
     * @param gameDuration the game duration (s)
     */
    public static ExtendedPerformance create(String stationId, double gameDuration) {
        return new ExtendedPerformance(stationId, gameDuration, "", System.currentTimeMillis(), 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Returns the performance from json node
     *
     * @param root    the json node
     * @param locator the locator of node
     */
    public static ExtendedPerformance fromJson(JsonNode root, Locator locator) {
        VALIDATOR.apply(locator).accept(root);
        String stationId1 = locator.path("stationId").getNode(root).asText();
        double gameDuration1 = locator.path("gameDuration").getNode(root).asDouble();
        String player1 = locator.path("player").getNode(root).asText();
        long timestamp1 = locator.path("timestamp").getNode(root).asLong();
        int incomingTrainNumber = locator.path("incomingTrainNumber").getNode(root).asInt();
        int rightTrainOutgoingNumber = locator.path("rightOutgoingTrainNumber").getNode(root).asInt();
        int wrongTrainOutgoingNumber = locator.path("wrongOutgoingTrainNumber").getNode(root).asInt();
        double totalTrainTime = locator.path("totalTrainTime").getNode(root).asDouble();
        double traveledDistance1 = locator.path("traveledDistance").getNode(root).asDouble();
        int trainStopNumber1 = locator.path("trainStopNumber").getNode(root).asInt();
        double trainWaitingTime1 = locator.path("trainWaitingTime").getNode(root).asDouble();
        double elapsedTime1 = locator.path("elapsedTime").getNode(root).asDouble();
        return new ExtendedPerformance(stationId1, gameDuration1, player1, timestamp1,
                incomingTrainNumber, rightTrainOutgoingNumber, wrongTrainOutgoingNumber,
                totalTrainTime, traveledDistance1, trainStopNumber1,
                trainWaitingTime1, elapsedTime1);
    }

    private final String player;
    private final String stationId;
    private final double gameDuration;
    private final long timestamp;

    /**
     * Creates the performance record
     *
     * @param stationId                the station identifier
     * @param gameDuration             the game duration (s)
     * @param player                   the player
     * @param timestamp                the timestamp
     * @param trainIncomingNumber      the number of incoming trains
     * @param trainRightOutgoingNumber the number of right outgoing trains
     * @param trainWrongOutgoingNumber the number of wrong outgoing trains
     * @param totalTime                the total train time (s)
     * @param traveledDistance         the distance traveled (m)
     * @param trainStopNumber          the number of train stops
     * @param trainWaitingTime         the total train waiting time
     * @param elapsedTime              the elapsed time (s)
     */
    protected ExtendedPerformance(String stationId, double gameDuration, String player, long timestamp, int trainIncomingNumber, int trainRightOutgoingNumber, int trainWrongOutgoingNumber,
                                  double totalTime, double traveledDistance,
                                  int trainStopNumber, double trainWaitingTime,
                                  double elapsedTime) {
        super(trainIncomingNumber, trainRightOutgoingNumber, trainWrongOutgoingNumber,
                totalTime, traveledDistance,
                trainStopNumber, trainWaitingTime,
                elapsedTime);
        this.stationId = requireNonNull(stationId);
        this.gameDuration = gameDuration;
        this.player = requireNonNull(player);
        this.timestamp = timestamp;
    }

    @Override
    public ExtendedPerformance add(Performance performance) {
        return setPerformance(performance.add(this));
    }

    @Override
    public ExtendedPerformance addElapsedTime(double dt) {
        return new ExtendedPerformance(stationId, gameDuration, player, timestamp,
                incomingTrainNumber, rightOutgoingTrainNumber, wrongOutgoingTrainNumber,
                totalTrainTime, traveledDistance, trainStopNumber,
                trainWaitingTime, elapsedTime + dt);
    }

    @Override
    public ExtendedPerformance addTrainIncomingNumber(int trainNumber) {
        return new ExtendedPerformance(stationId, gameDuration, player, timestamp,
                incomingTrainNumber + trainNumber, rightOutgoingTrainNumber, wrongOutgoingTrainNumber,
                totalTrainTime, traveledDistance, trainStopNumber,
                trainWaitingTime, elapsedTime);
    }

    @Override
    public int compareTo(Performance other) {
        int compare = super.compareTo(other);
        if (compare != 0) {
            return compare;
        }
        return (other instanceof ExtendedPerformance)
                ? Long.compare(timestamp, ((ExtendedPerformance) other).timestamp)
                : compare;
    }

    /**
     * Returns the game duration (s)
     */
    public double getGameDuration() {
        return gameDuration;
    }

    /**
     * Returns the json node
     */
    public JsonNode getJson() {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("player", getPlayer());
        result.put("stationId", getStationId());
        result.put("timestamp", getTimestamp());
        result.put("gameDuration", getGameDuration());
        result.put("incomingTrainNumber", getIncomingTrainNumber());
        result.put("rightOutgoingTrainNumber", getRightOutgoingTrainNumber());
        result.put("wrongOutgoingTrainNumber", getWrongOutgoingTrainNumber());
        result.put("trainStopNumber", getTrainStopNumber());
        result.put("totalTrainTime", getTotalTrainTime());
        result.put("traveledDistance", getTraveledDistance());
        result.put("elapsedTime", getElapsedTime());
        result.put("trainWaitingTime", getTrainWaitingTime());
        return result;
    }

    /**
     * Returns the player
     */
    public String getPlayer() {
        return player;
    }

    /**
     * Returns the performance with new player
     *
     * @param player the player
     */
    public ExtendedPerformance setPlayer(String player) {
        return !player.equals(this.player)
                ? new ExtendedPerformance(stationId, gameDuration, player, timestamp,
                incomingTrainNumber, rightOutgoingTrainNumber, wrongOutgoingTrainNumber,
                totalTrainTime, traveledDistance, trainStopNumber,
                trainWaitingTime, elapsedTime)
                : this;
    }

    /**
     * Returns the station id
     */
    public String getStationId() {
        return stationId;
    }

    /**
     * Returns the time stamp (ms)
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns true if the game is finished
     */
    public boolean isGameFinished() {
        return getElapsedTime() >= gameDuration;
    }

    @Override
    public ExtendedPerformance setElapsedTime(double elapsedTime) {
        return new ExtendedPerformance(stationId, gameDuration, player, timestamp,
                incomingTrainNumber, rightOutgoingTrainNumber, wrongOutgoingTrainNumber,
                totalTrainTime, traveledDistance, trainStopNumber,
                trainWaitingTime, elapsedTime);
    }

    /**
     * Returns the extended performance with performance set kpi
     *
     * @param performance the performance kpi
     */
    public ExtendedPerformance setPerformance(Performance performance) {
        return new ExtendedPerformance(stationId, gameDuration, player, timestamp,
                performance.incomingTrainNumber, performance.rightOutgoingTrainNumber, performance.wrongOutgoingTrainNumber,
                performance.totalTrainTime, performance.traveledDistance, performance.trainStopNumber,
                performance.trainWaitingTime, performance.elapsedTime);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ExtendedPerformance.class.getSimpleName() + "[", "]")
                .add("stationId='" + stationId + "'")
                .add("gameDuration=" + gameDuration)
                .add("timestamp=" + timestamp)
                .add("trainIncomingNumber=" + incomingTrainNumber)
                .add("trainRightOutgoingNumber=" + rightOutgoingTrainNumber)
                .add("trainWrongOutgoingNumber=" + wrongOutgoingTrainNumber)
                .add("totalTime=" + totalTrainTime)
                .add("traveledDistance=" + traveledDistance)
                .add("trainStopNumber=" + trainStopNumber)
                .add("trainWaitingTime=" + trainWaitingTime)
                .add("elapsedTime=" + elapsedTime)
                .toString();
    }
}
