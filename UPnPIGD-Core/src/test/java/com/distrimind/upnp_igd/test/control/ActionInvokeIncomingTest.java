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
import com.distrimind.upnp_igd.mock.MockUpnpService;
import com.distrimind.upnp_igd.model.action.ActionException;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.message.Connection;
import com.distrimind.upnp_igd.model.message.StreamRequestMessage;
import com.distrimind.upnp_igd.model.message.StreamResponseMessage;
import com.distrimind.upnp_igd.model.message.UpnpMessage;
import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.model.message.control.IncomingActionResponseMessage;
import com.distrimind.upnp_igd.model.message.header.ContentTypeHeader;
import com.distrimind.upnp_igd.model.message.header.EXTHeader;
import com.distrimind.upnp_igd.model.message.header.ServerHeader;
import com.distrimind.upnp_igd.model.message.header.SoapActionHeader;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.model.message.header.UserAgentHeader;
import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.meta.QueryStateVariableAction;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.types.ErrorCode;
import com.distrimind.upnp_igd.model.types.SoapActionType;
import com.distrimind.upnp_igd.protocol.sync.ReceivingAction;
import com.distrimind.upnp_igd.util.MimeType;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import static org.testng.Assert.*;

/**
 * @author Christian Bauer
 */
public class ActionInvokeIncomingTest {

    public static final String SET_REQUEST = "<?xml version=\"1.0\"?>\n" +
            " <s:Envelope\n" +
            "     xmlns:s=\"https://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "     s:encodingStyle=\"https://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "   <s:Body>\n" +
            "     <u:SetTarget xmlns:u=\"urn:schemas-upnp-org:service:SwitchPower:1\">\n" +
            "       <NewTargetValue>1</NewTargetValue>\n" +
            "     </u:SetTarget>\n" +
            "   </s:Body>\n" +
            " </s:Envelope>";

    public static final String GET_REQUEST = "<?xml version=\"1.0\"?>\n" +
            " <s:Envelope\n" +
            "     xmlns:s=\"https://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "     s:encodingStyle=\"https://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "   <s:Body>\n" +
            "     <u:GetTarget xmlns:u=\"urn:schemas-upnp-org:service:SwitchPower:1\"/>\n" +
            "   </s:Body>\n" +
            " </s:Envelope>";

    public static final String QUERY_STATE_VARIABLE_REQUEST = "<?xml version=\"1.0\"?>\n" +
            " <s:Envelope\n" +
            "     xmlns:s=\"https://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "     s:encodingStyle=\"https://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "   <s:Body>\n" +
            "     <u:QueryStateVariable xmlns:u=\"urn:schemas-upnp-org:control-1-0\">\n" +
            "       <varName>Status</varName>\n" +
            "     </u:QueryStateVariable>\n" +
            "   </s:Body>\n" +
            " </s:Envelope>";

    @Test
    public void incomingRemoteCallGet() throws Exception {
        incomingRemoteCallGet(ActionSampleData.createTestDevice());
    }

    @Test
    public void incomingRemoteCallClientInfo() throws Exception {
        UpnpMessage<?> response =
                incomingRemoteCallGet(ActionSampleData.createTestDevice(ActionSampleData.LocalTestServiceWithClientInfo.class));

        assertEquals(response.getHeaders().size(), 4);
        assertEquals(response.getHeaders().getFirstHeader("X-MY-HEADER"), "foobar");
    }

    public <T> IncomingActionResponseMessage incomingRemoteCallGet(LocalDevice<T> ld) throws Exception {

        MockUpnpService upnpService = new MockUpnpService();
        LocalService<T> service = ld.getServices().iterator().next();
        upnpService.getRegistry().addDevice(ld);

        Action<LocalService<T>> action = service.getAction("GetTarget");

        URI controlURI = upnpService.getConfiguration().getNamespace().getControlPath(service);
        StreamRequestMessage request = getStreamRequestMessage(controlURI);
        addMandatoryRequestHeaders(service, action, request);
        request.setBody(UpnpMessage.BodyType.STRING, GET_REQUEST);

        ReceivingAction prot = new ReceivingAction(upnpService, request);

        prot.run();

        StreamResponseMessage response = prot.getOutputMessage();

        assertNotNull(response);
        assertFalse(response.getOperation().isFailed());
        assertTrue(response.getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class).isUDACompliantXML());
        assertNotNull(response.getHeaders().getFirstHeader(UpnpHeader.Type.EXT, EXTHeader.class));
        assertEquals(
            response.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue(),
            new ServerHeader().getValue()
        );

