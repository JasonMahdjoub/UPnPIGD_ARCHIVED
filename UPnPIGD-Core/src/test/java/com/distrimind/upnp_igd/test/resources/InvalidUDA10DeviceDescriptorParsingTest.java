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

package com.distrimind.upnp_igd.test.resources;

import com.distrimind.upnp_igd.UpnpService;
import com.distrimind.upnp_igd.binding.xml.DescriptorBindingException;
import com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import com.distrimind.upnp_igd.binding.xml.UDA10DeviceDescriptorBinderSAXImpl;
import com.distrimind.upnp_igd.mock.MockUpnpService;
import com.distrimind.upnp_igd.mock.MockUpnpServiceConfiguration;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.test.data.SampleData;
import com.distrimind.upnp_igd.util.io.IO;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Objects;

/**
 * @author Christian Bauer
 */
public class InvalidUDA10DeviceDescriptorParsingTest {

    public static final String STRICT = "strict";

    @DataProvider(name = STRICT)
    public String[][] getStrict() throws Exception {
        return new String[][]{
            {"/invalidxml/device/atb_miviewtv.xml"},
            {"/invalidxml/device/doubletwist.xml"},
            {"/invalidxml/device/eyetv_netstream_sat.xml"},
            {"/invalidxml/device/makemkv.xml"},
            {"/invalidxml/device/tpg.xml"},
            {"/invalidxml/device/ceton_infinitv.xml"},
            {"/invalidxml/device/zyxel_miviewtv.xml"},
            {"/invalidxml/device/perfectwave.xml"},
            {"/invalidxml/device/escient.xml"},
            {"/invalidxml/device/eyecon.xml"},
            {"/invalidxml/device/kodak.xml"},
            {"/invalidxml/device/plutinosoft.xml"},
            {"/invalidxml/device/samsung.xml"},
            {"/invalidxml/device/philips_hue.xml"},
        };
    }

    @DataProvider(name = "recoverable")
    public String[][] getRecoverable() throws Exception {
        return new String[][]{
            {"/invalidxml/device/missing_namespaces.xml"},
            {"/invalidxml/device/ushare.xml"},
            {"/invalidxml/device/lg.xml"},
            {"/invalidxml/device/readydlna.xml"},
        };
    }

    @DataProvider(name = "unrecoverable")
    public String[][] getUnrecoverable() throws Exception {
        return new String[][]{
            {"/invalidxml/device/unrecoverable/pms.xml"},
            {"/invalidxml/device/unrecoverable/awox.xml"},
            {"/invalidxml/device/philips.xml"},
            {"/invalidxml/device/simplecenter.xml"},
            {"/invalidxml/device/ums.xml"},
        };
    }

    /* ############################## TEST FAILURE ############################ */

    @Test(dataProvider = "recoverable", expectedExceptions = DescriptorBindingException.class)
    public void readFailure(String recoverable) throws Exception {
        readDevice(recoverable, new MockUpnpService());
    }

    @Test(dataProvider = "unrecoverable", expectedExceptions = Exception.class)
    public void readRecoveringFailure(String unrecoverable) throws Exception {
        readDevice(
            unrecoverable,
            new MockUpnpService(new MockUpnpServiceConfiguration() {
                @Override
                public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
                    return new RecoveringUDA10DeviceDescriptorBinderImpl(getNetworkAddressFactory());
                }
            })
        );
    }

    /* ############################## TEST SUCCESS ############################ */

    @Test(dataProvider = STRICT)
    public void readDefault(String strict) throws Exception {
        readDevice(strict, new MockUpnpService());
    }

    @Test(dataProvider = STRICT)
    public void readSAX(String strict) throws Exception {
        readDevice(
            strict,
            new MockUpnpService(new MockUpnpServiceConfiguration() {
                @Override
                public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
                    return new UDA10DeviceDescriptorBinderSAXImpl(getNetworkAddressFactory());
                }
            })
        );
    }

    @Test(dataProvider = STRICT)
    public void readRecoveringStrict(String strict) throws Exception {
        readDevice(
            strict,
            new MockUpnpService(new MockUpnpServiceConfiguration() {
                @Override
                public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
                    return new RecoveringUDA10DeviceDescriptorBinderImpl(getNetworkAddressFactory());
                }
            })
        );
    }

    @Test(dataProvider = "recoverable")
    public void readRecovering(String recoverable) throws Exception {
        readDevice(
            recoverable,
            new MockUpnpService(new MockUpnpServiceConfiguration() {
                @Override
                public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
                    return new RecoveringUDA10DeviceDescriptorBinderImpl(getNetworkAddressFactory());
                }
            })
        );
    }

	protected void readDevice(String invalidXMLFile, UpnpService upnpService) throws Exception {
		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		upnpService.getConfiguration().getDeviceDescriptorBinderUDA10()
            .describe(device, IO.readLines(Objects.requireNonNull(getClass().getResourceAsStream(invalidXMLFile))));
	}

}

