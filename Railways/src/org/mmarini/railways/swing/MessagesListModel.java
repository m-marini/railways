package org.mmarini.railways.swing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * 
 * @author US00852
 * @version $Id: MessagesListModel.java,v 1.2.20.1 2012/02/04 19:22:56 marco Exp
 *          $
 */
public class MessagesListModel extends AbstractListModel {
	private static final long serialVersionUID = 7272673811420644483L;

	private List<String> values;

	/**
	 * 
	 */
	public MessagesListModel() {
		values = new ArrayList<String>();
	}

	/**
	 * 
	 * @param idx
	 * @param key
	 */
	public void add(int idx, String key) {
		List<String> values = getValues();
		values.add(idx, key);
		fireIntervalAdded(this, idx, idx);
	}

	/**
	 * 
	 * @param key
	 */
	public void add(String key) {
		List<String> values = getValues();
		int idx = values.size();
		values.add(key);
		fireIntervalAdded(this, idx, idx);
	}

	/**
	 * 
	 * 
	 */
	public void clear() {
		List<String> values = getValues();
		if (values.isEmpty())
			return;
		int size = values.size();
		values.clear();
		fireIntervalRemoved(this, 0, size - 1);
	}

	/**
	 * 
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public String getElementAt(int index) {
		String key = getValue(index);
		return Messages.getString(key);
	}

	/**
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return getValues().size();
	}

	/**
	 * 
	 */
	public String getValue(int idx) {
		return getValues().get(idx);
	}

	/**
	 * @return the values
	 */
	private List<String> getValues() {
		return values;
	}

	/**
	 * 
	 * @param idx
	 */
	public void remove(int idx) {
		List<String> values = getValues();
		values.remove(idx);
		fireIntervalRemoved(this, idx, idx);
	}

	/**
	 * 
	 * @param key
	 */
	public void remove(String key) {
		List<String> values = getValues();
		int idx = values.indexOf(key);
		remove(idx);
	}

	/**
	 * 
	 * @param idx
	 * @param key
	 */
	public void set(int idx, String key) {
		List<String> values = getValues();
		values.set(idx, key);
		fireContentsChanged(this, idx, idx);
	}
}
