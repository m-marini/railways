package org.mmarini.railways.model.xml;

import java.io.File;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 
 * @author US00852
 * 
 */
public class XMLBuilder {

	private static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();
	private static final String NULL_STRING = "";
	private TransformerHandler handler;
	private String nameSpace;

	/**
	 * 
	 */
	protected XMLBuilder() {
		nameSpace = NULL_STRING;
	}

	/**
	 * @throws SAXException
	 * 
	 */
	public void closeDocument() throws SAXException {
		handler.endDocument();
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @throws SAXException
	 */
	public void create(String name, boolean value) throws SAXException {
		create(name, Boolean.toString(value));
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @throws SAXException
	 */
	public void create(String name, double value) throws SAXException {
		create(name, String.valueOf(value));
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @throws SAXException
	 */
	public void create(String name, int value) throws SAXException {
		create(name, String.valueOf(value));
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @throws SAXException
	 */
	public void create(String name, String value) throws SAXException {
		startElement(name, EMPTY_ATTRIBUTES);
		createText(value);
		endElement(name);
	}

	/**
	 * 
	 * @param value
	 * @throws SAXException
	 */
	public void createText(String value) throws SAXException {
		char[] bfr = value.toCharArray();
		handler.characters(bfr, 0, bfr.length);
	}

	/**
	 * @throws SAXException
	 * 
	 */
	protected void destroy() throws SAXException {
		handler.endDocument();
	}

	/**
	 * 
	 * @param name
	 * @throws SAXException
	 */
	public void endElement(String name) throws SAXException {
		handler.endElement(nameSpace, NULL_STRING, name);
	}

	/**
	 * 
	 * @param file
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws SAXException
	 */
	protected void openDocument(File file)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, SAXException {
		openDocument(new StreamResult(file));
	}

	/**
	 * @param result
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws SAXException
	 */
	private void openDocument(Result result)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, SAXException {
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
				.newInstance();
		handler = tf.newTransformerHandler();
		Transformer tr = handler.getTransformer();
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		handler.setResult(result);
		handler.startDocument();
	}

	/**
	 * 
	 * @param nameSpace
	 */
	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	/**
	 * 
	 * @param stream
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws SAXException
	 */
	protected void startDocument(OutputStream stream)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, SAXException {
		openDocument(new StreamResult(stream));
	}

	/**
	 * 
	 * @param name
	 * @throws SAXException
	 */
	public void startElement(String name) throws SAXException {
		handler.startElement(nameSpace, NULL_STRING, name, EMPTY_ATTRIBUTES);
	}

	/**
	 * 
	 * @param name
	 * @param attributes
	 * @throws SAXException
	 */
	public void startElement(String name, Attributes attributes)
			throws SAXException {
		handler.startElement(nameSpace, NULL_STRING, name, attributes);
	}

}