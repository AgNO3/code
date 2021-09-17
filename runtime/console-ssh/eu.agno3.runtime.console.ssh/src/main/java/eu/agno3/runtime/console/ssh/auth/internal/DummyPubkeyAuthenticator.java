/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2013 by mbechler
 */
package eu.agno3.runtime.console.ssh.auth.internal;


import java.security.PublicKey;

import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;


/**
 * @author mbechler
 * 
 */
public class DummyPubkeyAuthenticator implements PublickeyAuthenticator {

    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator#authenticate(java.lang.String,
     *      java.security.PublicKey, org.apache.sshd.server.session.ServerSession)
     */
    @Override
    public boolean authenticate ( String user, PublicKey key, ServerSession sess ) {
        return false;
    }

}
