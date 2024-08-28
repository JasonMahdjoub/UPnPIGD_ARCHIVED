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

import com.distrimind.upnp_igd.mock.MockUpnpService;
import com.distrimind.upnp_igd.mock.MockUpnpServiceConfiguration;
import com.distrimind.upnp_igd.model.gena.CancelReason;
import com.distrimind.upnp_igd.model.gena.LocalGENASubscription;
import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.gena.IncomingEventRequestMessage;
import com.distrimind.upnp_igd.model.message.gena.OutgoingEventRequestMessage;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.state.StateVariableValue;
import com.distrimind.upnp_igd.test.data.SampleData;
import com.distrimind.upnp_igd.transport.impl.GENAEventProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.PullGENAEventProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.RecoveringGENAEventProcessorImpl;
import com.distrimind.upnp_igd.transport.spi.GENAEventProcessor;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class EventXMLProcessingTest {

    public static final String EVENT_MSG =
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>" +
            "<e:propertyset xmlns:e=\"urn:schemas-upnp-org:event-1-0\">" +
            "<e:property>" +
            "<Status>0</Status>" +
            "</e:property>" +
            "<e:property>" +
            "<SomeVar></SomeVar>" +
            "</e:property>" +
            "</e:propertyset>";

    @Test
    public void writeReadRequest() throws Exception {
        MockUpnpService upnpService = new MockUpnpService(new MockUpnpServiceConfiguration(){
            @Override
            public GENAEventProcessor getGenaEventProcessor() {
                return new GENAEventProcessorImpl();
            }
        });
        writeReadRequest(upnpService);
    }

    @Test
    public void writeReadRequestPull() throws Exception {
        MockUpnpService upnpService = new MockUpnpService(new MockUpnpServiceConfiguration(){
            @Override
            public GENAEventProcessor getGenaEventProcessor() {
                return new PullGENAEventProcessorImpl();
            }
        });
        writeReadRequest(upnpService);
    }

    @Test
    public void writeReadRequestRecovering() throws Exception {
        MockUpnpService upnpService = new MockUpnpService(new MockUpnpServiceConfiguration(){
            @Override
            public GENAEventProcessor getGenaEventProcessor() {
                return new RecoveringGENAEventProcessorImpl();
            }
        });
        writeReadRequest(upnpService);
    }

    public void writeReadRequest(MockUpnpService upnpService) throws Exception {

        LocalDevice<GenaSampleData.LocalTestService> localDevice = GenaSampleData.createTestDevice(GenaSampleData.LocalTestService.class);
        LocalService<GenaSampleData.LocalTestService> localService = localDevice.getServices().iterator().next();

        OutgoingEventRequestMessage outgoingCall = getOutgoingEventRequestMessage(localService);

        upnpService.getConfiguration().getGenaEventProcessor().writeBody(outgoingCall);

        assertEquals(outgoingCall.getBody(), EVENT_MSG);

        StreamRequestMessage incomingStream = new StreamRequestMessage(outgoingCall);

        RemoteDevice remoteDevice = SampleData.createRemoteDevice();
        RemoteService remoteService = SampleData.getFirstService(remoteDevice);

        IncomingEventRequestMessage incomingCall = new IncomingEventRequestMessage(incomingStream, remoteService);

        upnpService.getConfiguration().getGenaEventProcessor().readBody(incomingCall);

        assertEquals(incomingCall.getStateVariableValues().size(), 2);

        boolean gotValueOne = false;
        boolean gotValueTwo = false;
        for (StateVariableValue<?> stateVariableValue : incomingCall.getStateVariableValues()) {
            if ("Status".equals(stateVariableValue.getStateVariable().getName())) {
                gotValueOne = (!(Boolean) stateVariableValue.getValue());
            }
            if ("SomeVar".equals(stateVariableValue.getStateVariable().getName())) {
                // TODO: So... can it be null at all? It has a default value...
                gotValueTwo = stateVariableValue.getValue() == null;
            }
        }
        assertTrue(gotValueOne && gotValueTwo);
    }

    private static OutgoingEventRequestMessage getOutgoingEventRequestMessage(LocalService<GenaSampleData.LocalTestService> localService) throws Exception {
        List<URL> urls = new ArrayList<>() {
            private static final long serialVersionUID = 1L;
            {
			add(SampleData.getLocalBaseURL());
		}};

        LocalGENASubscription<GenaSampleData.LocalTestService> subscription =
                new LocalGENASubscription<>(localService, 1800, urls) {

                    @Override
					public void ended(CancelReason reason) {

                    }

                    @Override
					public void established() {

                    }

                    @Override
					public void eventReceived() {

                    }
                };

		return new OutgoingEventRequestMessage(subscription, subscription.getCallbackURLs().get(0));
    }


}
