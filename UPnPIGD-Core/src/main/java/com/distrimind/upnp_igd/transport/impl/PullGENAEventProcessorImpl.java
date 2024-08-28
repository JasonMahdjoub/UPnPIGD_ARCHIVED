/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
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

package com.distrimind.upnp_igd.transport.impl;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.distrimind.upnp_igd.model.message.gena.IncomingEventRequestMessage;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.meta.StateVariable;
import com.distrimind.upnp_igd.model.state.StateVariableValue;
import com.distrimind.upnp_igd.transport.spi.GENAEventProcessor;
import com.distrimind.upnp_igd.model.UnsupportedDataException;
import com.distrimind.upnp_igd.xml.XmlPullParserUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import jakarta.enterprise.inject.Alternative;

/**
 * Implementation based on the <em>Xml Pull Parser</em> XML processing API.
 * <p>
 * This processor is more lenient with parsing, looking only for the required XML tags.
 * </p>
 * <p>
 * To use this parser you need to install an implementation of the
 * <a href="http://www.xmlpull.org/impls.shtml">XMLPull API</a>.
 * </p>
 *
 * @author Michael Pujos
 */
@Alternative
public class PullGENAEventProcessorImpl extends GENAEventProcessorImpl {

	private static final Logger log = Logger.getLogger(GENAEventProcessor.class.getName());

	@Override
	public void readBody(IncomingEventRequestMessage requestMessage) throws UnsupportedDataException {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Reading body of: " + requestMessage);
		}
		if (log.isLoggable(Level.FINER)) {
            log.finer("===================================== GENA BODY BEGIN ============================================");
            log.finer(requestMessage.getBody() != null ? requestMessage.getBody().toString() : null);
            log.finer("-===================================== GENA BODY END ============================================");
        }

        String body = getMessageBody(requestMessage);
		try {
			Document d= Jsoup.parse(body, "", Parser.xmlParser());
			readProperties(d, requestMessage);
		} catch (Exception ex) {
			throw new UnsupportedDataException("Can't transform message payload: " + ex.getMessage(), ex, body);	
		}
	}
	protected void readProperties(Element e, IncomingEventRequestMessage message) throws Exception {
		Collection<StateVariable<RemoteService>> stateVariables = message.getService().getStateVariables();

		for (Element n : e.children())
		{
			if (XmlPullParserUtils.tagsEquals(n.tagName(), "property"))
			{
				readProperty(n, message, stateVariables);
			}
			readProperties(n, message);

		}
		if (message.getStateVariableValues().isEmpty())
			throw new Exception("There is no state variables !");
	}
	private void getAllText(StringBuilder text, Element element, String tagName) {
		String e=element.text();

		if (e.isEmpty()) {
			if (tagName!=null) {
				text.append("<")
						.append(tagName);
				for (Attribute a : element.attributes()) {
					text.append(" ")
							.append(a.getKey())
							.append("=")
							.append("\"")
							.append(a.getValue())
							.append("\"");
				}
				text.append(">");
			}
			for (Element child : element.children()) {
				getAllText(text, child, child.tagName());
			}
			if (tagName!=null)
				text.append("</")
						.append(tagName)
						.append(">");
		}
		else
			text.append(e);

	}
	private String getAllText(Element element) {
		StringBuilder text = new StringBuilder(element.text());
		if (text.length()==0) {
			getAllText(text, element, null);
		}
		return text.toString();

	}
	protected void readProperty(Element element, IncomingEventRequestMessage message, Collection<StateVariable<RemoteService>> stateVariables) throws Exception {
		String stateVariableName = element.tagName();
		for (StateVariable<RemoteService> stateVariable : stateVariables) {
			if (stateVariable.getName().equals(stateVariableName)) {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Reading state variable value: " + stateVariableName);
				}
				String value = getAllText(element);
				message.getStateVariableValues().add(new StateVariableValue<>(stateVariable, value));
				break;
			}
		}
		for (Element e : element.children())
			readProperty(e, message, stateVariables);
	}

}
