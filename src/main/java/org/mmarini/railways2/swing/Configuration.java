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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mmarini.yaml.Utils.fromFile;
import static org.mmarini.yaml.Utils.objectMapper;
import static org.mmarini.yaml.schema.Validator.arrayItems;
import static org.mmarini.yaml.schema.Validator.objectPropertiesRequired;

/**
 * Handles the configuration by storing and retrieving from file
 */
public class Configuration {
    public static final Validator VALIDATOR = objectPropertiesRequired(
            Map.of("hallOfFame", arrayItems(ExtendedPerformance.VALIDATOR)),
            List.of("hallOfFame")
    );
    private static final int MAX_ENTRIES = 20;
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    /**
     * Returns the config file
     */
    private static File getConfigFile() {
        return new File(System.getProperty("user.home") + File.separator
                + ".railways" + File.separator + "config.yml");
    }

    private final List<ExtendedPerformance> hallOfFame;

    /**
     * Creates the configuration
     */
    public Configuration() {
        this.hallOfFame = new ArrayList<>(MAX_ENTRIES + 1);
        load();
    }

    /**
     * Returns the hall of fame with the new fame
     *
     * @param performance the performance
     */
    public Configuration add(ExtendedPerformance performance) {
        if (isFame(performance)) {
            hallOfFame.add(performance);
            hallOfFame.sort(ExtendedPerformance::compareTo);
            while (hallOfFame.size() > MAX_ENTRIES) {
                hallOfFame.remove(hallOfFame.size() - 1);
            }
            save();
        }
        return this;
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
     * Returns the json node of configuration
     */
    private JsonNode createJson() {
        ObjectNode config = objectMapper.createObjectNode();
        config.set("hallOfFame", createHallOfFameJson());
        return config;
    }

    /**
     * Returns the hall of fame
     */
    public List<ExtendedPerformance> getHallOfFame() {
        return hallOfFame;
    }

    /**
     * Returns true if the performance is batter than the worst
     *
     * @param performance the performance
     */
    public boolean isFame(ExtendedPerformance performance) {
        return hallOfFame.size() < MAX_ENTRIES
                || performance.compareTo(hallOfFame.get(hallOfFame.size() - 1)) < 0;
    }

    /**
     * Loads the configuration from json doc
     *
     * @param root    the root
     * @param locator the configuration node locator
     */
    private void load(JsonNode root, Locator locator) {
        VALIDATOR.apply(locator).accept(root);
        hallOfFame.clear();
        locator.path("hallOfFame").elements(root)
                .map(perfLoc -> ExtendedPerformance.fromJson(root, perfLoc))
                .forEach(hallOfFame::add);
    }

    /**
     * Loads the configuration from file system
     */
    private void load() {
        try {
            JsonNode root = fromFile(getConfigFile());
            load(root, Locator.root());
        } catch (Exception e) {
            logger.atError().setCause(e).log("Error loading configuration");
        }
    }

    /**
     * Store the configuration in the file system
     */
    private void save() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(getConfigFile(), createJson());
        } catch (IOException e) {
            logger.atError().setCause(e).log("Error saving configuration");
        }
    }
}
