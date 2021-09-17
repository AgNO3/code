/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.01.2016 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import javax.security.auth.Subject;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public abstract class AbstractCachingGSSAPIFactory implements GSSAPISubjectFactory {

    /**
     * 
     */
    private static final long serialVersionUID = -505143999304794652L;

    private static final Logger log = Logger.getLogger(AbstractCachingGSSAPIFactory.class);

    private Subject subject;


    /**
     * {@inheritDoc}
     * 
     * @throws KerberosException
     *
     * @see eu.agno3.runtime.net.krb5.GSSAPISubjectFactory#getSubject()
     */
    @Override
    public synchronized Subject getSubject () throws KerberosException {
        if ( this.needsRenewal() ) {
            log.debug("Renewing credentials"); //$NON-NLS-1$
            this.subject = this.getSubjectInternal();
        }
        return this.subject;
    }


    /**
     * @throws KerberosException
     * 
     */
    protected abstract Subject getSubjectInternal () throws KerberosException;


    /**
     * @return whether the subject need key renewal
     */
    protected boolean needsRenewal () {
        if ( this.subject == null ) {
            return true;
        }
        return !Krb5SubjectUtil.hasValidTGT(this.subject);
    }

}
