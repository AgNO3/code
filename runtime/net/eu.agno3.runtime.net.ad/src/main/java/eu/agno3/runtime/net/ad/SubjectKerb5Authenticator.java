/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2016 by mbechler
 */
package eu.agno3.runtime.net.ad;


import javax.security.auth.Subject;

import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;
import eu.agno3.runtime.net.krb5.KerberosException;

import jcifs.RuntimeCIFSException;
import jcifs.smb.CredentialsInternal;
import jcifs.smb.Kerb5Authenticator;


/**
 * @author mbechler
 *
 */
public class SubjectKerb5Authenticator extends Kerb5Authenticator implements CredentialsInternal {

    /**
     * 
     */
    private static final long serialVersionUID = 9014812040963194434L;

    private GSSAPISubjectFactory subjectFactory;


    /**
     * @param subjectFactory
     */
    public SubjectKerb5Authenticator ( GSSAPISubjectFactory subjectFactory ) {
        super(null);
        this.subjectFactory = subjectFactory;
    }


    @Override
    public Kerb5Authenticator clone () {
        SubjectKerb5Authenticator auth = new SubjectKerb5Authenticator(this.subjectFactory);
        Kerb5Authenticator.cloneInternal(auth, this);
        return auth;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.smb.Kerb5Authenticator#getSubject()
     */
    @Override
    public Subject getSubject () {
        try {
            return this.subjectFactory.getSubject();
        }
        catch ( KerberosException e ) {
            throw new RuntimeCIFSException("Authentication failed", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.smb.Kerb5Authenticator#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object other ) {
        if ( other instanceof SubjectKerb5Authenticator ) {
            return ( (SubjectKerb5Authenticator) other ).subjectFactory == this.subjectFactory;
        }
        return other == this;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.smb.Kerb5Authenticator#hashCode()
     */
    @Override
    public int hashCode () {
        return this.subjectFactory != null ? this.subjectFactory.hashCode() : 0;
    }
}
