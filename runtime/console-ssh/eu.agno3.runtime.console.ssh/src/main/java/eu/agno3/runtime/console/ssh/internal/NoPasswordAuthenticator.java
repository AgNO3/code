/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;


/**
 * @author mbechler
 *
 */
public class NoPasswordAuthenticator implements PasswordAuthenticator {

    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.sshd.server.auth.password.PasswordAuthenticator#authenticate(java.lang.String, java.lang.String,
     *      org.apache.sshd.server.session.ServerSession)
     */
    @Override
    public boolean authenticate ( String user, String pass, ServerSession sess ) {
        return false;
    }

}
