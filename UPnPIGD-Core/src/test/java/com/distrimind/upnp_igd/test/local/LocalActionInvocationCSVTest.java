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

package com.distrimind.upnp_igd.test.local;

import com.distrimind.upnp_igd.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp_igd.binding.annotations.UpnpAction;
import com.distrimind.upnp_igd.binding.annotations.UpnpInputArgument;
import com.distrimind.upnp_igd.binding.annotations.UpnpOutputArgument;
import com.distrimind.upnp_igd.binding.annotations.UpnpService;
import com.distrimind.upnp_igd.binding.annotations.UpnpServiceId;
import com.distrimind.upnp_igd.binding.annotations.UpnpServiceType;
import com.distrimind.upnp_igd.binding.annotations.UpnpStateVariable;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.types.UDADeviceType;
import com.distrimind.upnp_igd.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp_igd.model.types.csv.*;
import com.distrimind.upnp_igd.test.data.SampleData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@SuppressWarnings("PMD.LooseCoupling")
public class LocalActionInvocationCSVTest {

    public LocalDevice<?> createTestDevice(LocalService<?> service) throws Exception {
        return new LocalDevice<>(
                SampleData.createLocalDeviceIdentity(),
                new UDADeviceType("TestDevice", 1),
                new DeviceDetails("Test Device"),
                service
        );
    }

    @DataProvider(name = "devices")
    public Object[][] getDevices() throws Exception {
        return new LocalDevice[][]{
                {createTestDevice(
                        SampleData.readService(
                                new AnnotationLocalServiceBinder(), TestServiceOne.class
                        )
                )},
        };
    }

    @Test(dataProvider = "devices")
    public void invokeActions(LocalDevice<?> device) throws Exception {

        LocalService<?> svc = SampleData.getFirstService(device);

        List<String> testStrings = new CSVString();
        testStrings.add("f\\oo");
        testStrings.add("bar");
        testStrings.add("b,az");
        String result = executeActions(svc, "SetStringVar", "GetStringVar", testStrings);
        List<String> csvString = new CSVString(result);
        assert csvString.size() == 3;
        assertEquals(csvString.get(0), "f\\oo");
        assertEquals(csvString.get(1), "bar");
        assertEquals(csvString.get(2), "b,az");

        List<Integer> testIntegers = new CSVInteger();
        testIntegers.add(123);
        testIntegers.add(-456);
        testIntegers.add(789);
        result = executeActions(svc, "SetIntVar", "GetIntVar", testIntegers);
        List<Integer> csvInteger = new CSVInteger(result);
        assert csvInteger.size() == 3;
        assertEquals(csvInteger.get(0), Integer.valueOf(123));
        assertEquals(csvInteger.get(1), Integer.valueOf(-456));
        assertEquals(csvInteger.get(2), Integer.valueOf(789));

        List<Boolean> testBooleans = new CSVBoolean();
        testBooleans.add(true);
        testBooleans.add(true);
        testBooleans.add(false);
        result = executeActions(svc, "SetBooleanVar", "GetBooleanVar", testBooleans);
        List<Boolean> csvBoolean = new CSVBoolean(result);
        assert csvBoolean.size() == 3;
        assertEquals(csvBoolean.get(0), Boolean.TRUE);
        assertEquals(csvBoolean.get(1), Boolean.TRUE);
        assertEquals(csvBoolean.get(2), Boolean.FALSE);

        List<UnsignedIntegerFourBytes> testUifour = new CSVUnsignedIntegerFourBytes();
        testUifour.add(new UnsignedIntegerFourBytes(123));
        testUifour.add(new UnsignedIntegerFourBytes(456));
        testUifour.add(new UnsignedIntegerFourBytes(789));
        result = executeActions(svc, "SetUifourVar", "GetUifourVar", testUifour);
        List<UnsignedIntegerFourBytes> csvUifour = new CSVUnsignedIntegerFourBytes(result);
        assert csvUifour.size() == 3;
        assertEquals(csvUifour.get(0), new UnsignedIntegerFourBytes(123));
        assertEquals(csvUifour.get(1), new UnsignedIntegerFourBytes(456));
        assertEquals(csvUifour.get(2), new UnsignedIntegerFourBytes(789));
    }

