/**
 * 
 */
package org.mmarini.railways.model.xml;

import org.mmarini.railways.model.SystemOptions;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author US00852
 * 
 */
public class SystemOptionsSaxHandler extends DefaultHandler implements
		XmlConstants {
	private SystemOptions options;
	private Locator locator;

	/**
	 * 
	 */
	public SystemOptionsSaxHandler() {
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(SAXParseException e) throws SAXException {
		throw e;
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator
	 *      )
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	/**
	 * @param options
	 *            the options to set
	 */
	public void setOptions(SystemOptions options) {
		this.options = options;
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (!SYSTEM_OPTIONS_NS_URI.equals(uri))
			return;
		if (SYSTEM_OPTIONS_ELEMENT.equals(qName)) {
			try {
				String laf = attributes.getValue(LOOK_AND_FEEL_CLASS_ATTRIBUTE);
				float gain = Float.parseFloat(attributes
						.getValue(GAIN_ATTRIBUTE));
				boolean mute = "true".equalsIgnoreCase(attributes
						.getValue(MUTE_ATTRIBUTE));
				double timeSpeed = Double.parseDouble(attributes
						.getValue(TIME_SPEED_ATTRIBUTE));
				options.setLookAndFeelClass(laf);
				options.setMute(mute);
				options.setGain(gain);
				options.setTimeSpeed(timeSpeed);
			} catch (NumberFormatException e) {
				throw new SAXParseException(e.getMessage(), locator, e);
			}
		}
	}
}
