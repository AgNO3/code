/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 31, 2017 by mbechler
 */
package eu.agno3.runtime.net.krb5.internal;


import java.nio.file.Path;
import java.util.Map;

import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KeyTab;

import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosRealmConfig;


/**
 * @author mbechler
 *
 */
public interface RealmManagerKerberosRealmConfig extends KerberosRealmConfig {

    /**
     * @param id
     * @param servicePrincipal
     * @return bound keytab instance
     * @throws KerberosException
     */
    KeyTab getBoundKeyTab ( String id, KerberosPrincipal servicePrincipal ) throws KerberosException;


    /**
     * @param ktId
     * @return path to key tab
     * @throws KerberosException
     */
    Path getKeyTabPath ( String ktId ) throws KerberosException;


    /**
     * @param id
     * @return unbound keytab instance
     * @throws KerberosException
     */
    KeyTab getUnboundKeyTab ( String id ) throws KerberosException;


    /**
     * @return path to keytabs
     * @throws KerberosException
     */
    Path getKeyTabsPath () throws KerberosException;


    /**
     * @return realm properties
     */
    Map<String, String> getProperties ();

}
