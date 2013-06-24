package org.mmarini.railways.model.visitor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author $$Author: marco $$
 * @version $Id: TraverseSet.java,v 1.4 2012/02/08 22:03:25 marco Exp $
 */
public class TraverseSet<Type> {
	private Set<Type> traveseList = new HashSet<Type>();

	/**
	 * @param object
	 */
	public void addTraversed(Type object) {
		getTraveseList().add(object);
	}

	/**
	 * @return Returns the traveseList.
	 */
	protected Set<Type> getTraveseList() {
		return traveseList;
	}

	/**
	 * @param object
	 * @return
	 */
	public boolean isTraversed(Type object) {
		return getTraveseList().contains(object);
	}
}