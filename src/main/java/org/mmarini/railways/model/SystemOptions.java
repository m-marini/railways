package org.mmarini.railways.model;

import org.mmarini.railways.model.xml.XMLBuilder;
import org.xml.sax.SAXException;

/**
 * 
 * @author US00852
 * @version $Id: SystemOptions.java,v 1.3 2012/02/08 22:03:18 marco Exp $
 */
public interface SystemOptions {

	/**
	 * 
	 * @param xmlBuilder
	 * @throws SAXException
	 */
	public abstract void createXML(XMLBuilder xmlBuilder) throws SAXException;

	/**
	 * 
	 * @return
	 */
	public abstract float getGain();

	/**
	 * 
	 * @return
	 */
	public abstract String getLookAndFeelClass();

	/**
	 * 
	 * @return
	 */
	public abstract double getTimeSpeed();

	/**
	 * 
	 * @return
	 */
	public abstract boolean isMute();

	/**
	 * 
	 * @param gain
	 */
	public abstract void setGain(float gain);

	/**
	 * 
	 * @param lookAndFeelClass
	 */
	public abstract void setLookAndFeelClass(String lookAndFeelClass);

	/**
	 * 
	 * @param mute
	 */
	public abstract void setMute(boolean mute);

	/**
	 * 
	 * @param timeSpeed
	 */
	public abstract void setTimeSpeed(double timeSpeed);
}
