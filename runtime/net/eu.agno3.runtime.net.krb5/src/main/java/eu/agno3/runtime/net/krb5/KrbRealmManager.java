/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KeyTab;

import eu.agno3.runtime.net.krb5.internal.RealmManagerKerberosRealmConfig;


/**
 * @author mbechler
 *
 */
public interface KrbRealmManager {

    /**
     * 
     * @param realm
     * @param id
     * @param servicePrincipal
     * @return a keytab
     * @throws KerberosException
     */
    KeyTab getKeytab ( String realm, String id, KerberosPrincipal servicePrincipal ) throws KerberosException;


    /**
     * @param realm
     * @param id
     * @return an unbound keytab
     * @throws KerberosException
     */
    KeyTab getUnboundKeytab ( String realm, String id ) throws KerberosException;


    /**
     * @param realm
     * @return the realm instance
     * @throws KerberosException
     */
    KerberosRealm getRealmInstance ( String realm ) throws KerberosException;


    /**
     * 
     * @param krbRealm
     * @return the keytabs found for the realm
     * @throws KerberosException
     */
    Collection<String> listKeytabs ( String krbRealm ) throws KerberosException;


    /**
     * @return the base path for the realm data
     */
    Path getRealmBase ();


    /**
     * @param realm
     * @return the path where the realm config resides
     * @throws KerberosException
     */
    Path getRealmPath ( String realm ) throws KerberosException;


    /**
     * @param realmName
     * @return whether the realm exists in this manager
     */
    boolean exists ( String realmName );


    /**
     * @return the permitted etypes for this realm
     */
    Collection<Integer> getPermittedETypeAlgos ();


    /**
     * @param name
     * @return the realm type
     * @throws KerberosException
     */
    RealmType getRealmType ( String name ) throws KerberosException;


    /**
     * @param realm
     * @return realm configuration
     * @throws KerberosException
     */
    RealmManagerKerberosRealmConfig getRealmConfig ( String realm ) throws KerberosException;


    /**
     * 
     * @param realm
     * @return configuration properties
     * @throws KerberosException
     */
    Map<String, String> getProperties ( String realm ) throws KerberosException;


    /**
     * @param realm
     * @param ktId
     * @return keytab data for the given keytab
     * @throws KerberosException
     */
    eu.agno3.runtime.net.krb5.KeyTab getKeytabData ( String realm, String ktId ) throws KerberosException;

}
