package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Address;

public class ICMPRedirectHeader extends ICMPHeader {
		private IPv4Address gateway;

	    /**
	     * @param code
	     */
	    public ICMPRedirectHeader(int code, IPv4Address gateway) {
	        super(ICMP_REDIRECT, code);
	        this.gateway = gateway;
	    }

	    /**
	     * @param skbuf
	     */
	    public ICMPRedirectHeader(SocketBuffer skbuf) {
	        super(skbuf);
	        final int type = getType();
	        gateway = new IPv4Address(skbuf, 4);
	        if (type != ICMP_REDIRECT) {
	            throw new IllegalArgumentException("Invalid type " + type);
	        }
	    }

	    /**
	     * @see org.jnode.net.ipv4.icmp.ICMPHeader#doPrefixTo(org.jnode.net.SocketBuffer)
	     */
	    protected void doPrefixTo(SocketBuffer skbuf) {
	    	gateway.writeTo(skbuf, 4);
	    }

	    /**
	     * @see org.jnode.net.LayerHeader#getLength()
	     */
	    public int getLength() {
	        return 8;
	    }


}
