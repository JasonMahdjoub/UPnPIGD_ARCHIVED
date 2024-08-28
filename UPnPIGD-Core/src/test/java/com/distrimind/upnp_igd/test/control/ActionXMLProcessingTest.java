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

import com.distrimind.upnp_igd.mock.MockUpnpService;
import com.distrimind.upnp_igd.mock.MockUpnpServiceConfiguration;
import com.distrimind.upnp_igd.model.action.ActionException;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.StreamResponseMessage;
import com.distrimind.upnp_igd.model.message.UpnpMessage;
import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.model.message.control.IncomingActionRequestMessage;
import com.distrimind.upnp_igd.model.message.control.IncomingActionResponseMessage;
import com.distrimind.upnp_igd.model.message.control.OutgoingActionRequestMessage;
import com.distrimind.upnp_igd.model.message.control.OutgoingActionResponseMessage;
import com.distrimind.upnp_igd.model.message.header.ContentTypeHeader;
import com.distrimind.upnp_igd.model.message.header.SoapActionHeader;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.types.ErrorCode;
import com.distrimind.upnp_igd.model.types.SoapActionType;
import com.distrimind.upnp_igd.test.data.SampleData;
import com.distrimind.upnp_igd.transport.impl.PullSOAPActionProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.RecoveringSOAPActionProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.SOAPActionProcessorImpl;
import com.distrimind.upnp_igd.transport.spi.SOAPActionProcessor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.*;

public class ActionXMLProcessingTest {

    public static final String ENCODED_REQUEST = "<?xml version=\"1.0\"?>\n" +
            " <s:Envelope\n" +
            "     xmlns:s=\"https://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "     s:encodingStyle=\"https://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "   <s:Body>\n" +
            "     <u:SetSomeValue xmlns:u=\"urn:schemas-upnp-org:service:SwitchPower:1\">\n" +
            "       <SomeValue>This is encoded: &lt;</SomeValue>\n" +
            "     </u:SetSomeValue>\n" +
            "   </s:Body>\n" +
            " </s:Envelope>";

    public static final String ALIAS_ENCODED_REQUEST = "<?xml version=\"1.0\"?>\n" +
            " <s:Envelope\n" +
            "     xmlns:s=\"https://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "     s:encodingStyle=\"https://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "   <s:Body>\n" +
            "     <u:SetSomeValue xmlns:u=\"urn:schemas-upnp-org:service:SwitchPower:1\">\n" +
            "       <SomeValue1>This is encoded: &lt;</SomeValue1>\n" +
            "     </u:SetSomeValue>\n" +
            "   </s:Body>\n" +
            " </s:Envelope>";
    public static final String PROCESSORS = "processors";

    @DataProvider(name = PROCESSORS)
    public SOAPActionProcessor[][] getProcessors() {
        return new SOAPActionProcessor[][] {
            {new SOAPActionProcessorImpl()},
            {new PullSOAPActionProcessorImpl()},
            {new RecoveringSOAPActionProcessorImpl()}
        };
    }

    @Test(dataProvider = PROCESSORS)
    public void writeReadRequest(final SOAPActionProcessor processor) throws Exception {

        LocalDevice<?> ld = ActionSampleData.createTestDevice(ActionSampleData.LocalTestServiceExtended.class);
        LocalService<?> svc = ld.getServices().iterator().next();

        Action<?> action = svc.getAction("SetTarget");
        ActionInvocation<?> actionInvocation = new ActionInvocation<>(action);
        actionInvocation.setInput("NewTargetValue", true);

        // The control URL doesn't matter
        OutgoingActionRequestMessage outgoingCall = new OutgoingActionRequestMessage(actionInvocation, SampleData.getLocalBaseURL());

        MockUpnpService upnpService = new MockUpnpService(new MockUpnpServiceConfiguration() {
            @Override
            public SOAPActionProcessor getSoapActionProcessor() {
                return processor;
            }
        });

        upnpService.getConfiguration().getSoapActionProcessor().writeBody(outgoingCall, actionInvocation);

        StreamRequestMessage incomingStream = new StreamRequestMessage(outgoingCall);
        IncomingActionRequestMessage incomingCall = new IncomingActionRequestMessage(incomingStream, svc);

        actionInvocation = new ActionInvocation<>(incomingCall.getAction());

        upnpService.getConfiguration().getSoapActionProcessor().readBody(incomingCall, actionInvocation);

        assertEquals(actionInvocation.getInput().size(), 1);
        assertEquals(actionInvocation.getInput().iterator().next().getArgument().getName(), "NewTargetValue");
    }

