/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent.api;


import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.realms.RealmConfig;
import eu.agno3.orchestrator.config.realms.RealmsConfig;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.KerberosException;


/**
 * @author mbechler
 *
 */
public interface RealmConfigUtil {

    /**
     * @param realmConfig
     * @param realmName
     * @return the realm config for the specified realm
     * @throws JobBuilderException
     */
    RealmConfig findRealm ( RealmsConfig realmConfig, String realmName ) throws JobBuilderException;


    /**
     * @param b
     * @param ctx
     * @param realmConfig
     * @param accessPrinc
     * @param realmName
     * @param keytab
     * @return the file that contains the keytab
     * @throws JobBuilderException
     * @throws ADException
     * @throws UnitInitializationFailedException
     * @throws KerberosException
     */
    Path ensureKeytab ( JobBuilder b, ConfigurationJobContext<@NonNull ?, ?> ctx, RealmsConfig realmConfig, String accessPrinc, String realmName,
            String keytab ) throws JobBuilderException, ADException, UnitInitializationFailedException, KerberosException;

}
