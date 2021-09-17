/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.log4j.Logger;

import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.krb5.AbstractCachingGSSAPIFactory;
import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.Krb5SubjectUtil;


/**
 * @author mbechler
 *
 */
public class ADGSSAPISubjectFactory extends AbstractCachingGSSAPIFactory implements GSSAPISubjectFactory {

    /**
     * 
     */
    private static final long serialVersionUID = -2375462029232807962L;

    /**
     * 
     */

    private static final Logger log = Logger.getLogger(ADGSSAPISubjectFactory.class);

    private ADRealm realm;
    private KerberosPrincipal principal;


    /**
     * @param realm
     */
    public ADGSSAPISubjectFactory ( ADRealm realm ) {
        this.realm = realm;
        this.principal = new KerberosPrincipal(String.format("%s@%s", realm.getMachineAccount(), realm.getKrbRealm())); //$NON-NLS-1$
    }


    @Override
    protected Subject getSubjectInternal () throws KerberosException {
        try {
            return Krb5SubjectUtil
                    .getInitiateAndAcceptSubject(this.principal, this.realm.getMachinePasswords(), this.realm.getSalts(this.principal), null, false);
        }
        catch ( ADException e ) {
            log.debug("Failed to get kerberos credentials", e); //$NON-NLS-1$
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
        return this.principal;
    }
}
