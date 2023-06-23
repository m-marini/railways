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

import org.mmarini.railways2.model.ExtendedPerformance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

import static java.lang.Math.max;
import static org.mmarini.railways2.swing.SwingUtils.formatMessage;

/**
 * Shows the performance information
 */
public class PerformancePanel extends JPanel {
    private static final Font MANAGER_FONT = Font.decode("Dialog Bold 10");
    /**
     * Seconds per minute
     */
    private static final int SPM = 60;
    /**
     * Seconds per hour
     */
    private static final int SPH = 3600;
    private static final Logger log = LoggerFactory.getLogger(PerformancePanel.class);

    /**
     * Returns the formatted text of averaged time totals
     * The message parameters are
     * <ol>
     *     <li>the total seconds</li>
     *     <li>the total minutes</li>
     *     <li>the total hours</li>
     *     <li>the average seconds</li>
     *     <li>the average minutes</li>
     *     <li>the average hours</li>
     * </ol>
     *
     * @param messageKey the formatting message key
     * @param totalTime  the total time (s)
     * @param count      the number of samples
     */
    private static String formatAverageTime(String messageKey,
                                            double totalTime, int count) {
        double avgTime = totalTime / max(1, count);
        return formatMessage(messageKey,
                getSeconds(totalTime),
                getMinutes(totalTime),
                getHours(totalTime),
                getSeconds(avgTime),
                getMinutes(avgTime),
                getHours(avgTime));
    }

    /**
     * Returns the formatted text of averaged time totals
     * The message parameters are
     * <ol>
     *     <li>the total seconds</li>
     *     <li>the total minutes</li>
     *     <li>the total hours</li>
     *     <li>the average seconds</li>
     *     <li>the average minutes</li>
     *     <li>the average hours</li>
     *     <li>the ratio between totalTime and reference</li>
     * </ol>
     *
     * @param messageKey the formatting message key
     * @param totalTime  the total time
     * @param count      the number of time samples
     * @param reference  the reference
     */
    private static String formatAverageTimeAndRatio(String messageKey,
                                                    double totalTime, int count, double reference) {
        double avgTime = totalTime / max(1, count);
        return formatMessage(messageKey,
                getSeconds(totalTime),
                getMinutes(totalTime),
                getHours(totalTime),
                getSeconds(avgTime),
                getMinutes(avgTime),
                getHours(avgTime),
                getRatio(totalTime, reference));
    }

    /**
     * Returns the formatted text of ratio
     * The message parameters are
     * <ol>
     *     <li>the numerator</li>
     *     <li>the ratio between numerator and denominator</li>
     * </ol>
     *
     * @param messageKey  the formatting message key
     * @param numerator   the numerator
     * @param denominator the denominator
     */
    private static String formatRatio(String messageKey, double numerator, double denominator) {
        return formatMessage(messageKey, numerator, getRatio(numerator, denominator));
    }

    /**
     * Returns the formatted time
     * The message parameters are:
     * <ol>
     * <li>the seconds</li>
     * <li>the minutes</li>
     * <li>the hours</li>
     * </ol>
     *
     * @param messageKey the formatting message key
     * @param time       the time (s)
     */
    private static String formatTime(String messageKey, double time) {
        return formatMessage(messageKey, getSeconds(time), getMinutes(time), getHours(time));
    }

    /**
     * Returns the number of hours
     *
     * @param value time interval (s)
     */
    private static int getHours(double value) {
        return (int) Math.round(value) / SPH;
    }

    /**
     * Returns the number of minutes
     *
     * @param value time interval (s)
     */
    private static int getMinutes(double value) {
        return ((int) Math.round(value) % SPH) / SPM;
    }

    /**
     * Returns the ratio value numerator/denominator or 0 if denominator is 0
     *
     * @param numerator   the numerator
     * @param denominator the denominator
     */
    private static double getRatio(double numerator, double denominator) {
        return denominator == 0 ? 0 : numerator / denominator;
    }

    /**
     * Returns the number of seconds
     *
     * @param value time interval (s)
     */
    private static int getSeconds(double value) {
        return (int) Math.round(value) % SPM;
    }

