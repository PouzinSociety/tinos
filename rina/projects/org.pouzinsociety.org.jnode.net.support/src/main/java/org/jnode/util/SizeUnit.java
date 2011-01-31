/*
 * 2008 - 2010 (c) Waterford Institute of Technology
 *		   TSSG, EU ICT 4WARD
 *
 * 2010 (c) Pouzin Society
 *   - Forked from EU ICT 4WARD Open Source Distribution.
 *   - Organisation Strings updated to reflect fork.
 *
 *
 * Author        : pphelan(at)tssg.org
 *
 * Modifications : Changes to port JNode code base to OSGi platform.
 */
package org.jnode.util;


/**
 * @deprecated use DecimalPrefix or BinaryPrefix instead.
 */
public enum SizeUnit {
    B(1l, "B"),
    K(1024l, "K"),
    M(1024l * 1024l, "M"),
    G(1024l * 1024l * 1024l, "G"),
    T(1024l * 1024l * 1024l * 1024l, "T"),
    P(1024l * 1024l * 1024l * 1024l * 1024l, "P"),
    E(1024l * 1024l * 1024l * 1024l * 1024l * 1024l, "E");
    //these units have too big multipliers to fit in a long
    // (aka they are greater than 2^64) :
    //Z(1024l*1024l*1024l*1024l*1024l*1024l*1024l, "Z"),
    //Y(1024l*1024l*1024l*1024l*1024l*1024l*1024l*1024l, "Y");

    public static final SizeUnit MIN = B;
    public static final SizeUnit MAX = E;

    private final long multiplier;
    private final String unit;

    private SizeUnit(long multiplier, String unit) {
        this.multiplier = multiplier;
        this.unit = unit;
    }

    public long getMultiplier() {
        return multiplier;
    }

    public String getUnit() {
        return unit;
    }

    public String toString() {
        return multiplier + ", " + unit;
    }
}
