package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;

public class ICMPTimeExceededHeader extends ICMPHeader {

    /**
     * @param code
     */
    public ICMPTimeExceededHeader(int code) {
        super(ICMP_TIME_EXCEEDED, code);
    }

    /**
     * @param skbuf
     */
    public ICMPTimeExceededHeader(SocketBuffer skbuf) {
        super(skbuf);
        final int type = getType();
        if (type != ICMP_TIME_EXCEEDED) {
            throw new IllegalArgumentException("Invalid type " + type);
        }
    }

    /**
     * @see org.jnode.net.ipv4.icmp.ICMPHeader#doPrefixTo(org.jnode.net.SocketBuffer)
     */
    protected void doPrefixTo(SocketBuffer skbuf) {
        skbuf.set16(4, 0); // Unused, must be 0
    }

    /**
     * @see org.jnode.net.LayerHeader#getLength()
     */
    public int getLength() {
        return 8;
    }

}
