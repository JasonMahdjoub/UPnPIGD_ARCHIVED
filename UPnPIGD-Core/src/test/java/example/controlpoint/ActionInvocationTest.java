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
package example.controlpoint;

import com.distrimind.upnp_igd.test.local.LocalActionInvocationEnumTest;
import com.distrimind.upnp_igd.test.model.IconTest;
import example.binarylight.BinaryLightSampleData;
import com.distrimind.upnp_igd.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp_igd.binding.annotations.UpnpAction;
import com.distrimind.upnp_igd.binding.annotations.UpnpInputArgument;
import com.distrimind.upnp_igd.binding.annotations.UpnpOutputArgument;
import com.distrimind.upnp_igd.binding.annotations.UpnpStateVariable;
import com.distrimind.upnp_igd.controlpoint.ActionCallback;
import com.distrimind.upnp_igd.mock.MockUpnpService;
import com.distrimind.upnp_igd.model.DefaultServiceManager;
import com.distrimind.upnp_igd.model.action.ActionArgumentValue;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.types.BooleanDatatype;
import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.UDAServiceId;
import com.distrimind.upnp_igd.model.types.UDAServiceType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Invoking an action
 * <p>
 * UPnP services expose state variables and actions. While the state variables represent the
 * current state of the service, actions are the operations used to query or maniuplate the
 * service's state. You have to obtain a <code>Service</code> instance from a
 * <code>Device</code> to access any <code>Action</code>. The target device can be local
 * to the same UPnP stack as your control point, or it can be remote of another device anywhere
 * on the network. We'll discuss later in this chapter how to access devices through the
 * local stack's <code>Registry</code>.
 * </p>
 * <p>
 * Once you have the device, access the <code>Service</code> through the metadata model, for example:
 * </p>
 * <a class="citation" href="javacode://this#invokeActions(LocalDevice)" id="ai_findservice" style="include: FINDSERVICE"/>
 * <p>
 * This method will search the device and all its embedded devices for a service with the given
 * identifier and returns either the found <code>Service</code> or <code>null</code>. The UPnPIGD
 * metamodel is thread-safe, so you can share an instance of <code>Service</code> or
 * <code>Action</code> and access it concurrently.
 * </p>
 * <p>
 * Invoking an action is the job of an instance of <code>ActionInvocation</code>, note that this
 * instance is <em>NOT</em> thread-safe and each thread that wishes to execute an action has to
 * obtain its own invocation from the <code>Action</code> metamodel:
 * </p>
 * <a class="citation" href="javacode://this#invokeActions(LocalDevice)" id="ai_getstatus" style="include: GETSTATUS; exclude: EXC1"/>
 * <p>
 * Execution is asynchronous, your <code>ActionCallback</code> has two methods which will be called
 * by the UPnP stack when the execution completes. If the action is successful, you can obtain any
 * output argument values from the invocation instance, which is conveniently passed into the
 * <code>success()</code> method. You can inspect the named output argument values and their datatypes to
 * continue processing the result.
 * </p>
 * <p>
 * Action execution doesn't have to be processed asynchronously, after all, the underlying HTTP/SOAP protocol
 * is a request waiting for a response. The callback programming model however fits nicely into a typical
 * UPnP client, which also has to process event notifications and device registrations asynchronously. If
 * you want to execute an <code>ActionInvocation</code> directly, within the current thread, use the empty
 * <code>ActionCallback.Default</code> implementation:
 * </p>
 * <a class="citation" href="javacode://this#invokeActions(LocalDevice)" id="ai_synchronous" style="include: SYNCHRONOUS"/>
 * <p>
 * When invocation fails you can access the failure details through
 * <code>invocation.getFailure()</code>, or use the shown convenience method to create a simple error
 * message. See the Javadoc of <code>ActionCallback</code> for more details.
 * </p>
 * <p>
 * When an action requires input argument values, you have to provide them. Like output arguments, any
 * input arguments of actions are also named, so you can set them by calling <code>setInput("MyArgumentName", value)</code>:
 * </p>
 * <a class="citation" href="javacode://this#invokeActions(LocalDevice)" id="ai_settarget" style="include: SETTARGET; exclude: EXC2"/>
 * <p>
 * This action has one input argument of UPnP type "boolean". You can set a Java <code>boolean</code>
 * primitive or <code>Boolean</code> instance and it will be automatically converted. If you set an
 * invalid value for a particular argument, such as an instance with the wrong type,
 * an <code>InvalidValueException</code> will be thrown immediately.
 * </p>
 * <div class="note">
 * <div class="title">Empty values and null in UPnPIGD</div>
 * There is no difference between empty string <code>""</code> and <code>null</code> in UPnPIGD,
 * because the UPnP specification does not address this issue. The SOAP  message of an action call
 * or an event message must contain an element {@code <SomeVar></SomeVar>} for all arguments, even if
 * it is an empty XML element. If you provide  an empty string or a null value when preparing a message,
 * it will always be a <code>null</code> on the receiving end because we can only transmit one
 * thing, an empty XML element. If you forget to set an input argument's value, it will be null/empty element.
 * </div>
 */
