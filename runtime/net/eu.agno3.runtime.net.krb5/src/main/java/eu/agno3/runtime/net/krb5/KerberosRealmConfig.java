package eu.agno3.runtime.net.krb5;


import java.util.List;

import org.joda.time.Duration;


/**
 * @author mbechler
 *
 */
public interface KerberosRealmConfig {

    /**
     * @return the adminServer
     */
    String getAdminServer ();


    /**
     * @return the kpasswdServer
     */
    String getKpasswdServer ();


    /**
     * @return the kdcs
     */
    List<String> getKdcs ();


    /**
     * @return the realm type
     */
    RealmType getRealmType ();


    /**
     * @return the override local hostname
     */
    String getOverrideLocalHostname ();


    /**
     * @return the maxiumumTicketLifetime
     */
    Duration getMaxiumumTicketLifetime ();


    /**
     * @return the rekeyServicesInterval
     */
    Duration getRekeyServicesInterval ();


    /**
     * @return the realm
     */
    String getRealm ();


    /**
     * @return number of auth factors this realm does supply
     */
    int getAuthFactors ();

}