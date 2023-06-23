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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;
import static org.mmarini.yaml.Utils.objectMapper;
import static org.mmarini.yaml.schema.Validator.*;

/**
 * Defines the user options
 */
public class UserPreferences {
    public static final Validator VALIDATOR = objectPropertiesRequired(
            Map.of(
                    "simulationSpeed", nonNegativeNumber(),
                    "mute", booleanValue(),
                    "gain", number(),
                    "lookAndFeelClass", string()
            ), List.of()
    );

    /**
     * Returns the user options from json document
     *
     * @param root    the rott
     * @param locator the user options locator
     */
    public static UserPreferences fromJson(JsonNode root, Locator locator) {
        VALIDATOR.apply(locator).accept(root);
        double simulationSpeed1 = locator.path("simulationSpeed").getNode(root).asDouble();
        boolean mute1 = locator.path("mute").getNode(root).asBoolean();
        double gain1 = locator.path("gain").getNode(root).asDouble();
        String lookAndFeelClass1 = locator.path("lookAndFeelClass").getNode(root).asText();
        return new UserPreferences(simulationSpeed1, mute1, gain1, lookAndFeelClass1);
    }

    private final String lookAndFeelClass;
    private final double simulationSpeed;
    private final boolean mute;
    private final double gain;

    /**
     * Creates the user options
     *
     * @param simulationSpeed  the simulation speed
     * @param mute             true if game mute
     * @param gain             the sound gain value (db)
     * @param lookAndFeelClass the look and feel class name
     */
    public UserPreferences(double simulationSpeed, boolean mute, double gain, String lookAndFeelClass) {
        this.lookAndFeelClass = requireNonNull(lookAndFeelClass);
        this.simulationSpeed = simulationSpeed;
        this.mute = mute;
        this.gain = gain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPreferences that = (UserPreferences) o;
        return Double.compare(that.simulationSpeed, simulationSpeed) == 0 && mute == that.mute && Double.compare(that.gain, gain) == 0 && lookAndFeelClass.equals(that.lookAndFeelClass);
    }

    /**
     * Returns the sound volume level (db)
     */
    public double getGain() {
        return gain;
    }

    /**
     * Returns the user option with gain value
     *
     * @param gain the gain value
     */
    public UserPreferences setGain(double gain) {
        return gain != this.gain
                ? new UserPreferences(simulationSpeed, mute, gain, lookAndFeelClass)
                : this;
    }

    /**
     * Returns the look and feel
     */
    public String getLookAndFeelClass() {
        return lookAndFeelClass;
    }

    /**
     * Returns the simulation speed
     */
    public double getSimulationSpeed() {
        return simulationSpeed;
    }

    /**
     * Returns the user option with simulation speed
     *
     * @param simulationSpeed the simulation speed
     */
    public UserPreferences setSimulationSpeed(double simulationSpeed) {
        return simulationSpeed != this.simulationSpeed
                ? new UserPreferences(simulationSpeed, mute, gain, lookAndFeelClass)
                : this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lookAndFeelClass, simulationSpeed, mute, gain);
    }

    /**
     * Returns true if sound is muted
     */
    public boolean isMute() {
        return mute;
    }

    /**
     * Returns the user option with mute
     *
     * @param mute true if sound is muted
     */
    public UserPreferences setMute(boolean mute) {
        return mute != this.mute
                ? new UserPreferences(simulationSpeed, mute, gain, lookAndFeelClass)
                : this;
    }

    /**
     * Returns the json node
     */
    public JsonNode toJson() {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("simulationSpeed", simulationSpeed);
        result.put("mute", mute);
        result.put("gain", gain);
        result.put("lookAndFeelClass", lookAndFeelClass);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserPreferences.class.getSimpleName() + "[", "]")
                .add("simulationSpeed=" + simulationSpeed)
                .add("mute=" + mute)
                .add("gain=" + gain)
                .add("lookAndFeelClass='" + lookAndFeelClass + "'")
                .toString();
    }
}
