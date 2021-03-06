package org.mmarini.railways.swing;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author $Author: marco $
 * @version $Id: Messages.java,v 1.3 2012/02/08 22:03:32 marco Exp $
 */
public class Messages {
	private static final String BUNDLE_NAME = "org.mmarini.railways.swing.messages";//$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	private Messages() {
	}
}