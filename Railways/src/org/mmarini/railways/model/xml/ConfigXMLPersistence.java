package org.mmarini.railways.model.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmarini.railways.model.HallOfFame;
import org.mmarini.railways.model.HallOfFameImpl;
import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.SystemOptions;
import org.mmarini.railways.model.SystemOptionsImpl;
import org.xml.sax.SAXException;

/**
 * 
 * @author US00852
 * @version $Id: HallOfFameXMLPersistence.java,v 1.1.2.1 2006/08/25 16:57:43
 *          marco Exp $
 */
public class ConfigXMLPersistence implements RailwayConstants {

	class NoCloseableInputStream extends FilterInputStream {

		/**
		 * 
		 * @param stream
		 */
		protected NoCloseableInputStream(InputStream stream) {
			super(stream);
		}

		/**
		 * @see java.io.FilterInputStream#close()
		 */
		@Override
		public void close() throws IOException {
		}

	}

	private static final String SYSTEM_OPTIONS_XSD = "/xsd/systemOptions-0.0.1.xsd";
	private static final String HALL_OF_FAME_XSD = "/xsd/hallOfFame-0.0.1.xsd";
	private static final String HALL_OF_FAME_ZIP_ENTRY = "hallOfFame.xml";
	private static final String SYSTEM_OPTIONS_ZIP_ENTRY = "systemOptions.xml";

	private static Log log = LogFactory.getLog(ConfigXMLPersistence.class);

	private File resource;
	private HallOfFame hallOfFame;
	private SystemOptions systemOptions;
	private SAXParserFactory systemOptionsFactory;
	private SAXParserFactory hofFactory;
	private SystemOptionsSaxHandler systemOptionsHandler;
	private HallOfFameSaxHandler hallOfFameHandler;

	/**
	 * 
	 */
	public ConfigXMLPersistence() {
		resource = new File(System.getProperty("user.home") + File.separator
				+ ".railways" + File.separator + "config.jar");
		hallOfFame = new HallOfFameImpl();
		systemOptions = new SystemOptionsImpl();
		systemOptionsHandler = new SystemOptionsSaxHandler();
		hallOfFameHandler = new HallOfFameSaxHandler();

		systemOptionsFactory = SAXParserFactory.newInstance();
		systemOptionsFactory.setNamespaceAware(true);
		URL schemaUrl = getClass().getResource(SYSTEM_OPTIONS_XSD);
		if (schemaUrl != null)
			try {
				Schema schema = SchemaFactory.newInstance(
						XMLConstants.W3C_XML_SCHEMA_NS_URI)
						.newSchema(schemaUrl);
				systemOptionsFactory.setSchema(schema);
			} catch (SAXException e) {
				log.error(e.getMessage(), e);
			}
		hofFactory = SAXParserFactory.newInstance();
		hofFactory.setNamespaceAware(true);
		schemaUrl = getClass().getResource(HALL_OF_FAME_XSD);
		if (schemaUrl != null)
			try {
				Schema schema = SchemaFactory.newInstance(
						XMLConstants.W3C_XML_SCHEMA_NS_URI)
						.newSchema(schemaUrl);
				hofFactory.setSchema(schema);
			} catch (SAXException e) {
				log.error(e.getMessage(), e);
			}
		load();
	}

	/**
	 * @param entry
	 * @return
	 * @throws IOException
	 */
	private InputStream createInputStream(String entry) throws IOException {
		if (!resource.exists()) {
			save();
		}
		JarFile file = new JarFile(resource);
		InputStream is = file.getInputStream(new JarEntry(entry));
		if (is == null)
			log.warn("\"" + entry + "\" entry not found in " + resource);
		return is;
	}

	/**
	 * @return the hallOfFame
	 */
	public HallOfFame getHallOfFame() {
		return hallOfFame;
	}

	/**
	 * @return the systemOptions
	 */
	public SystemOptions getSystemOptions() {
		return systemOptions;
	}

	/**
         * 
         * 
         */
	private void load() {
		try {
			loadHallOfFame();
			loadSystemOptions();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private void loadHallOfFame() throws IOException, SAXException,
			ParserConfigurationException {
		InputStream is = createInputStream(HALL_OF_FAME_ZIP_ENTRY);
		if (is != null)
			log.debug("loadHallOfFame");
		hallOfFameHandler.setHallOfFame(hallOfFame);
		hofFactory.newSAXParser().parse(is, hallOfFameHandler);
	}

	/**
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * 
	 */
	private void loadSystemOptions() throws IOException, SAXException,
			ParserConfigurationException {
		InputStream is = createInputStream(SYSTEM_OPTIONS_ZIP_ENTRY);
		if (is != null) {
			log.debug("loadSystemOptions");
			systemOptionsHandler.setOptions(systemOptions);
			systemOptionsFactory.newSAXParser().parse(is, systemOptionsHandler);
		}
	}

	/**
         * 
         * 
         */
	private void save() {
		try {
			File file = resource;
			file.getParentFile().mkdirs();
			OutputStream os = new FileOutputStream(file);
			JarOutputStream jos = new JarOutputStream(os);
			jos.putNextEntry(new JarEntry(SYSTEM_OPTIONS_ZIP_ENTRY));
			XMLBuilder xmlBuilder = new XMLBuilder();
			xmlBuilder.startDocument(jos);
			systemOptions.createXML(xmlBuilder);
			xmlBuilder.closeDocument();
			jos.closeEntry();
			jos.putNextEntry(new JarEntry(HALL_OF_FAME_ZIP_ENTRY));
			xmlBuilder.startDocument(jos);
			hallOfFame.createXML(xmlBuilder);
			xmlBuilder.closeDocument();
			jos.close();
			os.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * @param hallOfFame
	 *            the hallOfFame to set
	 */
	public void setHallOfFame(HallOfFame hallOfFame) {
		this.hallOfFame = hallOfFame;
		save();
	}

	/**
	 * @param systemOptions
	 *            the systemOptions to set
	 */
	public void setSystemOptions(SystemOptions systemOptions) {
		this.systemOptions = systemOptions;
		save();
	}
}
