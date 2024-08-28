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

package example.localservice;

import com.distrimind.upnp_igd.binding.LocalServiceBinder;
import com.distrimind.upnp_igd.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp_igd.model.DefaultServiceManager;
import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.DeviceType;
import com.distrimind.upnp_igd.test.data.SampleData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Restricting numeric value ranges
 * <p>
 * For numeric state variables, you can limit the set of legal values within a range
 * when declaring the state variable:
 * </p>
 * <a class="citation" href="javacode://example.localservice.MyServiceWithAllowedValueRange" style="include: VAR"/>
 * <p>
 * Alternatively, if your allowed range has to be determined dynamically when
 * your service is being bound, you can implement a class with the
 * <code>AllowedValueRangeProvider</code> interface:
 * </p>
 * <a class="citation" href="javacode://example.localservice.MyServiceWithAllowedValueRangeProvider" style="include: PROVIDER"/>
 * <p>
 * Then, instead of specifying a static list of string values in your state variable declaration,
 * name the provider class:
 * </p>
 * <a class="citation" id="MyServiceWithAllowedValueRangeProvider-VAR" href="javacode://example.localservice.MyServiceWithAllowedValueRangeProvider" style="include: VAR"/>
 * <p>
 * Note that this provider will only be queried when your annotations are being processed,
 * once when your service is bound in UPnPIGD.
 * </p>
 */
public class AllowedValueRangeTest {

    public <T> LocalDevice<T> createTestDevice(Class<T> serviceClass) throws Exception {

        LocalServiceBinder binder = new AnnotationLocalServiceBinder();
        LocalService<T> svc = binder.read(serviceClass);
        svc.setManager(new DefaultServiceManager<>(svc, serviceClass));

        return new LocalDevice<>(
            SampleData.createLocalDeviceIdentity(),
            new DeviceType("mydomain", "CustomDevice", 1),
            new DeviceDetails("A Custom Device"),
            svc
        );
    }

    @DataProvider(name = "devices")
    public Object[][] getDevices() {
        try {
            return new LocalDevice[][]{
                {createTestDevice(MyServiceWithAllowedValueRange.class)},
                {createTestDevice(MyServiceWithAllowedValueRangeProvider.class)},
            };
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            // Damn testng swallows exceptions in provider/factory methods
            throw new RuntimeException(ex);
        }
    }

    @Test(dataProvider = "devices")
    public void validateBinding(LocalDevice<?> device) {
        LocalService<?> svc = device.getServices().iterator().next();
        assertEquals(svc.getStateVariables().size(), 1);
        assertEquals(svc.getStateVariables().iterator().next().getTypeDetails().getDatatype().getBuiltin(), Datatype.Builtin.I4);
        assertEquals(svc.getStateVariables().iterator().next().getTypeDetails().getAllowedValueRange().getMinimum(), 10);
        assertEquals(svc.getStateVariables().iterator().next().getTypeDetails().getAllowedValueRange().getMaximum(), 100);
        assertEquals(svc.getStateVariables().iterator().next().getTypeDetails().getAllowedValueRange().getStep(), 5);
    }

}