    @Test(dataProvider = PROCESSORS)
    public void writeReadResponse(final SOAPActionProcessor processor) throws Exception {

        LocalDevice<?> ld = ActionSampleData.createTestDevice(ActionSampleData.LocalTestServiceExtended.class);
        LocalService<?> svc = ld.getServices().iterator().next();

        Action<?> action = svc.getAction("GetTarget");
        ActionInvocation<?> actionInvocation = new ActionInvocation<>(action);

        OutgoingActionResponseMessage outgoingCall = new OutgoingActionResponseMessage(action);
        actionInvocation.setOutput("RetTargetValue", true);

        MockUpnpService upnpService = new MockUpnpService(new MockUpnpServiceConfiguration() {
            @Override
            public SOAPActionProcessor getSoapActionProcessor() {
                return processor;
            }
        });

        upnpService.getConfiguration().getSoapActionProcessor().writeBody(outgoingCall, actionInvocation);

        StreamResponseMessage incomingStream = new StreamResponseMessage(outgoingCall);
        IncomingActionResponseMessage incomingCall = new IncomingActionResponseMessage(incomingStream);

        actionInvocation = new ActionInvocation<>(action);
        upnpService.getConfiguration().getSoapActionProcessor().readBody(incomingCall, actionInvocation);

        assertEquals(actionInvocation.getOutput().iterator().next().getArgument().getName(), "RetTargetValue");
    }

