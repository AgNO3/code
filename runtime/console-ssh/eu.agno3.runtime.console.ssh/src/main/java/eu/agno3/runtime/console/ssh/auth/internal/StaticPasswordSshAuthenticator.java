/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2013 by mbechler
 */
package eu.agno3.runtime.console.ssh.auth.internal;


import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.console.ssh.SSHServiceConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    PasswordAuthenticator.class
}, configurationPid = SSHServiceConfiguration.PID_SIMPLE_AUTH, configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true )
public class StaticPasswordSshAuthenticator implements PasswordAuthenticator {

    private static final Logger log = Logger.getLogger(StaticPasswordSshAuthenticator.class);

    private String adminUser = SSHServiceConfiguration.ADMIN_USERNAME_DEFAULT;
    private String adminPassword = null;


    @Activate
    @Modified
    protected void activate ( ComponentContext context ) throws IOException {
        if ( context.getProperties().get(SSHServiceConfiguration.ADMIN_USER) == null ) {
            this.adminUser = SSHServiceConfiguration.ADMIN_USERNAME_DEFAULT;
        }
        else {
            this.adminUser = (String) context.getProperties().get(SSHServiceConfiguration.ADMIN_USER);
        }
        this.adminPassword = ConfigUtil.parseSecret(context.getProperties(), SSHServiceConfiguration.ADMIN_PASSWORD, null);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.sshd.server.auth.password.PasswordAuthenticator#authenticate(java.lang.String, java.lang.String,
     *      org.apache.sshd.server.session.ServerSession)
     */
    @Override
    public boolean authenticate ( String user, String pass, ServerSession sess ) {

        log.debug(String.format("Trying to authenticate user '%s' using password", user)); //$NON-NLS-1$

        if ( this.adminPassword != null && this.adminUser.equals(user) && this.adminPassword.equals(pass) ) {
            return true;
        }

        log.debug("Authentication failed"); //$NON-NLS-1$
        return false;
    }

}