        IncomingActionResponseMessage responseMessage = new IncomingActionResponseMessage(response);
        ActionInvocation<?> responseInvocation = new ActionInvocation<>(action);
        upnpService.getConfiguration().getSoapActionProcessor().readBody(responseMessage, responseInvocation);

        assertNotNull(responseInvocation.getOutput("RetTargetValue"));
        return responseMessage;
    }

    private static StreamRequestMessage getStreamRequestMessage(URI controlURI) {
        StreamRequestMessage request = new StreamRequestMessage(UpnpRequest.Method.POST, controlURI);
        request.setConnection(new Connection() {
            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public InetAddress getRemoteAddress() {
                try {
                    return InetAddress.getByName("10.0.0.1");
                } catch (UnknownHostException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public InetAddress getLocalAddress() {
                try {
                    return InetAddress.getByName("10.0.0.2");
                } catch (UnknownHostException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        return request;
    }

    @Test
    public void incomingRemoteCallGetConcurrent() throws Exception {

        // Register local device and its service
        MockUpnpService upnpService = new MockUpnpService(false, false, true);
        LocalDevice<ActionSampleData.LocalTestServiceThrowsException> ld = ActionSampleData.createTestDevice(ActionSampleData.LocalTestServiceThrowsException.class);
        LocalService<ActionSampleData.LocalTestServiceThrowsException> service = ld.getServices().iterator().next();
        upnpService.getRegistry().addDevice(ld);

        // TODO: Use a latch instead of waiting
        int i = 0;
        while (i < 10) {
            new Thread(new ConcurrentGetTest(upnpService, service)).start();
            i++;
        }

        // Wait for the threads to finish
        Thread.sleep(2000);
    }

    static class ConcurrentGetTest implements Runnable {
        private final UpnpService upnpService;
        private final LocalService<?> service;

        ConcurrentGetTest(UpnpService upnpService, LocalService<?> service) {
            this.upnpService = upnpService;
            this.service = service;
        }

        @Override
		public void run() {
            Action<?> action = service.getAction("GetTarget");

            URI controlURI = upnpService.getConfiguration().getNamespace().getControlPath(service);
            StreamRequestMessage request = new StreamRequestMessage(UpnpRequest.Method.POST, controlURI);
            request.getHeaders().add(
                    UpnpHeader.Type.CONTENT_TYPE,
                    new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8)
            );

            SoapActionType actionType = new SoapActionType(service.getServiceType(), action.getName());
            request.getHeaders().add(UpnpHeader.Type.SOAPACTION, new SoapActionHeader(actionType));
            request.setBody(UpnpMessage.BodyType.STRING, GET_REQUEST);

            ReceivingAction prot = new ReceivingAction(upnpService, request);

            prot.run();

            StreamResponseMessage response = prot.getOutputMessage();

            assertNotNull(response);
            assertFalse(response.getOperation().isFailed());
            assertTrue(response.getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class).isUDACompliantXML());
            assertNotNull(response.getHeaders().getFirstHeader(UpnpHeader.Type.EXT, EXTHeader.class));
            assertEquals(
                response.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue(),
                new ServerHeader().getValue()
            );

            IncomingActionResponseMessage responseMessage = new IncomingActionResponseMessage(response);
            ActionInvocation<?> responseInvocation = new ActionInvocation<>(action);
            upnpService.getConfiguration().getSoapActionProcessor().readBody(responseMessage, responseInvocation);

            assertNotNull(responseInvocation.getOutput("RetTargetValue"));
        }
    }


    @Test
    public void incomingRemoteCallSet() throws Exception {

        // Register local device and its service
        MockUpnpService upnpService = new MockUpnpService();
        LocalDevice<?> ld = ActionSampleData.createTestDevice();
        LocalService<?> service = ld.getServices().iterator().next();
        upnpService.getRegistry().addDevice(ld);

        Action<?> action = service.getAction("SetTarget");

        URI controlURI = upnpService.getConfiguration().getNamespace().getControlPath(service);
        StreamRequestMessage request = new StreamRequestMessage(UpnpRequest.Method.POST, controlURI);
        addMandatoryRequestHeaders(service, action, request);
        request.setBody(UpnpMessage.BodyType.STRING, SET_REQUEST);

        ReceivingAction prot = new ReceivingAction(upnpService, request);

        prot.run();

        StreamResponseMessage response = prot.getOutputMessage();

        assertNotNull(response);
        assertFalse(response.getOperation().isFailed());
        assertTrue(response.getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class).isUDACompliantXML());
        assertNotNull(response.getHeaders().getFirstHeader(UpnpHeader.Type.EXT, EXTHeader.class));
        assertEquals(
            response.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue(),
            new ServerHeader().getValue()
        );

        IncomingActionResponseMessage responseMessage = new IncomingActionResponseMessage(response);
        ActionInvocation<?> responseInvocation = new ActionInvocation<>(action);
        upnpService.getConfiguration().getSoapActionProcessor().readBody(responseMessage, responseInvocation);

        assertEquals(responseInvocation.getOutput().size(), 0);

    }

    @Test
    public void incomingRemoteCallControlURINotFound() throws Exception {

        // Register local device and its service
        MockUpnpService upnpService = new MockUpnpService();
        LocalDevice<?> ld = ActionSampleData.createTestDevice();
        LocalService<?> service = ld.getServices().iterator().next();
        upnpService.getRegistry().addDevice(ld);

        Action<?> action = service.getAction("SetTarget");

        StreamRequestMessage request = new StreamRequestMessage(UpnpRequest.Method.POST, URI.create("/some/random/123/uri"));
        addMandatoryRequestHeaders(service, action, request);
        request.setBody(UpnpMessage.BodyType.STRING, SET_REQUEST);

        ReceivingAction prot = new ReceivingAction(upnpService, request);

        prot.run();

        UpnpMessage<?> response = prot.getOutputMessage();

        assertNull(response);
        // The StreamServer will send a 404 response
    }

    @Test
    public void incomingRemoteCallMethodException() throws Exception {

        // Register local device and its service
        MockUpnpService upnpService = new MockUpnpService();
        LocalDevice<?> ld = ActionSampleData.createTestDevice(ActionSampleData.LocalTestServiceThrowsException.class);
        LocalService<?> service = ld.getServices().iterator().next();
        upnpService.getRegistry().addDevice(ld);

        Action<?> action = service.getAction("SetTarget");

        URI controlURI = upnpService.getConfiguration().getNamespace().getControlPath(service);
        StreamRequestMessage request = new StreamRequestMessage(UpnpRequest.Method.POST, controlURI);
        addMandatoryRequestHeaders(service, action, request);

        request.setBody(UpnpMessage.BodyType.STRING, SET_REQUEST);

        ReceivingAction prot = new ReceivingAction(upnpService, request);

        prot.run();

        StreamResponseMessage response = prot.getOutputMessage();

        assertNotNull(response);
        assertTrue(response.getOperation().isFailed());
        assertTrue(response.getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class).isUDACompliantXML());
        assertNotNull(response.getHeaders().getFirstHeader(UpnpHeader.Type.EXT, EXTHeader.class));
        assertEquals(
            response.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue(),
            new ServerHeader().getValue()
        );

        IncomingActionResponseMessage responseMessage = new IncomingActionResponseMessage(response);
        ActionInvocation<?> responseInvocation = new ActionInvocation<>(action);
        upnpService.getConfiguration().getSoapActionProcessor().readBody(responseMessage, responseInvocation);

        ActionException ex = responseInvocation.getFailure();
        assertNotNull(ex);

        assertEquals(ex.getMessage(), ErrorCode.ACTION_FAILED.getDescription() + ". Something is wrong.");

    }

    @Test
    public void incomingRemoteCallNoContentType() throws Exception {

        // Register local device and its service
        MockUpnpService upnpService = new MockUpnpService();
        LocalDevice<?> ld = ActionSampleData.createTestDevice();
        LocalService<?> service = ld.getServices().iterator().next();
        upnpService.getRegistry().addDevice(ld);

        Action<?> action = service.getAction("GetTarget");

        URI controlURI = upnpService.getConfiguration().getNamespace().getControlPath(service);
        StreamRequestMessage request = new StreamRequestMessage(UpnpRequest.Method.POST, controlURI);
        SoapActionType actionType = new SoapActionType(service.getServiceType(), action.getName());
        request.getHeaders().add(UpnpHeader.Type.SOAPACTION, new SoapActionHeader(actionType));
        // NO CONTENT TYPE!
        request.setBody(UpnpMessage.BodyType.STRING, GET_REQUEST);

        ReceivingAction prot = new ReceivingAction(upnpService, request);

        prot.run();

        StreamResponseMessage response = prot.getOutputMessage();

        assertNotNull(response);
        assertFalse(response.getOperation().isFailed());
        assertTrue(response.getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class).isUDACompliantXML());
        assertNotNull(response.getHeaders().getFirstHeader(UpnpHeader.Type.EXT, EXTHeader.class));
        assertEquals(
            response.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue(),
            new ServerHeader().getValue()
        );

        IncomingActionResponseMessage responseMessage = new IncomingActionResponseMessage(response);
        ActionInvocation<?> responseInvocation = new ActionInvocation<>(action);
        upnpService.getConfiguration().getSoapActionProcessor().readBody(responseMessage, responseInvocation);

        assertNotNull(responseInvocation.getOutput("RetTargetValue"));

    }

    @Test
    public void incomingRemoteCallWrongContentType() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();

        StreamRequestMessage request = new StreamRequestMessage(UpnpRequest.Method.POST, URI.create("/some/random/123/uri"));
        request.getHeaders().add(
                UpnpHeader.Type.CONTENT_TYPE,
                new ContentTypeHeader(MimeType.valueOf("some/randomtype"))
        );
        request.setBody(UpnpMessage.BodyType.STRING, SET_REQUEST);

        ReceivingAction prot = new ReceivingAction(upnpService, request);

        prot.run();

        StreamResponseMessage response = prot.getOutputMessage();

        assertNotNull(response);
        assertEquals(response.getOperation().getStatusCode(), UpnpResponse.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    }

    @Test
    public void incomingRemoteCallQueryStateVariable() throws Exception {

        // Register local device and its service
        MockUpnpService upnpService = new MockUpnpService();
        LocalDevice<?> ld = ActionSampleData.createTestDevice();
        LocalService<?> service = ld.getServices().iterator().next();
        upnpService.getRegistry().addDevice(ld);

        Action<?> action = service.getAction(QueryStateVariableAction.ACTION_NAME);

        URI controlURI = upnpService.getConfiguration().getNamespace().getControlPath(service);
        StreamRequestMessage request = new StreamRequestMessage(UpnpRequest.Method.POST, controlURI);
        request.getHeaders().add(
                UpnpHeader.Type.CONTENT_TYPE,
                new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8)
        );
        request.getHeaders().add(
                UpnpHeader.Type.SOAPACTION,
                new SoapActionHeader(
                        new SoapActionType(
                                SoapActionType.MAGIC_CONTROL_NS, SoapActionType.MAGIC_CONTROL_TYPE, null, action.getName()
                        )
                )

        );
        request.setBody(UpnpMessage.BodyType.STRING, QUERY_STATE_VARIABLE_REQUEST);

        ReceivingAction prot = new ReceivingAction(upnpService, request);

        prot.run();

        StreamResponseMessage response = prot.getOutputMessage();

        assertNotNull(response);
        assertFalse(response.getOperation().isFailed());
        assertTrue(response.getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class).isUDACompliantXML());
        assertNotNull(response.getHeaders().getFirstHeader(UpnpHeader.Type.EXT, EXTHeader.class));
        assertEquals(
            response.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue(),
            new ServerHeader().getValue()
        );

        IncomingActionResponseMessage responseMessage = new IncomingActionResponseMessage(response);
        ActionInvocation<?> responseInvocation = new ActionInvocation<>(action);
        upnpService.getConfiguration().getSoapActionProcessor().readBody(responseMessage, responseInvocation);

        assertEquals(responseInvocation.getOutput().iterator().next().getArgument().getName(), "return");
        assertEquals(responseInvocation.getOutput().iterator().next().toString(), "0");
    }


    protected void addMandatoryRequestHeaders(Service<?, ?, ?> service, Action<?> action, StreamRequestMessage request) {
        request.getHeaders().add(
                UpnpHeader.Type.CONTENT_TYPE,
                new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8)
        );

        SoapActionType actionType = new SoapActionType(service.getServiceType(), action.getName());
        request.getHeaders().add(UpnpHeader.Type.SOAPACTION, new SoapActionHeader(actionType));
        // Not mandatory but only for the tests
        request.getHeaders().add(UpnpHeader.Type.USER_AGENT, new UserAgentHeader("foo/bar"));
    }

}