    @Test(dataProvider = PROCESSORS)
    public void writeFailureReadFailure(final SOAPActionProcessor processor) throws Exception {

        LocalDevice<?> ld = ActionSampleData.createTestDevice(ActionSampleData.LocalTestServiceExtended.class);
        LocalService<?> svc = ld.getServices().iterator().next();

        Action<?> action = svc.getAction("GetTarget");
        ActionInvocation<?> actionInvocation = new ActionInvocation<>(action);
        actionInvocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, "A test string"));

        OutgoingActionResponseMessage outgoingCall = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);

        MockUpnpService upnpService = new MockUpnpService(new MockUpnpServiceConfiguration() {
            @Override
            public SOAPActionProcessor getSoapActionProcessor() {
                return processor;
            }
        });

        upnpService.getConfiguration().getSoapActionProcessor().writeBody(outgoingCall, actionInvocation);

        StreamResponseMessage incomingStream = new StreamResponseMessage(outgoingCall);
        IncomingActionResponseMessage incomingCall = new IncomingActionResponseMessage(incomingStream);

        actionInvocation = new ActionInvocation<>(action);
        upnpService.getConfiguration().getSoapActionProcessor().readBody(incomingCall, actionInvocation);

        assertEquals(actionInvocation.getFailure().getErrorCode(), ErrorCode.ACTION_FAILED.getCode());
        // Note the period at the end of the test string!
        assertEquals(actionInvocation.getFailure().getMessage(), ErrorCode.ACTION_FAILED.getDescription() + ". A test string.");
    }

    @Test(dataProvider = PROCESSORS)
    public void readEncodedRequest(final SOAPActionProcessor processor) throws Exception {

        LocalDevice<?> ld = ActionSampleData.createTestDevice(ActionSampleData.LocalTestServiceExtended.class);
        LocalService<?> svc = ld.getServices().iterator().next();

        Action<?> action = svc.getAction("SetSomeValue");
        ActionInvocation<?> actionInvocation = new ActionInvocation<>(action);

        MockUpnpService upnpService = new MockUpnpService(new MockUpnpServiceConfiguration() {
            @Override
            public SOAPActionProcessor getSoapActionProcessor() {
                return processor;
            }
        });

        StreamRequestMessage streamRequest = new StreamRequestMessage(UpnpRequest.Method.POST, URI.create("https://some.uri"));
        streamRequest.getHeaders().add(
                UpnpHeader.Type.CONTENT_TYPE,
                new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8)
        );
        streamRequest.getHeaders().add(
                UpnpHeader.Type.SOAPACTION,
                new SoapActionHeader(
                        new SoapActionType(
                                action.getService().getServiceType(),
                                action.getName()
                        )
                )
        );
        streamRequest.setBody(UpnpMessage.BodyType.STRING, ENCODED_REQUEST);

        IncomingActionRequestMessage request = new IncomingActionRequestMessage(streamRequest, svc);

        upnpService.getConfiguration().getSoapActionProcessor().readBody(request, actionInvocation);

        assertEquals(actionInvocation.getInput().iterator().next().toString(), "This is encoded: <");

    }

    @Test(dataProvider = PROCESSORS)
    public void readEncodedRequestWithAlias(final SOAPActionProcessor processor) throws Exception {

        LocalDevice<?> ld = ActionSampleData.createTestDevice(ActionSampleData.LocalTestServiceExtended.class);
        LocalService<?> svc = ld.getServices().iterator().next();

        Action<?> action = svc.getAction("SetSomeValue");
        ActionInvocation<?> actionInvocation = new ActionInvocation<>(action);

        MockUpnpService upnpService = new MockUpnpService(new MockUpnpServiceConfiguration() {
            @Override
            public SOAPActionProcessor getSoapActionProcessor() {
                return processor;
            }
        });

        StreamRequestMessage streamRequest = new StreamRequestMessage(UpnpRequest.Method.POST, URI.create("http://some.uri"));
        streamRequest.getHeaders().add(
                UpnpHeader.Type.CONTENT_TYPE,
                new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8)
        );
        streamRequest.getHeaders().add(
                UpnpHeader.Type.SOAPACTION,
                new SoapActionHeader(
                        new SoapActionType(
                                action.getService().getServiceType(),
                                action.getName()
                        )
                )
        );
        streamRequest.setBody(UpnpMessage.BodyType.STRING, ALIAS_ENCODED_REQUEST);

        IncomingActionRequestMessage request = new IncomingActionRequestMessage(streamRequest, svc);

        upnpService.getConfiguration().getSoapActionProcessor().readBody(request, actionInvocation);

        assertEquals(actionInvocation.getInput().iterator().next().toString(), "This is encoded: <");
        assertEquals(actionInvocation.getInput("SomeValue").toString(), "This is encoded: <");

    }

    @Test(dataProvider = PROCESSORS)
    public void writeDecodedResponse(final SOAPActionProcessor processor) throws Exception {

        LocalDevice<?> ld = ActionSampleData.createTestDevice(ActionSampleData.LocalTestServiceExtended.class);
        LocalService<?> svc = ld.getServices().iterator().next();

        Action<?> action = svc.getAction("GetSomeValue");
        ActionInvocation<?> actionInvocation = new ActionInvocation<>(action);

        MockUpnpService upnpService = new MockUpnpService(new MockUpnpServiceConfiguration() {
            @Override
            public SOAPActionProcessor getSoapActionProcessor() {
                return processor;
            }
        });

        OutgoingActionResponseMessage response = new OutgoingActionResponseMessage(action);
        actionInvocation.setOutput("SomeValue", "This is decoded: &<>'\"");

        upnpService.getConfiguration().getSoapActionProcessor().writeBody(response, actionInvocation);

        // Note that quotes are not encoded because this text is not an XML attribute value!
        assertTrue(response.getBodyString().contains("<SomeValue>This is decoded: &amp;&lt;&gt;'\"</SomeValue>"));
    }
}
