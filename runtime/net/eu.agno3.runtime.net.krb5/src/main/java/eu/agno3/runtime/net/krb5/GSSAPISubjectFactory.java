/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import java.io.Serializable;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;


/**
 * @author mbechler
 *
 */
public interface GSSAPISubjectFactory extends Serializable {

    /**
     * @return a subject for authentication
     * @throws KerberosException
     */
    Subject getSubject () throws KerberosException;


    /**
     * @return get the principal produced by this subject factory, null if unknown
     */
    KerberosPrincipal getPrincipal ();
}