@SuppressWarnings("PMD.SystemPrintln")
public class ActionInvocationTest {

    public static final String MY_STRING = "MyString";
    public static final String NEW_TARGET_VALUE = "NewTargetValue";
    public static final String MY_STRING_1 = "MyString1";
    public static final String NEW_TARGET_VALUE_1 = "NewTargetValue1";
    public static final String RESULT_STATUS = "ResultStatus";

    protected <T> LocalService<T> bindService(Class<T> clazz) throws Exception {
        AnnotationLocalServiceBinder binder = new AnnotationLocalServiceBinder();
        // Let's also test the overloaded reader
        LocalService<T> svc = binder.read(
                clazz,
                new UDAServiceId(LocalActionInvocationEnumTest.SWITCH_POWER),
                new UDAServiceType(LocalActionInvocationEnumTest.SWITCH_POWER, 1),
                true,
                List.of(MyString.class)
        );
        svc.setManager(
                new DefaultServiceManager<>(svc, clazz)
        );
        return svc;
    }

    @DataProvider(name = "devices")
    public Object[][] getDevices() throws Exception {
        return new LocalDevice[][]{
                {BinaryLightSampleData.createDevice(bindService(TestServiceOne.class))},
                {BinaryLightSampleData.createDevice(bindService(TestServiceTwo.class))},
                {BinaryLightSampleData.createDevice(bindService(TestServiceThree.class))},
        };
    }

    @Test(dataProvider = "devices")
    public void invokeActions(LocalDevice<?> device) throws Exception {

        MockUpnpService upnpService = new MockUpnpService();

        LocalService<?> service = device.findService(new UDAServiceId(LocalActionInvocationEnumTest.SWITCH_POWER)); // DOC: FINDSERVICE
        Action<?> getStatusAction = service.getAction("GetStatus");               // DOC: FINDSERVICE

        final boolean[] tests = new boolean[3];

        ActionInvocation<?> getStatusInvocation = new ActionInvocation<>(getStatusAction);   // DOC: GETSTATUS

        ActionCallback getStatusCallback = new ActionCallback(getStatusInvocation) {

            @Override
            public void success(ActionInvocation<?> invocation) {
                ActionArgumentValue<?> status  = invocation.getOutput(RESULT_STATUS);

                assert status != null;

                assertEquals(status.getArgument().getName(), RESULT_STATUS);

                assertEquals(status.getDatatype().getClass(), BooleanDatatype.class);
                assertEquals(status.getDatatype().getBuiltin(), Datatype.Builtin.BOOLEAN);

                assertEquals((Boolean) status.getValue(), Boolean.FALSE);
                assertEquals(status.toString(), "0"); // '0' is 'false' in UPnP
                tests[0] = true; // DOC: EXC1
            }

            @Override
            public void failure(ActionInvocation<?> invocation,
                                UpnpResponse operation,
                                String defaultMsg) {
                System.err.println(defaultMsg);
            }
        };

        upnpService.getControlPoint().execute(getStatusCallback);                       // DOC: GETSTATUS


        Action<?> action = service.getAction("SetTarget");                                 // DOC: SETTARGET

        ActionCallback setTargetCallback = getActionCallback(action, tests);

        upnpService.getControlPoint().execute(setTargetCallback);                       // DOC: SETTARGET

        getStatusInvocation = new ActionInvocation<>(getStatusAction);
        new ActionCallback.Default(getStatusInvocation, upnpService.getControlPoint()).run(); // DOC: SYNCHRONOUS
        ActionArgumentValue<?> status  = getStatusInvocation.getOutput(RESULT_STATUS);
        if (Boolean.TRUE.equals(status.getValue())) {
            tests[2] = true;
        }

        for (boolean test : tests) {
			assertTrue(test);
        }


		ActionInvocation<? extends LocalService<?>> getTargetInvocation = new ActionInvocation<>(service.getAction("GetTarget"));
        service.getExecutor(getTargetInvocation.getAction()).executeWithUntypedGeneric(getTargetInvocation);
		assertNull(getTargetInvocation.getFailure());
        assertEquals(getTargetInvocation.getOutput().size(), 1);
        assertEquals(getTargetInvocation.getOutput().iterator().next().toString(), "1");

        ActionInvocation<? extends LocalService<?>> setMyStringInvocation = new ActionInvocation<>(service.getAction("SetMyString"));
        setMyStringInvocation.setInput(MY_STRING, IconTest.FOO);
        service.getExecutor(setMyStringInvocation.getAction()).executeWithUntypedGeneric(setMyStringInvocation);
		assertNull(setMyStringInvocation.getFailure());
        assertEquals(setMyStringInvocation.getOutput().size(), 0);

        ActionInvocation<? extends LocalService<?>> getMyStringInvocation = new ActionInvocation<>(service.getAction("GetMyString"));
        service.getExecutor(getMyStringInvocation.getAction()).executeWithUntypedGeneric(getMyStringInvocation);
		assertNull(getTargetInvocation.getFailure());
        assertEquals(getMyStringInvocation.getOutput().size(), 1);
        assertEquals(getMyStringInvocation.getOutput().iterator().next().toString(), IconTest.FOO);

    }

