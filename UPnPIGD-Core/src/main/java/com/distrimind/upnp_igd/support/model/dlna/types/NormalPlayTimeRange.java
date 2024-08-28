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
package com.distrimind.upnp_igd.support.model.dlna.types;

import com.distrimind.upnp_igd.model.types.InvalidValueException;

/**
 *
 * @author Mario Franco
 */
public class NormalPlayTimeRange {

    public static final String PREFIX = "npt=";
    
    private final NormalPlayTime timeStart;
    private final NormalPlayTime timeEnd;
    private NormalPlayTime timeDuration;

    public NormalPlayTimeRange(long timeStart, long timeEnd) {
        this.timeStart = new NormalPlayTime(timeStart);
        this.timeEnd = new NormalPlayTime(timeEnd);
    }

    public NormalPlayTimeRange(NormalPlayTime timeStart, NormalPlayTime timeEnd) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }

    public NormalPlayTimeRange(NormalPlayTime timeStart, NormalPlayTime timeEnd, NormalPlayTime timeDuration) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.timeDuration = timeDuration;
    }

    /**
     * @return the timeStart
     */
    public NormalPlayTime getTimeStart() {
        return timeStart;
    }

    /**
     * @return the timeEnd
     */
    public NormalPlayTime getTimeEnd() {
        return timeEnd;
    }

    /**
     * @return the timeDuration
     */
    public NormalPlayTime getTimeDuration() {
        return timeDuration;
    }

    /**
     * 
     * @return String format of Normal Play Time Range for response message header 
     */
    public String getString() {
        return getString(true);
    }

    /**
     * 
     * @return String format of Normal Play Time Range for response message header 
     */
    public String getString(boolean includeDuration) {
        String s = PREFIX;

        s += timeStart.getString() + "-";
        if (timeEnd != null) {
            s += timeEnd.getString();
        }
        if (includeDuration) {
            s += "/" + (timeDuration != null ? timeDuration.getString() : "*");
        }

        return s;
    }

    public static NormalPlayTimeRange valueOf(String s) throws InvalidValueException {
        return valueOf(s, false);
    }

    @SuppressWarnings("PMD.ImplicitSwitchFallThrough")
    public static NormalPlayTimeRange valueOf(String s, boolean mandatoryTimeEnd) throws InvalidValueException {
        if (s.startsWith(PREFIX)) {
            NormalPlayTime timeStart;
            NormalPlayTime timeEnd = null;
            NormalPlayTime timeDuration = null;
            String[] params = s.substring(PREFIX.length()).split("[-/]");
            switch (params.length) {
                case 3:
                    if (!params[2].isEmpty() && !"*".equals(params[2])) {
                        timeDuration = NormalPlayTime.valueOf(params[2]);
                    }
                case 2:
                    if (!params[1].isEmpty()) {
                        timeEnd = NormalPlayTime.valueOf(params[1]);
                    }
                case 1:
                    if (!params[0].isEmpty() && (!mandatoryTimeEnd || params.length > 1)) {
                        timeStart = NormalPlayTime.valueOf(params[0]);
                        return new NormalPlayTimeRange(timeStart, timeEnd, timeDuration);
                    }
                default:
                    break;
            }
        }
        throw new InvalidValueException("Can't parse NormalPlayTimeRange: " + s);
    }
}
