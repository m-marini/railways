package org.mmarini.railways.swing;

import org.mmarini.railways.model.GameHandler;
import org.mmarini.railways.model.ManagerInfos;
import org.mmarini.railways.model.RailwayConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;

/**
 * @author $Author: marco $
 * @version $Id: PerformancePanel.java,v 1.5 2012/02/08 22:03:31 marco Exp $
 */
public class ManagerPane extends JPanel implements RailwayConstants {
    private static final Font MANAGER_FONT = Font.decode("Dialog Bold 10");
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(ManagerPane.class);
   private JLabel incomeTrainCount;
    private JLabel wrongOutcomeTrainCount;
    private JLabel rightOutcomeTrainCount;
    private JLabel trainsLifeTime;
    private JLabel trainsDistance;
    private JLabel trainsStopCount;
    private JLabel trainsWaitTime;
    private JLabel totalLifeTime;
    private JLabel averageSpeed;
    private JLabel stationTrainCount;
    private JLabel lostTrainCount;
    private JLabel performance;
    private GameHandler handler;

    /**
     *
     */
    public ManagerPane() {
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
     * @param data
     * @param index
     * @param value
     * @param count
     */
    private void createAverageTimeData(Object[] data, int index, double value,
                                       int count) {
        if (count > 0)
            value /= count;
        createTimeData(data, index, value);
    }

    /**
     * @param data
     * @param index
     * @param value
     * @param reference
     */
    private void createRatioData(Object[] data, int index, double value,
                                 double reference) {
        if (reference == 0)
            data[index] = new Double(0);
        else
            data[index] = new Double(value / reference);
    }

    /**
     * @param data
     * @param index
     * @param value
     * @param count
     */
    private void createRatioData(Object[] data, int index, double value,
                                 int count) {
        if (count == 0)
            data[index] = new Double(0);
        else
            data[index] = new Double(value / count);
    }

    /**
     * @param data
     * @param offset
     * @param value
     */
    private void createTimeData(Object[] data, int offset, double value) {
        int secTime = (int) Math.round(value);
        int hours = secTime / (int) SPH;
        int minutes = (secTime % (int) SPH) / (int) SPM;
        int seconds = secTime % (int) SPM;
        data[offset] = new Integer(seconds);
        data[offset + 1] = new Integer(minutes);
        data[offset + 2] = new Integer(hours);
    }

    /**
     * @return
     */
    private int getStationTrainCountValue() {
        ManagerInfos mi = handler.getManagerInfos();
        return mi.getStationTrainCount();
        // return getHandler().getStation().getTrainList().size();
    }

    /**
     *
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
    }

    /**
     *
     */
    public void reload() {
        ManagerInfos infos = handler.getManagerInfos();

        setLabelValue(performance,
                Messages.getString("ManagerPane.performance.message"), //$NON-NLS-1$
                infos.getPerformance() * SPH);

        setLabelValue(incomeTrainCount,
                Messages.getString("ManagerPane.incomeTrainCount.message"), //$NON-NLS-1$
                infos.getIncomeTrainCount());

        setPercentLabelValue(stationTrainCount,
                Messages.getString("ManagerPane.stationTrainCount.message"), //$NON-NLS-1$
                getStationTrainCountValue(), infos.getIncomeTrainCount());

        setPercentLabelValue(
                rightOutcomeTrainCount,
                Messages.getString("ManagerPane.rightOutcomeTrainCount.message"), //$NON-NLS-1$
                infos.getRightOutcomeTrainCount(), infos.getIncomeTrainCount());

        setPercentLabelValue(
                lostTrainCount,
                Messages.getString("ManagerPane.lostTrainCount.message"), infos //$NON-NLS-1$
                        .getIncomeTrainCount() - getStationTrainCountValue()
                        - infos.getRightOutcomeTrainCount()
                        - infos.getWrongOutcomeTrainCount(),
                infos.getIncomeTrainCount());

        setPercentLabelValue(
                wrongOutcomeTrainCount,
                Messages.getString("ManagerPane.wrongOutcomeTrainCount.message"), //$NON-NLS-1$
                infos.getWrongOutcomeTrainCount(), infos.getIncomeTrainCount());

        setTimeLabelValue(
                totalLifeTime,
                Messages.getString("ManagerPane.totalLifeTime.message"), infos.getTotalLifeTime()); //$NON-NLS-1$

        setAverageLabelValue(trainsDistance,
                Messages.getString("ManagerPane.trainDistance.message"), infos //$NON-NLS-1$
                        .getTrainsDistance() / 1000.,
                infos.getIncomeTrainCount());

        setAverageTimeLabelValue(trainsLifeTime,
                Messages.getString("ManagerPane.trainsLifeTime.message"), //$NON-NLS-1$
                infos.getTrainsLifeTime(), infos.getIncomeTrainCount());

        setRatioLabelValue(averageSpeed,
                Messages.getString("ManagerPane.averageSpeed.message"), infos //$NON-NLS-1$
                        .getTrainsDistance() / 1000., infos.getTrainsLifeTime()
                        / SPH);

        setAverageLabelValue(
                trainsStopCount,
                Messages.getString("ManagerPane.trainsStopCount.message"), infos.getTrainsStopCount(), //$NON-NLS-1$
                infos.getIncomeTrainCount());

        setAveragePercentTimeLabelValue(
                trainsWaitTime,
                Messages.getString("ManagerPane.trainsWaitTime.message"), //$NON-NLS-1$
                infos.getTrainsWaitTime(), infos.getTrainsStopCount(),
                infos.getTrainsLifeTime());
    }

    /**
     * @param label
     * @param msg
     * @param value
     * @param count
     */
    private void setAverageLabelValue(JLabel label, String msg, double value,
                                      int count) {
        Object[] data = new Object[2];
        data[0] = new Double(value);
        createRatioData(data, 1, value, count);
        setLabelValue(label, msg, data);
    }

    /**
     * @param label
     * @param msg
     * @param value
     * @param count
     * @param reference
     */
    private void setAveragePercentTimeLabelValue(JLabel label, String msg,
                                                 double value, int count, double reference) {
        Object[] data = new Object[7];
        createTimeData(data, 0, value);
        createAverageTimeData(data, 3, value, count);
        createRatioData(data, 6, value, reference);
        setLabelValue(label, msg, data);
    }

    /**
     * @param label
     * @param msg
     * @param value
     * @param count
     */
    private void setAverageTimeLabelValue(JLabel label, String msg,
                                          double value, int count) {
        Object[] data = new Object[6];
        createTimeData(data, 0, value);
        createAverageTimeData(data, 3, value, count);
        setLabelValue(label, msg, data);
    }

    /**
     * @param handler The handler to set.
     */
    public void setHandler(GameHandler handler) {
        this.handler = handler;
    }

    /**
     * @param label
     * @param msg
     * @param value
     */
    private void setLabelValue(JLabel label, String msg, double value) {
        setLabelValue(label, msg, new Object[]{new Double(value)});
    }

    /**
     * @param label
     * @param msg
     * @param value
     */
    private void setLabelValue(JLabel label, String msg, int value) {
        setLabelValue(label, msg, new Object[]{new Integer(value)});
    }

    /**
     * @param label
     * @param fmt
     * @param data
     */
    private void setLabelValue(JLabel label, String fmt, Object[] data) {
        label.setFont(MANAGER_FONT);
        label.setText(MessageFormat.format(fmt, data));
    }

    /**
     * @param label
     * @param msg
     * @param value
     * @param reference
     */
    private void setPercentLabelValue(JLabel label, String msg, int value,
                                      int reference) {
        Object[] data = new Object[2];
        data[0] = new Double(value);
        createRatioData(data, 1, value, reference);
        setLabelValue(label, msg, data);
    }

    /**
     * @param label
     * @param msg
     * @param value
     * @param reference
     */
    private void setRatioLabelValue(JLabel label, String msg, double value,
                                    double reference) {
        Object[] data = new Object[1];
        createRatioData(data, 0, value, reference);
        setLabelValue(label, msg, data);
    }

    /**
     * @param label
     * @param msg
     * @param value
     */
    private void setTimeLabelValue(JLabel label, String msg, double value) {
        Object[] data = new Object[3];
        createTimeData(data, 0, value);
        setLabelValue(label, msg, data);
    }
}