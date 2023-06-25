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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mmarini.railways2.model.ExtendedPerformance;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mmarini.yaml.Utils.fromFile;
import static org.mmarini.yaml.Utils.objectMapper;
import static org.mmarini.yaml.schema.Validator.*;

/**
 * Handles the configuration by storing and retrieving from file
 */
public class Configuration {

    public static final String VERSION = "1.0";
    public static final Validator VALIDATOR = objectPropertiesRequired(
            Map.of(
                    "version", string(values(VERSION)),
                    "hallOfFame", arrayItems(ExtendedPerformance.VALIDATOR),
                    "userPreferences", UserPreferences.VALIDATOR),
            List.of("version", "hallOfFame", "userPreferences")
    );
    private static final int MAX_ENTRIES = 20;
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    /**
     * Returns the default configuration
     */
    private static Configuration defaultConfig() {
        return new Configuration(
                new UserPreferences(1, false, 0,
                        UIManager.getCrossPlatformLookAndFeelClassName()),
                List.of()
        );
    }

    /**
     * Returns the configuration from json doc
     *
     * @param root    the root
     * @param locator the configuration node locator
     */
    static Configuration fromJson(JsonNode root, Locator locator) {
        VALIDATOR.apply(locator).accept(root);
        List<ExtendedPerformance> hallOfFame1 = locator.path("hallOfFame").elements(root)
                .map(perfLoc -> ExtendedPerformance.fromJson(root, perfLoc))
                .collect(Collectors.toList());
        UserPreferences userOptions = UserPreferences.fromJson(root, locator.path("userPreferences"));
        return new Configuration(userOptions, hallOfFame1);
    }

    /**
     * Returns the config file
     */
    private static File getConfigFile() {
        return new File(System.getProperty("user.home") + File.separator
                + ".railways" + File.separator + "config.yml");
    }

    /**
     * Creates the configuration
     */
    public static Configuration load() {
        try {
            JsonNode root = fromFile(getConfigFile());
            return fromJson(root, Locator.root());
        } catch (Exception e) {
            logger.atError().setCause(e).log("Error loading configuration");
            return defaultConfig();
        }
    }

    private final List<ExtendedPerformance> hallOfFame;
    private final UserPreferences userPreferences;

    /**
     * Creates the configuration
     */
    protected Configuration(UserPreferences userPreferences, List<ExtendedPerformance> hallOfFame) {
        this.userPreferences = userPreferences;
        this.hallOfFame = hallOfFame;
    }

    /**
     * Returns the hall of fame with the new fame
     *
     * @param performance the performance
     */
    public Configuration add(ExtendedPerformance performance) {
        if (isFame(performance)) {
            List<ExtendedPerformance> newHallOfFame = new ArrayList<>(hallOfFame);
            newHallOfFame.add(performance);
            newHallOfFame.sort(ExtendedPerformance::compareTo);
            while (newHallOfFame.size() > MAX_ENTRIES) {
                newHallOfFame.remove(hallOfFame.size() - 1);
            }
            return new Configuration(userPreferences, newHallOfFame).save();
        } else {
            return this;
        }
    }

    /**
     * Returns the hall of fame json node
     */
    private JsonNode createHallOfFameJson() {
        ArrayNode result = objectMapper.createArrayNode();

        hallOfFame.stream()
                .map(ExtendedPerformance::getJson)
                .forEach(result::add);
        return result;
    }

    /**
     * Returns the hall of fame
     */
    public List<ExtendedPerformance> getHallOfFame() {
        return hallOfFame;
    }

    /**
     * Returns the user options
     */
    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    /**
     * Returns the configuration with user options
     *
     * @param userPreferences user options
     */
    Configuration setUserPreferences(UserPreferences userPreferences) {
        return !userPreferences.equals(this.userPreferences)
                ? new Configuration(userPreferences, hallOfFame).save()
                : this;
    }

    /**
     * Returns true if the performance is better than the worst
     *
     * @param performance the performance
     */
    public boolean isFame(ExtendedPerformance performance) {
        return hallOfFame.size() < MAX_ENTRIES
                || performance.compareTo(hallOfFame.get(hallOfFame.size() - 1)) < 0;
    }

    /**
     * Returns the configuration after storing in the file system
     */
    private Configuration save() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(getConfigFile(), toJson());
        } catch (IOException e) {
            logger.atError().setCause(e).log("Error saving configuration");
        }
        return this;
    }

    /**
     * Returns the configuration with gain
     *
     * @param gain the gain (db)
     */
    public Configuration setGain(double gain) {
        return setUserPreferences(userPreferences.setGain(gain));
    }

    /**
     * Returns the configuration with mute
     *
     * @param mute true if mute
     */
    public Configuration setMute(boolean mute) {
        return setUserPreferences(userPreferences.setMute(mute));
    }

    /**
     * Returns the configuration with simulation speed
     *
     * @param simulationSpeed the simulation speed
     */
    public Configuration setSimulationSpeed(double simulationSpeed) {
        return setUserPreferences(userPreferences.setSimulationSpeed(simulationSpeed));
    }

    /**
     * Returns the json node of configuration
     */
    private JsonNode toJson() {
        ObjectNode config = objectMapper.createObjectNode();
        config.put("version", VERSION);
        config.set("userPreferences", userPreferences.toJson());
        config.set("hallOfFame", createHallOfFameJson());
        return config;
    }
}
