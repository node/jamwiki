/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides utility methods useful for parsing and processing XML data.
 */
public class XMLUtil {

	/** Logger */
	public static final WikiLogger logger = WikiLogger.getLogger(XMLUtil.class.getName());

	/**
	 *
	 */
	private XMLUtil() {
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param escape If <code>true</code> then any less than, greater than, quotation mark,
	 *  apostrophe or ampersands in tagValue will be XML-escaped.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, String tagValue, boolean escape) {
		// note - closing a StringWriter is not necessary
		Writer writer = new StringWriter();
		try {
			XMLUtil.buildTag(writer, tagName, tagValue, escape);
		} catch (IOException e) {
			// StringWriter never throws IOException, ignore
		}
		return writer.toString();
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param writer The writer to write the XML tag output to.
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param escape If <code>true</code> then any less than, greater than, quotation mark,
	 *  apostrophe or ampersands in tagValue will be XML-escaped.
	 * @throws IOException Thrown if an error occurs while writing to the writer.
	 */
	public static void buildTag(Writer writer, String tagName, String tagValue, boolean escape) throws IOException {
		if (tagValue == null) {
			return;
		}
		if (tagValue.length() == 0) {
			writer.append('<').append(tagName).append(" />");
		} else {
			writer.append('<').append(tagName).append('>');
			if (escape) {
				StringEscapeUtils.ESCAPE_XML.translate(tagValue, writer);
			} else {
				writer.append(tagValue);
			}
			writer.append("</").append(tagName).append('>');
		}
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param attributes A map of attributes for the tag.
	 * @param escape If <code>true</code> then any less than, greater than, quotation mark,
	 *  apostrophe or ampersands in tagValue will be XML-escaped.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, String tagValue, Map<String, String> attributes, boolean escape) {
		// note - closing a StringWriter is not necessary
		Writer writer = new StringWriter();
		try {
			XMLUtil.buildTag(writer, tagName, tagValue, attributes, escape);
		} catch (IOException e) {
			// StringWriter never throws IOException, ignore
		}
		return writer.toString();
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param writer The writer to write the XML tag output to.
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param attributes A map of attributes for the tag.
	 * @param escape If <code>true</code> then any less than, greater than, quotation mark,
	 *  apostrophe or ampersands in tagValue will be XML-escaped.
	 * @throws IOException Thrown if an error occurs while writing to the writer.
	 */
	public static void buildTag(Writer writer, String tagName, String tagValue, Map<String, String> attributes, boolean escape) throws IOException {
		if (tagValue == null) {
			return;
		}
		writer.append('<').append(tagName);
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			writer.append(' ');
			if (escape) {
				StringEscapeUtils.ESCAPE_XML.translate(entry.getKey(), writer);
				writer.append("=\"");
				StringEscapeUtils.ESCAPE_XML.translate(entry.getValue(), writer);
			} else {
				writer.append(' ').append(entry.getKey()).append("=\"").append(entry.getValue());
			}
			writer.append('\"');
		}
		if (tagValue.length() == 0) {
			writer.append(" />");
		} else {
			writer.append('>');
			if (escape) {
				StringEscapeUtils.ESCAPE_XML.translate(tagValue, writer);
			} else {
				writer.append(tagValue);
			}
			writer.append("</").append(tagName).append('>');
		}
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, int tagValue) {
		return XMLUtil.buildTag(tagName, Integer.toString(tagValue), false);
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param writer The writer to write the XML tag output to.
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @throws IOException Thrown if an error occurs while writing to the writer.
	 */
	public static void buildTag(Writer writer, String tagName, int tagValue) throws IOException {
		XMLUtil.buildTag(writer, tagName, Integer.toString(tagValue), false);
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, Integer tagValue) {
		return (tagValue == null) ? "" : XMLUtil.buildTag(tagName, tagValue.toString(), false);
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param writer The writer to write the XML tag output to.
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @throws IOException Thrown if an error occurs while writing to the writer.
	 */
	public static void buildTag(Writer writer, String tagName, Integer tagValue) throws IOException {
		XMLUtil.buildTag(writer, tagName, tagValue.toString(), false);
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, boolean tagValue) {
		return XMLUtil.buildTag(tagName, Boolean.toString(tagValue), false);
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param writer The writer to write the XML tag output to.
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @throws IOException Thrown if an error occurs while writing to the writer.
	 */
	public static void buildTag(Writer writer, String tagName, boolean tagValue) throws IOException {
		XMLUtil.buildTag(writer, tagName, Boolean.toString(tagValue), false);
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, Timestamp tagValue) {
		return (tagValue == null) ? "" : XMLUtil.buildTag(tagName, tagValue.toString(), false);
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param writer The writer to write the XML tag output to.
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @throws IOException Thrown if an error occurs while writing to the writer.
	 */
	public static void buildTag(Writer writer, String tagName, Timestamp tagValue) throws IOException {
		XMLUtil.buildTag(writer, tagName, tagValue.toString(), false);
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, long tagValue) {
		return XMLUtil.buildTag(tagName, Long.toString(tagValue), false);
	}

	/**
	 * Utility method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param writer The writer to write the XML tag output to.
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @throws IOException Thrown if an error occurs while writing to the writer.
	 */
	public static void buildTag(Writer writer, String tagName, long tagValue) throws IOException {
		XMLUtil.buildTag(writer, tagName, Long.toString(tagValue), false);
	}

	/**
	 * Get XML <code>Node</code> text content.  This method duplicates the
	 * org.w3c.dom.Node.getTextContent() method in JDK 1.5.
	 *
	 * @param baseNode The XML node from which the content is being retrieved.
	 * @return The text content of the XML node.
	 */
	public static String getTextContent(Node baseNode) {
		// if element, first child will be a text element with content
		Node child = baseNode.getFirstChild();
		if (child != null && child.getNodeType() == Node.TEXT_NODE) {
			return child.getNodeValue();
		}
		return "";
	}

	/**
	 * Given a <code>File</code> object that points to an XML file, parse the
	 * XML and return a parsed <code>Document</code> object.
	 *
	 * @param file The File object that points to the XML file.
	 * @param validating Set to <code>true</code> if the parser should
	 *  validate against a DTD.
	 * @return A parsed Document object.
	 * @throws FileNotFoundException Thrown if the XML file cannot be found.
	 * @throws ParseException Thrown if any error occurs during parsing.
	 */
	public static Document parseXML(File file, boolean validating) throws FileNotFoundException, ParseException {
		if (!file.exists()) {
			throw new FileNotFoundException("File " + file.getAbsolutePath() + " does not exist");
		}
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			InputSource source = new InputSource(stream);
			try {
				return XMLUtil.parseXML(source, validating);
			} catch (ParseException e) {
				// wrap the exception in order to report the file path
				ParseException pe = new ParseException("The file " + file.getAbsolutePath() + " could not be parsed", -1);
				pe.initCause(e);
				throw pe;
			}
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	/**
	 * Given an <code>InputSource</code> object that points to XML data, parse
	 * the XML and return a parsed <code>Document</code> object.
	 *
	 * @param source The InputSource object that points to XML data.
	 * @param validating Set to <code>true</code> if the parser should
	 *  validate against a DTD.
	 * @return A parsed Document object.
	 * @throws ParseException Thrown if any error occurs during parsing.
	 */
	public static Document parseXML(InputSource source, boolean validating) throws ParseException {
		// Create a builder factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(validating);
		// Create the builder and parse the file
		try {
			return factory.newDocumentBuilder().parse(source);
		} catch (IOException e) {
			ParseException pe = new ParseException("IO exception while parsing XML", -1);
			pe.initCause(e);
			throw pe;
		} catch (ParserConfigurationException e) {
			ParseException pe = new ParseException("XML could not be parsed", -1);
			pe.initCause(e);
			throw pe;
		} catch (SAXException e) {
			ParseException pe = new ParseException("XML contains invalid XML", -1);
			pe.initCause(e);
			throw pe;
		}
	}
}
