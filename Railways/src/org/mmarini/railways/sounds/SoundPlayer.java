package org.mmarini.railways.sounds;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author US00852
 * @version $Id: SoundPlayer.java,v 1.7 2013/06/02 14:53:04 marco Exp $
 */
public class SoundPlayer {

	private static final String ARRIVED_SOUND = "/sounds/arrived.wav";
	private static final String STOPPED_SOUND = "/sounds/stopped.wav";
	private static final String LEAVING_SOUND = "/sounds/leaving.wav";
	private static final String BRAKING_SOUND = "/sounds/braking.wav";
	private static final String LEOPOLD_SOUND = "/sounds/leopold.wav";
	private static final String DEVIATOR_SOUNDS = "/sounds/deviator.wav";

	public static Logger log = LoggerFactory.getLogger(SoundPlayer.class);

	private Clip deviatorClip;
	private Clip leopoldClip;
	private Clip arrivedClip;
	private Clip brakingClip;
	private Clip leavingClip;
	private Clip stoppedClip;
	private boolean mute;
	private float gain;

	/**
	 * 
	 */
	public SoundPlayer() {
		init();
	}

	/**
	 * @param gain
	 */
	private void applyClipGain() {
		float gain = getGain();
		setClipGain(deviatorClip, gain);
		setClipGain(leavingClip, gain);
		setClipGain(leopoldClip, gain);
		setClipGain(brakingClip, gain);
		setClipGain(stoppedClip, gain);
		setClipGain(arrivedClip, gain);
	}

	/**
	 * @return the gain
	 */
	public float getGain() {
		return gain;
	}

	private void init() {
		deviatorClip = loadClip(DEVIATOR_SOUNDS);
		leopoldClip = loadClip(LEOPOLD_SOUND);
		brakingClip = loadClip(BRAKING_SOUND);
		leavingClip = loadClip(LEAVING_SOUND);
		stoppedClip = loadClip(STOPPED_SOUND);
		arrivedClip = loadClip(ARRIVED_SOUND);
		applyClipGain();
	}

	/**
	 * @return
	 */
	public boolean isMute() {
		return mute;
	}

	/**
	 * @param resource
	 * @return
	 */
	private Clip loadClip(String resource) {
		Line.Info info = new Line.Info(Clip.class);
		Clip clip;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(getClass()
					.getResource(resource));
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(ais);
			return clip;
		} catch (Exception e) {
			log.error("Error loading sample " + resource, e);
			return null;
		}
	}

	/**
	 * @param clip
	 */
	private void play(Clip clip) {
		if (isMute() || clip == null || clip.isRunning())
			return;
		clip.stop();
		clip.setFramePosition(0);
		clip.start();
	}

	/**
	 * 
	 * 
	 */
	public void playArrived() {
		play(arrivedClip);
	}

	/**
	 * 
	 * 
	 */
	public void playBraking() {
		play(brakingClip);
	}

	/**
	 * 
	 * 
	 */
	public void playDeviator() {
		play(deviatorClip);
	}

	/**
	 * 
	 * 
	 */
	public void playLeaving() {
		play(leavingClip);
	}

	/**
	 * 
	 * 
	 */
	public void playLeopold() {
		play(leopoldClip);
	}

	/**
	 * 
	 * 
	 */
	public void playStopped() {
		play(stoppedClip);
	}

	/**
	 * @param clip
	 * @param gain
	 */
	private void setClipGain(Clip clip, float gain) {
		if (clip != null) {
			try {
				FloatControl control = (FloatControl) clip
						.getControl(FloatControl.Type.MASTER_GAIN);
				control.setValue(gain);
			} catch (Exception e) {
				log.error("Error setting gain: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 * @param gain
	 */
	public void setGain(float gain) {
		this.gain = gain;
		applyClipGain();
	}

	/**
	 * @param mute
	 */
	public void setMute(boolean mute) {
		this.mute = mute;
	}
}