package org.mmarini.railways.model;

import org.mmarini.railways.model.xml.XMLBuilder;
import org.mmarini.railways.model.xml.XmlConstants;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * 
 * @author US00852
 * @version $Id: SystemOptionsImpl.java,v 1.2.20.1 2012/02/04 19:22:56 marco Exp
 *          $
 */
public class SystemOptionsImpl implements SystemOptions, XmlConstants {

	private static final String CDATA = "CDATA";

	private static final String NULL_STRING = "";

	private static final double DEFAULT_TIME_SPEED = 1e-3;

	private String lookAndFeelClass;
	private double timeSpeed;
	private boolean mute;
	private float gain;

	/**
	 * 
	 */
	public SystemOptionsImpl() {
		timeSpeed = DEFAULT_TIME_SPEED;
	}

	/**
	 * @throws SAXException
	 * @see org.mmarini.railways.model.SystemOptions#createXML(org.mmarini.railways
	 *      .model.xml.XMLBuilder)
	 */
	@Override
	public void createXML(XMLBuilder xmlBuilder) throws SAXException {
		AttributesImpl attributes = new AttributesImpl();
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				LOOK_AND_FEEL_CLASS_ATTRIBUTE, CDATA,
				String.valueOf(lookAndFeelClass));
		attributes.addAttribute(NULL_STRING, NULL_STRING, GAIN_ATTRIBUTE,
				CDATA, String.valueOf(gain));
		attributes.addAttribute(NULL_STRING, NULL_STRING, MUTE_ATTRIBUTE,
				CDATA, String.valueOf(mute));
		attributes.addAttribute(NULL_STRING, NULL_STRING, TIME_SPEED_ATTRIBUTE,
				CDATA, String.valueOf(timeSpeed));

		xmlBuilder.setNameSpace(SYSTEM_OPTIONS_NS_URI);
		xmlBuilder.startElement(SYSTEM_OPTIONS_ELEMENT, attributes);
		xmlBuilder.endElement(SYSTEM_OPTIONS_ELEMENT);
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.SystemOptions#getGain()
	 */
	@Override
	public float getGain() {
		return gain;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.SystemOptions#getLookAndFeelClass()
	 */
	@Override
	public String getLookAndFeelClass() {
		return lookAndFeelClass;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.SystemOptions#getTimeSpeed()
	 */
	@Override
	public double getTimeSpeed() {
		return timeSpeed;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.SystemOptions#isMute()
	 */
	@Override
	public boolean isMute() {
		return mute;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.SystemOptions#setGain(float)
	 */
	@Override
	public void setGain(float gain) {
		this.gain = gain;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.SystemOptions#setLookAndFeelClass(java.lang.String)
	 */
	@Override
	public void setLookAndFeelClass(String lookAndFeelClass) {
		this.lookAndFeelClass = lookAndFeelClass;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.SystemOptions#setMute(boolean)
	 */
	@Override
	public void setMute(boolean mute) {
		this.mute = mute;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.SystemOptions#setTimeSpeed(double)
	 */
	@Override
	public void setTimeSpeed(double timeSpeed) {
		this.timeSpeed = timeSpeed;
	}
}
