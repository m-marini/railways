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

import org.mmarini.Tuple2;
import org.mmarini.railways2.model.SimulatorEngineImpl;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.blocks.BlockStationBuilder;
import org.mmarini.railways2.model.blocks.StationDef;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.util.Random;
import java.util.function.Function;

import static org.mmarini.railways2.model.RailwayConstants.ENTRY_TIMEOUT;

class TrainPanelTest {
    public static final int SIMULATION_SEED = 1234;
    public static final double GAME_DURATION = 300d;
    public static final double FREQUENCY = 0.1;
    private static final Logger logger = LoggerFactory.getLogger(TrainPanelTest.class);
    private static final Dimension DEFAULT_SIZE = new Dimension(800, 600);
    private static final int FPS = 60;

    public static void main(String[] args) throws IOException {
        /*
        StationStatus status1 = new WithTrain(StationExamples.createSwitchStation())
                .addTrain(10, "a", "e", "ab", "b", 0)
                .addTrain(3, "a", "f", "bc", "c", 0)
                .build();
        StationStatus status2 = new WithTrain(StationExamples.create2CrossExitStation(true))
                .addTrain(10, "a", "e", "ab", "b", 0)
                .addTrain(3, "a", "j", "gh", "h", 0)
                .build();

        StationStatus status3 = new WithTrain(StationExamples.create3Entry2ExitStation())
                .addTrain(10, "i1", "o1", "p1", "s1", 500)
                .build();
         */

        StationStatus status3 = new WithTrain(new BlockStationBuilder(StationDef.create(
                Utils.fromResource("/stations/downville.station.yml"), Locator.root()), GAME_DURATION, FREQUENCY, new Random(SIMULATION_SEED), null).build())
                .build()
                .setTime(ENTRY_TIMEOUT);
        new TrainPanelTest(status3).run();
    }

    private final TrainPane panel;
    private final JFrame frame;
    private final SimulatorEngineImpl<StationStatus, StationStatus> engine;
    private final Random random;
    private final StationStatus initialStatus;

    public TrainPanelTest(StationStatus status) {
        this.frame = new JFrame(getClass().getSimpleName());
        this.panel = new TrainPane();
        this.engine = SimulatorEngineImpl.create(this::stepUp, Function.identity())
                .setEventInterval(Duration.ofMillis(1000 / FPS))
                .setOnEvent(this::handleEvent)
                .setOnSpeed(this::handleSpeed);
        engine.setSpeed(1);
        random = new Random(SIMULATION_SEED);
        this.initialStatus = status;
    }

    private void handleEvent(StationStatus status) {
        panel.setStatus(status);
    }

    private void handleSpeed(double speed) {

    }

    void run() {
        frame.getContentPane().setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(panel);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setSize(DEFAULT_SIZE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        engine.start(initialStatus);
    }

    private Tuple2<StationStatus, Double> stepUp(StationStatus status, double dt) {
        StationStatus next = status.tick(dt, random);
        return Tuple2.of(next, dt);
    }

}