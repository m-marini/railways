package org.mmarini.railways.swing;

import org.mmarini.railways.model.graphics.LinePainterFactory;

/**
 * @author $Author: marco $
 * @version $Id: JStationGlobal.java,v 1.5 2006/08/29 16:42:37 marco Exp $
 */
public class JStationGlobal extends JStation {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public JStationGlobal() {
		super(LinePainterFactory.getInstance());
		setPaintNodes(false);
		setPaintLabel(false);
	}

}