    protected <T> String executeActions(LocalService<T> svc, String setAction, String getAction, List<?> input) throws Exception {
        ActionInvocation<LocalService<T>> setActionInvocation = new ActionInvocation<>(svc.getAction(setAction));
        setActionInvocation.setInput(svc.getAction(setAction).getFirstInputArgument().getName(), input.toString());
        svc.getExecutor(setActionInvocation.getAction()).execute(setActionInvocation);
		assertNull(setActionInvocation.getFailure());
        assertEquals(setActionInvocation.getOutput().size(), 0);

        ActionInvocation<LocalService<T>> getActionInvocation = new ActionInvocation<>(svc.getAction(getAction));
        svc.getExecutor(getActionInvocation.getAction()).execute(getActionInvocation);
		assertNull(getActionInvocation.getFailure());
        assertEquals(getActionInvocation.getOutput().size(), 1);
        return getActionInvocation.getOutput(svc.getAction(getAction).getFirstOutputArgument()).toString();
    }


    /* ####################################################################################################### */


    @UpnpService(
            serviceId = @UpnpServiceId("TestService"),
            serviceType = @UpnpServiceType(value = "TestService", version = 1)
    )
    public static class TestServiceOne {

        @UpnpStateVariable(sendEvents = false)
        private CSV<String> stringVar;

        @UpnpStateVariable(sendEvents = false)
        private CSV<Integer> intVar;

        @UpnpStateVariable(sendEvents = false)
        private CSV<Boolean> booleanVar;

        @UpnpStateVariable(sendEvents = false)
        private CSV<UnsignedIntegerFourBytes> uifourVar;

        @UpnpAction
        public void setStringVar(@UpnpInputArgument(name = "StringVar") CSVString stringVar) {
            this.stringVar = stringVar;
            assertEquals(stringVar.size(), 3);
            assertEquals(stringVar.get(0), "f\\oo");
            assertEquals(stringVar.get(1), "bar");
            assertEquals(stringVar.get(2), "b,az");
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "StringVar"))
        public CSV<String> getStringVar() {
            return stringVar;
        }

        @UpnpAction
        public void setIntVar(@UpnpInputArgument(name = "IntVar") CSVInteger intVar) {
            this.intVar = intVar;
            assertEquals(intVar.size(), 3);
            assertEquals(intVar.get(0), Integer.valueOf(123));
            assertEquals(intVar.get(1), Integer.valueOf(-456));
            assertEquals(intVar.get(2), Integer.valueOf(789));
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "IntVar"))
        public CSV<Integer> getIntVar() {
            return intVar;
        }

        @UpnpAction
        public void setBooleanVar(@UpnpInputArgument(name = "BooleanVar") CSVBoolean booleanVar) {
            this.booleanVar = booleanVar;
            assertEquals(booleanVar.size(), 3);
            assertEquals(booleanVar.get(0), Boolean.TRUE);
            assertEquals(booleanVar.get(1), Boolean.TRUE);
            assertEquals(booleanVar.get(2), Boolean.FALSE);
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "BooleanVar"))
        public CSV<Boolean> getBooleanVar() {
            return booleanVar;
        }

        @UpnpAction
        public void setUifourVar(@UpnpInputArgument(name = "UifourVar") CSVUnsignedIntegerFourBytes uifourVar) {
            this.uifourVar = uifourVar;
            assertEquals(uifourVar.size(), 3);
            assertEquals(uifourVar.get(0), new UnsignedIntegerFourBytes(123));
            assertEquals(uifourVar.get(1), new UnsignedIntegerFourBytes(456));
            assertEquals(uifourVar.get(2), new UnsignedIntegerFourBytes(789));
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "UifourVar"))
        public CSV<UnsignedIntegerFourBytes> getUifourVar() {
            return uifourVar;
        }

    }

}