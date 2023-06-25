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

package org.mmarini.railways2.swing;

import org.mmarini.railways2.model.SoundEvent;
import org.mmarini.railways2.model.StationStatus;
import org.reactivestreams.Subscriber;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;
import static org.mmarini.railways2.model.RailwayConstants.SPH;
import static org.mmarini.railways2.model.RailwayConstants.SPM;

/**
 * Defines the game options
 */
public class GameOptions {
    private final String stationResource;
    private final double gameDuration;
    private final double trainFrequency;

    /**
     * Creates the game options
     *
     * @param stationResource the station resource
     * @param gameDuration    the game duration (min)
     * @param trainFrequency  the train frequency (#/h)
     */
    public GameOptions(String stationResource, double gameDuration, double trainFrequency) {
        this.stationResource = requireNonNull(stationResource);
        this.gameDuration = gameDuration;
        this.trainFrequency = trainFrequency;
    }

    /**
     * Returns the station status of game options
     *
     * @param random the random generator
     * @param events the event subscriber
     */
    public Optional<StationStatus> createStatus(Random random, Subscriber<SoundEvent> events) {
        return GameDialog.loadStation(stationResource, gameDuration * SPM, trainFrequency / SPH, random, events);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameOptions that = (GameOptions) o;
        return Double.compare(that.gameDuration, gameDuration) == 0 && Double.compare(that.trainFrequency, trainFrequency) == 0 && stationResource.equals(that.stationResource);
    }

    /**
     * Returns the game duration (min)
     */
    public double getGameDuration() {
        return gameDuration;
    }

    /**
     * Returns the station resource
     */
    public String getStationResource() {
        return stationResource;
    }

    /**
     * Returns the train frequency (#/h)
     */
    public double getTrainFrequency() {
        return trainFrequency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationResource, gameDuration, trainFrequency);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GameOptions.class.getSimpleName() + "[", "]")
                .add("stationId='" + stationResource + "'")
                .add("gameDuration=" + gameDuration)
                .add("trainFrequency=" + trainFrequency)
                .toString();
    }
}
