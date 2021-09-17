/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;


/**
 * @author mbechler
 *
 */
public class StaticSubjectGSSAPIFactory implements GSSAPISubjectFactory {

    /**
     * 
     */
    private static final long serialVersionUID = -8365909222404766310L;
    private Subject subject;


    /**
     * @param subject
     */
    public StaticSubjectGSSAPIFactory ( Subject subject ) {
        this.subject = subject;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.GSSAPISubjectFactory#getSubject()
     */
    @Override
    public Subject getSubject () {
        return this.subject;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.GSSAPISubjectFactory#getPrincipal()
     */
    @Override
    public KerberosPrincipal getPrincipal () {
        return null;
    }
}
