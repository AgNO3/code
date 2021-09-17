/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2016 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KeyTab;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class KeyTabGSSAPIFactory extends AbstractCachingGSSAPIFactory implements GSSAPISubjectFactory {

    /**
     * 
     */
    private static final long serialVersionUID = 7751978361751222575L;

    private static final Logger log = Logger.getLogger(KeyTabGSSAPIFactory.class);
    private KeyTab keytab;


    /**
     * @param keytab
     */
    public KeyTabGSSAPIFactory ( KeyTab keytab ) {
        this.keytab = keytab;
    }


    @Override
    protected Subject getSubjectInternal () {
        try {
            return Krb5SubjectUtil.getInitiatorSubject(this.keytab, this.keytab.getPrincipal(), null, false);
        }
        catch ( KerberosException e ) {
            log.error("Failed to get kerberos credentials", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.GSSAPISubjectFactory#getPrincipal()
     */
    @Override
    public KerberosPrincipal getPrincipal () {
        return this.keytab.getPrincipal();
    }
}
