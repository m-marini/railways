package org.mmarini.railways.model.visitor;

import java.util.ArrayList;
import java.util.List;

import org.mmarini.railways.model.elements.Line;

/**
 * @author $Author: marco $
 * @version $Id: LineListVisitor.java,v 1.7 2012/02/08 22:03:26 marco Exp $
 */
public class LineListVisitor extends ElementVisitorAdapter {
	private List<Line> list = new ArrayList<Line>();

	/**
	 * @return Returns the list.
	 */
	public List<Line> getList() {
		return list;
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitLine(org.mmarini.railways.model.elements.Line)
	 */
	@Override
	public void visitLine(Line line) {
		list.add(line);
	}
}