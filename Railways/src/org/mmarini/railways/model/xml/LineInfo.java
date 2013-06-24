/**
 * 
 */
package org.mmarini.railways.model.xml;

import org.mmarini.railways.model.elements.Line;

/**
 * @author US00852
 * 
 */
public class LineInfo {
	private Line line;
	private boolean destination;

	/**
	 * 
	 */
	public LineInfo() {
	}

	/**
	 * 
	 * @param line
	 * @param destination
	 */
	public LineInfo(Line line, boolean destination) {
		this.line = line;
		this.destination = destination;
	}

	/**
	 * @return the line
	 */
	public Line getLine() {
		return line;
	}

	/**
	 * @return the destination
	 */
	public boolean isDestination() {
		return destination;
	}

}
