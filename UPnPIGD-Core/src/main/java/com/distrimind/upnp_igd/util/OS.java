 /*
 * Copyright (C) 2012 4th Line GmbH, Switzerland
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
 package com.distrimind.upnp_igd.util;

 import java.util.Locale;

 /**
 * @author Christian Bauer
 */
public class OS {

    public static final String OS_NAME = "os.name";
    public static final String LINUX = "linux";

    public static boolean checkForLinux() {
        return checkForPresence(OS_NAME, LINUX);
    }

    public static boolean checkForHp() {
        return checkForPresence(OS_NAME, "hp");
    }

    public static boolean checkForSolaris() {
        return checkForPresence(OS_NAME, "sun");
    }

    public static boolean checkForWindows() {
        return checkForPresence(OS_NAME, "win");
    }

    public static boolean checkForMac() {
        return checkForPresence(OS_NAME, "mac");
    }

    private static boolean checkForPresence(String key, String value) {
        try {
            String tmp = System.getProperty(key);
            return tmp != null && tmp.trim().toLowerCase(Locale.ROOT).startsWith(value);
        }
        catch (Throwable t) {
            return false;
        }
    }

}