    private final JLabel incomeTrainCount;
    private final JLabel wrongOutcomeTrainCount;
    private final JLabel rightOutcomeTrainCount;
    private final JLabel trainsLifeTime;
    private final JLabel trainsDistance;
    private final JLabel trainsStopCount;
    private final JLabel trainsWaitTime;
    private final JLabel totalLifeTime;
    private final JLabel averageSpeed;
    private final JLabel stationTrainCount;
    private final JLabel lostTrainCount;
    private final JLabel performance;

    /**
     *
     */
    public PerformancePanel() {
        incomeTrainCount = new JLabel();
        wrongOutcomeTrainCount = new JLabel();
        rightOutcomeTrainCount = new JLabel();
        trainsLifeTime = new JLabel();
        trainsDistance = new JLabel();
        trainsStopCount = new JLabel();
        trainsWaitTime = new JLabel();
        totalLifeTime = new JLabel();
        averageSpeed = new JLabel();
        stationTrainCount = new JLabel();
        lostTrainCount = new JLabel();
        performance = new JLabel();
        init();
    }

    /**
     * Initializes the content
     */
    private void init() {
        log.debug("init");
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        gbc.gridx = 0;
        Component comp;

        comp = performance;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = incomeTrainCount;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = rightOutcomeTrainCount;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = wrongOutcomeTrainCount;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = stationTrainCount;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = lostTrainCount;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = totalLifeTime;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = trainsLifeTime;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = trainsStopCount;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = trainsWaitTime;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = trainsDistance;
        gbl.setConstraints(comp, gbc);
        add(comp);

        comp = averageSpeed;
        gbl.setConstraints(comp, gbc);
        add(comp);

        for (int i = 0; i < getComponentCount(); i++) {
            Component comp1 = getComponent(i);
            if (comp1 instanceof JLabel) {
                getComponent(i).setFont(MANAGER_FONT);
            }
        }
    }

    /**
     * Sets the performance to show
     *
     * @param performance the performance
     */
    public void setPerformance(ExtendedPerformance performance) {
        this.performance.setText(formatMessage(
                "PerformancePanel.performance.message",
                performance.getPerformance() * SPH));
        incomeTrainCount.setText(formatMessage(
                "PerformancePanel.incomingTrainNumber.message",
                performance.getIncomingTrainNumber()));

        stationTrainCount.setText(formatRatio(
                "PerformancePanel.stationTrainNumber.message",
                performance.getStationTrainNumber(),
                performance.getIncomingTrainNumber()));

        rightOutcomeTrainCount.setText(formatRatio(
                "PerformancePanel.rightOutgoingTrainNumber.message",
                performance.getRightOutgoingTrainNumber(),
                performance.getIncomingTrainNumber()));

        lostTrainCount.setText(formatRatio(
                "PerformancePanel.lostTrainNumber.message",
                performance.getIncomingTrainNumber() - performance.getStationTrainNumber()
                        - performance.getRightOutgoingTrainNumber()
                        - performance.getWrongOutgoingTrainNumber(),
                performance.getIncomingTrainNumber()));

        wrongOutcomeTrainCount.setText(formatRatio(
                "PerformancePanel.wrongOutgoingTrainNumber.message",
                performance.getWrongOutgoingTrainNumber(),
                performance.getIncomingTrainNumber()));

        totalLifeTime.setText(formatTime(
                "PerformancePanel.elapsedTime.message",
                performance.getElapsedTime()));

        trainsDistance.setText(formatRatio(
                "PerformancePanel.traveledDistance.message",
                performance.getTraveledDistance() / 1000.,
                performance.getIncomingTrainNumber()));

        trainsLifeTime.setText(formatAverageTime(
                "PerformancePanel.trainsLifeTime.message",
                performance.getTotalTrainTime(),
                performance.getIncomingTrainNumber()));

        averageSpeed.setText(formatRatio(
                "PerformancePanel.averageSpeed.message",
                performance.getTraveledDistance() / 1000.,
                performance.getTotalTrainTime() / SPH));

        trainsStopCount.setText(formatRatio(
                "PerformancePanel.trainsStopCount.message",
                performance.getTrainStopNumber(),
                performance.getIncomingTrainNumber()));

        trainsWaitTime.setText(formatAverageTimeAndRatio(
                "PerformancePanel.trainWaitingTime.message",
                performance.getTrainWaitingTime(),
                performance.getTrainStopNumber(),
                performance.getTotalTrainTime()));
    }
}