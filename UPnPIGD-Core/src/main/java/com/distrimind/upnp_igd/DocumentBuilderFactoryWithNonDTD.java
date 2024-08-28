/*
 * Copyright or © or Corp. Jason Mahdjoub (01/04/2013)
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.distrimind.upnp_igd;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import java.util.Locale;

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MaDKitLanEdition 5.19.0
 */
public class DocumentBuilderFactoryWithNonDTD extends DocumentBuilderFactory {

	public static DocumentBuilderFactoryWithNonDTD newDocumentBuilderFactoryWithNonDTDInstance()
	{
		return newDocumentBuilderFactoryWithNonDTDInstance(false);
	}
	public static DocumentBuilderFactoryWithNonDTD newDocumentBuilderFactoryWithNonDTDInstance(boolean enableDocType)
	{
		if (enableDocType) {
			try {
				return new DocumentBuilderFactoryWithNonDTD(true);
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
		return (DocumentBuilderFactoryWithNonDTD)DocumentBuilderFactory.newInstance(DocumentBuilderFactoryWithNonDTD.class.getName(), ClassLoader.getSystemClassLoader());
	}
	private final DocumentBuilderFactory base;
	public DocumentBuilderFactoryWithNonDTD() throws ParserConfigurationException {
		this(false);
	}
	public DocumentBuilderFactoryWithNonDTD(boolean enableDocType) throws ParserConfigurationException {
		base=DocumentBuilderFactory.newInstance();
		base.setAttribute("http://apache.org/xml/properties/locale", Locale.ROOT);
		base.setValidating(false);
		base.setNamespaceAware(true);
		base.setCoalescing(false);
		base.setFeature("http://xml.org/sax/features/validation", false);
		base.setFeature("http://apache.org/xml/features/disallow-doctype-decl", !enableDocType);
		base.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		base.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		base.setFeature("http://xml.org/sax/features/external-general-entities", false);
		base.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		base.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		base.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
		base.setFeature("http://apache.org/xml/features/xinclude/fixup-language", false);
		base.setXIncludeAware(false);
		base.setExpandEntityReferences(false);
		base.setFeature("http://apache.org/xml/features/validation/dynamic", true);
	}

	@Override
	public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return base.newDocumentBuilder();
	}

	@Override
	public void setAttribute(String name, Object value) throws IllegalArgumentException {
		base.setAttribute(name, value);
	}

	@Override
	public Object getAttribute(String name) throws IllegalArgumentException {
		return base.getAttribute(name);
	}

	@Override
	public void setFeature(String name, boolean value) throws ParserConfigurationException {
		base.setFeature(name, value);
	}

	@Override
	public boolean getFeature(String name) throws ParserConfigurationException {
		return base.getFeature(name);
	}


	public static DocumentBuilderFactory newInstance(String factoryClassName, ClassLoader classLoader) {
		return DocumentBuilderFactory.newInstance(factoryClassName, classLoader);
	}

	@Override
	public void setNamespaceAware(boolean awareness) {
		base.setNamespaceAware(awareness);
	}

	@Override
	public void setValidating(boolean validating) {
		base.setValidating(validating);
	}

	@Override
	public void setIgnoringElementContentWhitespace(boolean whitespace) {
		base.setIgnoringElementContentWhitespace(whitespace);
	}

	@Override
	public void setExpandEntityReferences(boolean expandEntityRef) {
		base.setExpandEntityReferences(expandEntityRef);
	}

	@Override
	public void setIgnoringComments(boolean ignoreComments) {
		base.setIgnoringComments(ignoreComments);
	}

	@Override
	public void setCoalescing(boolean coalescing) {
		base.setCoalescing(coalescing);
	}

	@Override
	public boolean isNamespaceAware() {
		return base.isNamespaceAware();
	}

	@Override
	public boolean isValidating() {
		return base.isValidating();
	}

	@Override
	public boolean isIgnoringElementContentWhitespace() {
		return base.isIgnoringElementContentWhitespace();
	}

	@Override
	public boolean isExpandEntityReferences() {
		return base.isExpandEntityReferences();
	}

	@Override
	public boolean isIgnoringComments() {
		return base.isIgnoringComments();
	}

	@Override
	public boolean isCoalescing() {
		return base.isCoalescing();
	}

	@Override
	public Schema getSchema() {
		return base.getSchema();
	}

	@Override
	public void setSchema(Schema schema) {
		base.setSchema(schema);
	}

	@Override
	public void setXIncludeAware(boolean state) {
		base.setXIncludeAware(state);
	}

	@Override
	public boolean isXIncludeAware() {
		return base.isXIncludeAware();
	}

}