    private static ActionCallback getActionCallback(Action<?> action, boolean[] tests) {
        ActionInvocation<?> setTargetInvocation = new ActionInvocation<>(action);

        setTargetInvocation.setInput(NEW_TARGET_VALUE, true); // Can throw InvalidValueException

        // Alternative:
        //
        // setTargetInvocation.setInput(
        //         new ActionArgumentValue(
        //                 action.getInputArgument("NewTargetValue"),
        //                 true
        //         )
        // );

		return new ActionCallback(setTargetInvocation) {

            @Override
            public void success(ActionInvocation<?> invocation) {
                Collection<? extends ActionArgumentValue<?>> output = invocation.getOutput();
                assertEquals(output.size(), 0);
                tests[1] = true; // DOC: EXC2
            }

            @Override
            public void failure(ActionInvocation<?> invocation,
                                UpnpResponse operation,
                                String defaultMsg) {
                System.err.println(defaultMsg);
            }
        };
    }

    @Test(dataProvider = "devices")
    public void invokeActionsWithAlias(LocalDevice<?> device) throws Exception {

        MockUpnpService upnpService = new MockUpnpService();

        LocalService<?> service = device.findService(new UDAServiceId(LocalActionInvocationEnumTest.SWITCH_POWER));


        final boolean[] tests = new boolean[1];

        Action<? extends LocalService<?>> action = service.getAction("SetTarget");
        ActionCallback setTargetCallback = getCallback(action, tests);
        upnpService.getControlPoint().execute(setTargetCallback);

        for (boolean test : tests) {
			assertTrue(test);
        }

		ActionInvocation<? extends LocalService<?>> getTargetInvocation = new ActionInvocation<>(service.getAction("GetTarget"));
        service.getExecutor(getTargetInvocation.getAction()).executeWithUntypedGeneric(getTargetInvocation);
		assertNull(getTargetInvocation.getFailure());
        assertEquals(getTargetInvocation.getOutput().size(), 1);
        assertEquals(getTargetInvocation.getOutput().iterator().next().toString(), "1");

        ActionInvocation<? extends LocalService<?>> setMyStringInvocation = new ActionInvocation<>(service.getAction("SetMyString"));
        setMyStringInvocation.setInput(MY_STRING_1, IconTest.FOO);
        service.getExecutor(setMyStringInvocation.getAction()).executeWithUntypedGeneric(setMyStringInvocation);
		assertNull(setMyStringInvocation.getFailure());
        assertEquals(setMyStringInvocation.getOutput().size(), 0);

        ActionInvocation<? extends LocalService<?>> getMyStringInvocation = new ActionInvocation<>(service.getAction("GetMyString"));
        service.getExecutor(getMyStringInvocation.getAction()).executeWithUntypedGeneric(getMyStringInvocation);
		assertNull(getTargetInvocation.getFailure());
        assertEquals(getMyStringInvocation.getOutput().size(), 1);
        assertEquals(getMyStringInvocation.getOutput().iterator().next().toString(), IconTest.FOO);

    }

