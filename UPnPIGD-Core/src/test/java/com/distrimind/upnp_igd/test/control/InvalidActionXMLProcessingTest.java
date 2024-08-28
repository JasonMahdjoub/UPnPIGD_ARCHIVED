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

package com.distrimind.upnp_igd.test.control;

import com.distrimind.upnp_igd.UpnpService;
import com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.UDA10ServiceDescriptorBinderImpl;
import com.distrimind.upnp_igd.mock.MockUpnpService;
import com.distrimind.upnp_igd.mock.MockUpnpServiceConfiguration;
import com.distrimind.upnp_igd.model.UnsupportedDataException;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.StreamResponseMessage;
import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.model.message.control.IncomingActionRequestMessage;
import com.distrimind.upnp_igd.model.message.control.IncomingActionResponseMessage;
import com.distrimind.upnp_igd.model.message.header.ContentTypeHeader;
import com.distrimind.upnp_igd.model.message.header.SoapActionHeader;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.types.SoapActionType;
import com.distrimind.upnp_igd.transport.impl.NetworkAddressFactoryImpl;
import com.distrimind.upnp_igd.test.data.SampleData;
import com.distrimind.upnp_igd.transport.impl.PullSOAPActionProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.RecoveringSOAPActionProcessorImpl;
import com.distrimind.upnp_igd.transport.spi.SOAPActionProcessor;
import com.distrimind.upnp_igd.util.io.IO;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class InvalidActionXMLProcessingTest {

    @DataProvider(name = "invalidXMLFile")
    public String[][] getInvalidXMLFile() throws Exception {
        return new String[][]{
            {"/invalidxml/control/request_missing_envelope.xml"},
            {"/invalidxml/control/request_missing_action_namespace.xml"},
            {"/invalidxml/control/request_invalid_action_namespace.xml"},
        };
    }

    @DataProvider(name = "invalidRecoverableXMLFile")
    public String[][] getInvalidRecoverableXMLFile() throws Exception {
        return new String[][]{
            {"/invalidxml/control/request_no_entityencoding.xml"},
            {"/invalidxml/control/request_wrong_termination.xml"},
        };
    }

    @DataProvider(name = "invalidUnrecoverableXMLFile")
    public String[][] getInvalidUnrecoverableXMLFile() throws Exception {
        return new String[][]{
            {"/invalidxml/control/unrecoverable/naim_unity.xml"},
        };
    }

    /* ############################## TEST FAILURE ############################ */

    @Test(dataProvider = "invalidXMLFile", expectedExceptions = UnsupportedDataException.class)
    public void readRequestDefaultFailure(String invalidXMLFile) throws Exception {
        // This should always fail!
        readRequest(invalidXMLFile, new MockUpnpService());
    }

    @Test(dataProvider = "invalidRecoverableXMLFile", expectedExceptions = UnsupportedDataException.class)
    public void readRequestRecoverableFailure(String invalidXMLFile) throws Exception {
        // This should always fail!
        readRequest(invalidXMLFile, new MockUpnpService());
    }

    @Test(dataProvider = "invalidUnrecoverableXMLFile", expectedExceptions = Exception.class)
    public void readRequestRecoveringFailure(String invalidXMLFile) throws Exception {
        // This should always fail!
        readRequest(
            invalidXMLFile,
            new MockUpnpService(new MockUpnpServiceConfiguration() {
                @Override
                public SOAPActionProcessor getSoapActionProcessor() {
                    return new RecoveringSOAPActionProcessorImpl();
                }
            })
        );
    }

    /* ############################## TEST SUCCESS ############################ */

    @Test(dataProvider = "invalidXMLFile")
    public void readRequestPull(String invalidXMLFile) throws Exception {
        readRequest(
            invalidXMLFile,
            new MockUpnpService(new MockUpnpServiceConfiguration() {
                @Override
                public SOAPActionProcessor getSoapActionProcessor() {
                    return new PullSOAPActionProcessorImpl();
                }
            })
        );
    }

    @Test(dataProvider = "invalidRecoverableXMLFile")
    public void readRequestRecovering(String invalidXMLFile) throws Exception {
        readRequest(
            invalidXMLFile,
            new MockUpnpService(new MockUpnpServiceConfiguration() {
                @Override
                public SOAPActionProcessor getSoapActionProcessor() {
                    return new RecoveringSOAPActionProcessorImpl();
                }
            })
        );
    }

    @Test
   	public void uppercaseOutputArguments() throws Exception {
   		SOAPActionProcessor processor = new RecoveringSOAPActionProcessorImpl();
   		ServiceDescriptorBinder binder = new UDA10ServiceDescriptorBinderImpl(new NetworkAddressFactoryImpl());

   		RemoteService service = SampleData.createUndescribedRemoteService();
   		service = binder.describe(
               service,
               IO.readLines(getClass().getResourceAsStream("/descriptors/service/uda10_connectionmanager.xml"))
           );

   		Action<?> action = service.getAction("GetProtocolInfo");

   		ActionInvocation<?> actionInvocation = new ActionInvocation<>(action);
   		StreamResponseMessage response = new StreamResponseMessage(
               IO.readLines(getClass().getResourceAsStream("/invalidxml/control/response_uppercase_args.xml"))
           );

   		processor.readBody(new IncomingActionResponseMessage(response), actionInvocation);
   	}

    protected void readRequest(String invalidXMLFile, UpnpService upnpService) throws Exception {
        LocalDevice<?> ld = ActionSampleData.createTestDevice(ActionSampleData.LocalTestServiceExtended.class);
        LocalService<?> svc = ld.getServices().iterator().next();

        Action<?> action = svc.getAction("SetSomeValue");
        ActionInvocation<?> actionInvocation = new ActionInvocation<>(action);

        StreamRequestMessage message = createRequestMessage(action, invalidXMLFile);
        IncomingActionRequestMessage request = new IncomingActionRequestMessage(message, svc);

        upnpService.getConfiguration().getSoapActionProcessor().readBody(request, actionInvocation);

        assertEquals(actionInvocation.getInput().iterator().next().toString(), "foo&bar");
    }

    public StreamRequestMessage createRequestMessage(Action<?> action, String xmlFile) throws Exception {
        StreamRequestMessage message =
            new StreamRequestMessage(UpnpRequest.Method.POST, URI.create("https://some.uri"));

        message.getHeaders().add(
            UpnpHeader.Type.CONTENT_TYPE,
            new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8)
        );
        message.getHeaders().add(
            UpnpHeader.Type.SOAPACTION,
            new SoapActionHeader(
                new SoapActionType(
                    action.getService().getServiceType(),
                    action.getName()
                )
            )
        );
        message.setBody(IO.readLines(getClass().getResourceAsStream(xmlFile)));
        return message;
    }
}
