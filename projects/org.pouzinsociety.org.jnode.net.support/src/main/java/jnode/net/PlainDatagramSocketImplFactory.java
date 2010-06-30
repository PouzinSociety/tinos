/*
 * $
 */
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
 *                 - java.net -> jnode.net
 */
package jnode.net;

import jnode.net.DatagramSocketImplFactory;

/**
 *
 * @author Levente S\u00e1ntha
 */
public interface PlainDatagramSocketImplFactory extends DatagramSocketImplFactory {
    public PlainDatagramSocketImpl createPlainDatagramSocketImpl();
}