    private static ActionCallback getCallback(Action<? extends LocalService<?>> action, boolean[] tests) {
        ActionInvocation<? extends LocalService<?>> setTargetInvocation = new ActionInvocation<>(action);
        setTargetInvocation.setInput(NEW_TARGET_VALUE_1, true);
		return new ActionCallback(setTargetInvocation) {

            @Override
            public void success(ActionInvocation<?> invocation) {
                Collection<? extends ActionArgumentValue<?>> output = invocation.getOutput();
                assertEquals(output.size(), 0);
                tests[0] = true;
            }

            @Override
            public void failure(ActionInvocation<?> invocation,
                                UpnpResponse operation,
                                String defaultMsg) {
                System.err.println(defaultMsg);
            }
        };
    }

    /* ####################################################################################################### */

    public static class TestServiceOne {

        @UpnpStateVariable(sendEvents = false)
        private boolean target = false;

        @UpnpStateVariable
        private boolean status = false;

        @UpnpStateVariable(sendEvents = false)
        private MyString myString;

        @UpnpAction
        public void setTarget(@UpnpInputArgument(name = NEW_TARGET_VALUE, aliases ={NEW_TARGET_VALUE_1}) boolean newTargetValue) {
            target = newTargetValue;
            status = newTargetValue;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "RetTargetValue"))
        public boolean getTarget() {
            return target;
        }

        @UpnpAction(name = "GetStatus", out = @UpnpOutputArgument(name = RESULT_STATUS, getterName = "getStatus"))
        public void dummyStatus() {
            // NOOP
        }

        public boolean getStatus() {
            return status;
        }

        @UpnpAction
        public void setMyString(@UpnpInputArgument(name = MY_STRING, aliases ={MY_STRING_1}) MyString myString) {
            this.myString = myString;
        }

        @UpnpAction(name = "GetMyString", out = @UpnpOutputArgument(name = MY_STRING, getterName = "getMyString"))
        public void getMyStringDummy() {
        }

        public MyString getMyString() {
            return myString;
        }
    }

    public static class TestServiceTwo {

        @UpnpStateVariable(sendEvents = false)
        private boolean target = false;

        @UpnpStateVariable
        private boolean status = false;

        @UpnpStateVariable(sendEvents = false)
        private MyString myString;

        @UpnpAction
        public void setTarget(@UpnpInputArgument(name = NEW_TARGET_VALUE, aliases ={NEW_TARGET_VALUE_1}) boolean newTargetValue) {
            target = newTargetValue;
            status = newTargetValue;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "RetTargetValue"))
        public boolean getTarget() {
            return target;
        }

        @UpnpAction(name = "GetStatus", out = @UpnpOutputArgument(name = RESULT_STATUS, getterName = "getStatus"))
        public StatusHolder dummyStatus() {
            return new StatusHolder(status);
        }

        @UpnpAction
        public void setMyString(@UpnpInputArgument(name = MY_STRING, aliases ={MY_STRING_1}) MyString myString) {
            this.myString = myString;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = MY_STRING, getterName = "getMyString"))
        public MyStringHolder getMyString() {
            return new MyStringHolder(myString);
        }

        public static class StatusHolder {
            final boolean st;

            public StatusHolder(boolean st) {
                this.st = st;
            }

            public boolean getStatus() {
                return st;
            }
        }

        public static class MyStringHolder {
            final MyString myString;

            public MyStringHolder(MyString myString) {
                this.myString = myString;
            }

            public MyString getMyString() {
                return myString;
            }
        }

    }

    public static class TestServiceThree {

        @UpnpStateVariable(sendEvents = false)
        private boolean target = false;

        @UpnpStateVariable
        private boolean status = false;

        @UpnpStateVariable(sendEvents = false)
        private MyString myString;

        @UpnpAction
        public void setTarget(@UpnpInputArgument(name = NEW_TARGET_VALUE, aliases ={NEW_TARGET_VALUE_1}) boolean newTargetValue) {
            target = newTargetValue;
            status = newTargetValue;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "RetTargetValue"))
        public boolean getTarget() {
            return target;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = RESULT_STATUS))
        public boolean getStatus() {
            return status;
        }

        @UpnpAction
        public void setMyString(@UpnpInputArgument(name = MY_STRING, aliases ={MY_STRING_1}) MyString myString) {
            this.myString = myString;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = MY_STRING))
        public MyString getMyString() {
            return myString;
        }
    }

    public static class MyString {
        private final String s;

        public MyString(String s) {
            this.s = s;
        }

        public String getS() {
            return s;
        }

        @Override
        public String toString() {
            return s;
        }
    }

}
