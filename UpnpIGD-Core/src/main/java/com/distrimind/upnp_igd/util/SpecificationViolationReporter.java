/*
 * Copyright (C) 2011-2024 4th Line GmbH, Switzerland and others
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * SPDX-License-Identifier: CDDL-1.0
 */
package com.distrimind.upnp_igd.util;

import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.DeviceIdentity;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * This class reports violations again UPnP specification. It allows to
 * enable/disable these reports. E.g. for embedded devices it makes sense to
 * disable these checks for performance improvement and to avoid flooding of
 * logs if you have UPnP devices in your network which do not comply to UPnP
 * specifications.
 *
 * @author Jochen Hiller
 * @author Victor Toni - made logger non-static
 */
public class SpecificationViolationReporter {

	private static final SpecificationViolationReporter INSTANCE = new SpecificationViolationReporter();

	/**
	 * Defaults to enabled. Is volatile to reflect changes in arbitrary threads immediately.
	 */
	private volatile boolean enabled = true;

	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SpecificationViolationReporter.class.getName());

	private void _disableReporting() {
		enabled = false;
	}

	private void _enableReporting() {
		enabled = true;
	}

	private void _report(Object... arguments) {
		if (enabled) {
			if (logger.isLoggable(Level.WARNING))
				logger.warning("UPnP specification violation : "+ Arrays.toString(arguments));
		}
	}

	private <D extends Device<DeviceIdentity, D, ?>> void _report(D device, Object... arguments) {
		if (enabled) {
			if (device == null) {
				if (logger.isLoggable(Level.WARNING))
					logger.warning("UPnP specification violation : "+ Arrays.toString(arguments));
			} else {
				if (logger.isLoggable(Level.WARNING))
					logger.warning("UPnP specification violation : "+ device+", "+ Arrays.toString(arguments));
			}
		}
	}

	public static void disableReporting() {
		INSTANCE._disableReporting();
	}

	public static void enableReporting() {
		INSTANCE._enableReporting();
	}

	public static void report(Object... arguments) {
		INSTANCE._report(arguments);
	}

	public static <D extends Device<DeviceIdentity, D, ?>> void report(D device, Object... arguments) {
		INSTANCE._report(device, arguments);
	}
}
