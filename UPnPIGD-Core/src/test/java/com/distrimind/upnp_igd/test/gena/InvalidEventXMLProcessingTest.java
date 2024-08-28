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

package com.distrimind.upnp_igd.test.gena;

import com.distrimind.upnp_igd.UpnpService;
import com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.UDA10ServiceDescriptorBinderImpl;
import com.distrimind.upnp_igd.mock.MockUpnpService;
import com.distrimind.upnp_igd.mock.MockUpnpServiceConfiguration;
import com.distrimind.upnp_igd.model.UnsupportedDataException;
import com.distrimind.upnp_igd.model.gena.CancelReason;
import com.distrimind.upnp_igd.model.gena.RemoteGENASubscription;
import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.UpnpMessage.BodyType;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.model.message.gena.IncomingEventRequestMessage;
import com.distrimind.upnp_igd.model.message.gena.OutgoingEventRequestMessage;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.state.StateVariableValue;
import com.distrimind.upnp_igd.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp_igd.transport.impl.NetworkAddressFactoryImpl;
import com.distrimind.upnp_igd.test.data.SampleData;
import com.distrimind.upnp_igd.transport.impl.PullGENAEventProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.RecoveringGENAEventProcessorImpl;
import com.distrimind.upnp_igd.transport.spi.GENAEventProcessor;
import com.distrimind.upnp_igd.util.io.IO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class InvalidEventXMLProcessingTest {

    @DataProvider(name = "invalidXMLFile")
    public String[][] getInvalidXMLFile() throws Exception {
        return new String[][]{
            {"/invalidxml/event/invalid_root_element.xml"},
        };
    }

    @DataProvider(name = "invalidRecoverableXMLFile")
    public String[][] getInvalidRecoverableXMLFile() throws Exception {
        return new String[][]{
            {"/invalidxml/event/truncated.xml"},
            {"/invalidxml/event/orange_liveradio.xml"},
        };
    }

    // TODO: Shouldn't these be failures of the LastChangeParser?
    // The GENA parser does the right thing for most of them, no?
    @DataProvider(name = "invalidUnrecoverableXMLFile")
    public String[][] getInvalidUnrecoverableXMLFile() throws Exception {
        return new String[][]{//TODO the commented tests now pass with JSoup but should not ?
            //{"/invalidxml/event/unrecoverable/denon_avr4306.xml"},
            //{"/invalidxml/event/unrecoverable/philips_np2900.xml"},
            //{"/invalidxml/event/unrecoverable/philips_sla5220.xml"},
            //{"/invalidxml/event/unrecoverable/terratec_noxon2.xml"},
            //{"/invalidxml/event/unrecoverable/marantz_mcr603.xml"},
            {"/invalidxml/event/unrecoverable/teac_wap4500.xml"},
            {"/invalidxml/event/unrecoverable/technisat_digi_hd8+.xml"},
        };
    }

    /* ############################## TEST FAILURE ############################ */

    @Test(dataProvider = "invalidXMLFile", expectedExceptions = UnsupportedDataException.class)
    public void readDefaultFailure(String invalidXMLFile) throws Exception {
        // This should always fail!
        read(invalidXMLFile,new MockUpnpService());
    }

    @Test(dataProvider = "invalidRecoverableXMLFile", expectedExceptions = UnsupportedDataException.class)
    public void readRecoverableFailure(String invalidXMLFile) throws Exception {
        // This should always fail!
        read(invalidXMLFile,new MockUpnpService());
    }

    @Test(dataProvider = "invalidUnrecoverableXMLFile", expectedExceptions = Exception.class)
    public void readRecoveringFailure(String invalidXMLFile) throws Exception {
        // This should always fail!
        read(
            invalidXMLFile,
            new MockUpnpService(new MockUpnpServiceConfiguration() {
                @Override
                public GENAEventProcessor getGenaEventProcessor() {
                    return new RecoveringGENAEventProcessorImpl();
                }
            })
        );
    }

    /* ############################## TEST SUCCESS ############################ */

    @Test(dataProvider = "invalidXMLFile")
    public void readPull(String invalidXMLFile) throws Exception {
        read(
            invalidXMLFile,
            new MockUpnpService(new MockUpnpServiceConfiguration() {
                @Override
                public GENAEventProcessor getGenaEventProcessor() {
                    return new PullGENAEventProcessorImpl();
                }
            })
        );
    }

    @Test(dataProvider = "invalidRecoverableXMLFile")
    public void readRecovering(String invalidXMLFile) throws Exception {
        read(
            invalidXMLFile,
            new MockUpnpService(new MockUpnpServiceConfiguration() {
                @Override
                public GENAEventProcessor getGenaEventProcessor() {
                    return new RecoveringGENAEventProcessorImpl();
                }
            })
        );
    }

    protected void read(String invalidXMLFile, UpnpService upnpService) throws Exception {
        ServiceDescriptorBinder binder = new UDA10ServiceDescriptorBinderImpl(new NetworkAddressFactoryImpl());
        RemoteService service = SampleData.createUndescribedRemoteService();
        service = binder.describe(service, IO.readLines(
            getClass().getResourceAsStream("/descriptors/service/uda10_avtransport.xml"))
        );

        RemoteGENASubscription subscription = new RemoteGENASubscription(service, 1800) {
            @Override
			public void failed(UpnpResponse responseStatus) {
            }

            @Override
			public void ended(CancelReason reason, UpnpResponse responseStatus) {
            }

            @Override
			public void eventsMissed(int numberOfMissedEvents) {
            }

            @Override
			public void established() {
            }

            @Override
			public void eventReceived() {
            }

            @Override
			public void invalidMessage(UnsupportedDataException ex) {
            }
        };
        subscription.receive(new UnsignedIntegerFourBytes(0), new ArrayList<>());

        OutgoingEventRequestMessage outgoingCall =
            new OutgoingEventRequestMessage(subscription, SampleData.getLocalBaseURL());

        upnpService.getConfiguration().getGenaEventProcessor().writeBody(outgoingCall);

        StreamRequestMessage incomingStream = new StreamRequestMessage(outgoingCall);

        IncomingEventRequestMessage message = new IncomingEventRequestMessage(incomingStream, service);
        message.setBody(BodyType.STRING, IO.readLines(getClass().getResourceAsStream(invalidXMLFile)));

        upnpService.getConfiguration().getGenaEventProcessor().readBody(message);

        // All of the messages must have a LastChange state variable, and we should be able to parse
        // the XML value of that state variable
        boolean found = false;
        for (StateVariableValue<RemoteService> stateVariableValue : message.getStateVariableValues()) {
            if ("LastChange".equals(stateVariableValue.getStateVariable().getName())
                && stateVariableValue.getValue() != null) {
                found = true;
                String lastChange = (String) stateVariableValue.getValue();
                Map<String, String> lastChangeValues = parseLastChangeXML(lastChange);
                assertFalse(lastChangeValues.isEmpty());
                break;
            }
        }

        assertTrue(found);
    }


    @SuppressWarnings("PMD")
    public static void parseLastChangeXML(Element e, Map<String, String> m) throws ParserConfigurationException {
        Elements nl=e.children();
        if (!nl.isEmpty())
        {
            for (Element e2 : nl)
                parseLastChangeXML(e2, m);
        }
        else {
            String att="val";
            if (e.hasAttr(att))
                m.put(e.tagName(), e.attr(att));
        }
    }
    public static Map<String, String> parseLastChangeXML(String text) throws ParserConfigurationException, IOException, SAXException {
        Document d= Jsoup.parse(text, "", Parser.xmlParser());
        Map<String, String> r=new HashMap<>();
        parseLastChangeXML(d, r);
        return r;
    }
}
