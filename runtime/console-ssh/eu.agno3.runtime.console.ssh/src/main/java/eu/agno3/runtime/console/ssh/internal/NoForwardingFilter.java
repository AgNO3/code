/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2013 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.forward.ForwardingFilter;


/**
 * @author mbechler
 *
 */
class NoForwardingFilter implements ForwardingFilter {

    @Override
    public boolean canConnect ( Type t, SshdSocketAddress sock, Session sess ) {
        return false;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.sshd.server.forward.ForwardingFilter#canForwardAgent(org.apache.sshd.common.session.Session,
     *      java.lang.String)
     */
    @Override
    public boolean canForwardAgent ( Session sess, String arg ) {
        return false;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.sshd.server.forward.ForwardingFilter#canForwardX11(org.apache.sshd.common.session.Session,
     *      java.lang.String)
     */
    @Override
    public boolean canForwardX11 ( Session sess, String arg ) {
        return false;
    }


    @Override
    public boolean canListen ( SshdSocketAddress sock, Session sess ) {
        return false;
    }

}
