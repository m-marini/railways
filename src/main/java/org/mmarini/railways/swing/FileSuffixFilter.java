package org.mmarini.railways.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * @author $Author: marco $
 * @version $Id: FileSuffixFilter.java,v 1.2.40.1 2012/02/04 19:22:55 marco Exp
 *          $
 */
public class FileSuffixFilter extends FileFilter {
	private String description;
	private String suffix;
	private boolean readMode;
	private boolean writeMode;

	/**
	 * @param description
	 * @param suffix
	 */
	public FileSuffixFilter(String description, String suffix) {
		this(description, suffix, true, true);
	}

	/**
	 * @param description
	 * @param suffix
	 * @param readMode
	 * @param writeMode
	 */
	public FileSuffixFilter(String description, String suffix,
			boolean readMode, boolean writeMode) {
		this.description = description;
		this.suffix = suffix;
		this.readMode = readMode;
		this.writeMode = writeMode;
	}

	/**
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		if (!f.getName().endsWith(getSuffix()))
			return false;
		if (isReadMode() && f.canRead())
			return true;
		if (isWriteMode() && f.canWrite())
			return true;
		return false;
	}

	/**
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @return Returns the suffix.
	 */
	private String getSuffix() {
		return suffix;
	}

	/**
	 * @return Returns the readMode.
	 */
	private boolean isReadMode() {
		return readMode;
	}

	/**
	 * @return Returns the writeMode.
	 */
	private boolean isWriteMode() {
		return writeMode;
	}
}