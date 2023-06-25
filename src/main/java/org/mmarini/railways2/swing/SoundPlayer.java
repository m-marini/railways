package org.mmarini.railways2.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.net.URL;

/**
 * @author US00852
 * @version $Id: SoundPlayer.java,v 1.7 2013/06/02 14:53:04 marco Exp $
 */
public class SoundPlayer {

    private static final String ARRIVED_SOUND = "/sounds/arrived.wav";
    private static final String STOPPED_SOUND = "/sounds/stopped.wav";
    private static final String LEAVING_SOUND = "/sounds/leaving.wav";
    private static final String BRAKING_SOUND = "/sounds/braking.wav";
    private static final String ARRIVING_SOUND = "/sounds/leopold.wav";
    private static final String SWITCH_SOUND = "/sounds/switch.wav";

    public static Logger logger = LoggerFactory.getLogger(SoundPlayer.class);

    /**
     * Returns the clip loaded from resource
     *
     * @param resource the resource
     */
    private static Clip loadClip(String resource) {
        URL url = SoundPlayer.class.getResource(resource);
        if (url == null) {
            logger.atError().log("Missing resource {}", resource);
            return null;
        }
        try {
            Line.Info info = new Line.Info(Clip.class);
            Clip clip = (Clip) AudioSystem.getLine(info);
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            logger.atError().setCause(e).log("Error loading sound {}", resource);
            return null;
        }
    }

    /**
     * Sets the gain level to the clip
     *
     * @param clip the clip
     * @param gain the gain level (db)
     */
    private static void setClipGain(Clip clip, float gain) {
        if (clip != null) {
            try {
                FloatControl control = (FloatControl) clip
                        .getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(gain);
            } catch (Exception e) {
                logger.error("Error setting gain: " + e.getMessage(), e);
            }
        }
    }

    private Clip switchClip;
    private Clip arrivingClip;
    private Clip arrivedClip;
    private Clip brakingClip;
    private Clip leavingClip;
    private Clip stoppedClip;
    private boolean mute;
    private float gain;

    /**
     * Creates the sound player
     */
    public SoundPlayer() {
        init();
    }

    /**
     * Applies the gain level to all clips
     */
    private void applyClipGain() {
        float gain = getGain();
        setClipGain(switchClip, gain);
        setClipGain(leavingClip, gain);
        setClipGain(arrivingClip, gain);
        setClipGain(brakingClip, gain);
        setClipGain(stoppedClip, gain);
        setClipGain(arrivedClip, gain);
    }

    /**
     * Returns the arrived clip
     */
    public Clip getArrivedClip() {
        return arrivedClip;
    }

    /**
     * Returns the arriving clip
     */
    public Clip getArrivingClip() {
        return arrivingClip;
    }

    /**
     * Returns the braking clip
     */
    public Clip getBrakingClip() {
        return brakingClip;
    }

    /**
     * Returns the gain level of player (db)
     */
    public float getGain() {
        return gain;
    }

    /**
     * Sets the gain level of player
     *
     * @param gain the gain (db)
     */
    public void setGain(float gain) {
        this.gain = gain;
        applyClipGain();
    }

    /**
     * Returns the leaving clip
     */
    public Clip getLeavingClip() {
        return leavingClip;
    }

    /**
     * Return the stopped clip
     */
    public Clip getStoppedClip() {
        return stoppedClip;
    }

    /**
     * Returns the switch clip
     */
    public Clip getSwitchClip() {
        return switchClip;
    }

    /**
     * Initializes
     */
    private void init() {
        switchClip = loadClip(SWITCH_SOUND);
        arrivingClip = loadClip(ARRIVING_SOUND);
        brakingClip = loadClip(BRAKING_SOUND);
        leavingClip = loadClip(LEAVING_SOUND);
        stoppedClip = loadClip(STOPPED_SOUND);
        arrivedClip = loadClip(ARRIVED_SOUND);
        applyClipGain();
    }

    /**
     * Returns true if sound player is mute
     */
    public boolean isMute() {
        return mute;
    }

    /**
     * Set mute sound player
     *
     * @param mute true to mute sound player
     */
    public void setMute(boolean mute) {
        this.mute = mute;
    }

    /**
     * Plays the clip
     *
     * @param clip the clip
     */
    void play(Clip clip) {
        if (!isMute() && clip != null && !clip.isRunning()) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }
}