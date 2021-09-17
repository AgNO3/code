/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.02.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.config;


import javax.security.auth.kerberos.KerberosPrincipal;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.realms.KeyTabManager;
import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 *
 */
public interface KerberosConfigUtil {

    /**
     * @param realm
     * @return a realm manager for the specified realm
     * @throws KerberosException
     * @throws JobBuilderException
     */
    RealmManager getRealmManager ( String realm ) throws KerberosException, JobBuilderException;


    /**
     * @param realm
     * @param type
     * @return a realm manager for the specified realm
     * @throws JobBuilderException
     * @throws KerberosException
     */
    RealmManager getRealmManager ( String realm, RealmType type ) throws JobBuilderException, KerberosException;


    /**
     * @param service
     * @param ktm
     * @return the found principal
     * @throws JobBuilderException
     */
    KerberosPrincipal checkKeytab ( String service, KeyTabManager ktm ) throws JobBuilderException;


    /**
     * @param b
     * @param ctx
     * @param rm
     * @param keytab
     * @param princName
     * @return the used principal name, null if ad host principal is used
     * @throws ServiceManagementException
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     * @throws ADException
     * @throws KerberosException
     * @throws UnixAccountException
     */
    String ensureInitiatorCredentials ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, RealmManager rm, String keytab,
            String princName ) throws ServiceManagementException, JobBuilderException, UnitInitializationFailedException, ADException,
                    KerberosException, UnixAccountException;


    /**
     * @param ktm
     * @return the principal name (if only one is present)
     * @throws JobBuilderException
     */
    KerberosPrincipal findPrincipalInKeytab ( KeyTabManager ktm ) throws JobBuilderException;


    /**
     * @param b
     * @param ctx
     * @param keytab
     * @param serviceName
     * @param defaultServiceName
     * @param rm
     * @throws ServiceManagementException
     * @throws KerberosException
     * @throws JobBuilderException
     * @throws UnixAccountException
     * @throws UnitInitializationFailedException
     * @throws ADException
     */
    void ensureAcceptorCredentials ( @NonNull JobBuilder b, RuntimeConfigContext<?, ?> ctx, RealmManager rm, String keytab, String serviceName,
            String defaultServiceName ) throws ServiceManagementException, KerberosException, JobBuilderException, UnixAccountException,
                    UnitInitializationFailedException, ADException;


    /**
     * @param rm
     * @param princName
     * @return the principal name
     */
    String getInitiatorName ( RealmManager rm, String princName );


    /**
     * @param serviceName
     * @param rm
     * @param defaultServiceName
     * @return the service name
     * @throws KerberosException
     */
    String getAcceptorServiceName ( RealmManager rm, String serviceName, String defaultServiceName ) throws KerberosException;


    /**
     * @param rm
     * @throws JobBuilderException
     * @throws ADException
     */
    void checkJoin ( RealmManager rm ) throws ADException, JobBuilderException;


    /**
     * @param ctx
     * @throws KerberosException
     * @throws JobBuilderException
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    void writeKerberosConfig ( RuntimeConfigContext<?, ?> ctx )
            throws KerberosException, JobBuilderException, InvalidParameterException, UnitInitializationFailedException, ServiceManagementException;

}