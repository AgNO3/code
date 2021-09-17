/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.02.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.internal;


import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.Locale;

import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.realms.ADRealmManager;
import eu.agno3.orchestrator.agent.realms.KeyTabManager;
import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.agent.realms.RealmsManager;
import eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil;
import eu.agno3.orchestrator.agent.realms.units.EnsureKeytabAccess;
import eu.agno3.orchestrator.config.realms.KerberosSecurityLevel;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = KerberosConfigUtil.class )
public class KerberosConfigUtilImpl implements KerberosConfigUtil {

    private RealmsManager realmsManager;


    @Reference
    protected synchronized void setRealmsManager ( RealmsManager rm ) {
        this.realmsManager = rm;
    }


    protected synchronized void unsetRealmsManager ( RealmsManager rm ) {
        if ( this.realmsManager == rm ) {
            this.realmsManager = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil#getRealmManager(java.lang.String)
     */
    @Override
    public RealmManager getRealmManager ( String realm ) throws KerberosException, JobBuilderException {
        RealmManager rm = this.realmsManager.getRealmManager(realm);
        if ( !rm.exists() ) {
            throw new JobBuilderException("Realm not configured " + realm); //$NON-NLS-1$
        }
        return rm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil#getRealmManager(java.lang.String,
     *      eu.agno3.runtime.net.krb5.RealmType)
     */
    @Override
    public RealmManager getRealmManager ( String realm, RealmType type ) throws JobBuilderException, KerberosException {
        RealmManager rm = this.realmsManager.getRealmManager(realm, type);
        if ( !rm.exists() ) {
            throw new JobBuilderException("Realm not configured " + realm); //$NON-NLS-1$
        }
        return rm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil#checkKeytab(java.lang.String,
     *      eu.agno3.orchestrator.agent.realms.KeyTabManager)
     */
    @Override
    public KerberosPrincipal checkKeytab ( String service, KeyTabManager ktm ) throws JobBuilderException {

        if ( !ktm.exists() ) {
            throw new JobBuilderException("Keytab does not exist " + ktm.getAlias()); //$NON-NLS-1$
        }

        for ( KerberosKey kerberosKey : ktm.listKeys() ) {
            if ( service.equals(kerberosKey.getPrincipal().getName()) ) {
                return kerberosKey.getPrincipal();
            }
        }

        throw new JobBuilderException(String.format("Service principal not found in keytab %s: %s", ktm.getAlias(), service)); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil#ensureInitiatorCredentials(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext,
     *      eu.agno3.orchestrator.agent.realms.RealmManager, java.lang.String, java.lang.String)
     */
    @Override
    public String ensureInitiatorCredentials ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, RealmManager rm, String keytab,
            String princName ) throws ServiceManagementException, JobBuilderException, UnitInitializationFailedException, ADException,
                    KerberosException, UnixAccountException {
        UserPrincipal servicePrincipal = ctx.getServiceManager().getServicePrincipal();
        String fullPrinc = null;
        if ( !StringUtils.isBlank(keytab) ) {
            KeyTabManager ktm = rm.getKeytabManager(keytab);
            if ( !StringUtils.isBlank(princName) ) {
                fullPrinc = getInitiatorName(rm, princName);
                checkKeytab(fullPrinc, ktm);
            }
            else {
                fullPrinc = findPrincipalInKeytab(ktm).getName();
            }

            if ( servicePrincipal != null ) {
                b.add(EnsureKeytabAccess.class).type(rm.getType()).realm(rm.getRealmName()).keytab(keytab).user(servicePrincipal);
            }
        }
        else if ( servicePrincipal != null ) {
            b.add(EnsureKeytabAccess.class).type(rm.getType()).realm(rm.getRealmName()).user(servicePrincipal);
        }

        if ( rm.getType() == RealmType.AD ) {
            if ( ( StringUtils.isBlank(keytab) || StringUtils.isBlank(princName) ) && ! ( (ADRealmManager) rm ).isJoined() ) {
                throw new JobBuilderException("No keytab/principal given for active directory and domain not joined to " + rm.getRealmName()); //$NON-NLS-1$
            }
            ctx.ensureFactory("ad", rm.getRealmName()); //$NON-NLS-1$
        }
        else {
            if ( ( StringUtils.isBlank(keytab) || StringUtils.isBlank(princName) ) ) {
                throw new JobBuilderException("No keytab/principal given for kerberos realm " + rm.getRealmName()); //$NON-NLS-1$
            }
            ctx.ensureFactory("krb", rm.getRealmName()); //$NON-NLS-1$
        }

        return fullPrinc;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil#findPrincipalInKeytab(eu.agno3.orchestrator.agent.realms.KeyTabManager)
     */
    @Override
    public KerberosPrincipal findPrincipalInKeytab ( KeyTabManager ktm ) throws JobBuilderException {
        if ( !ktm.exists() ) {
            throw new JobBuilderException("Keytab does not exist " + ktm.getAlias()); //$NON-NLS-1$
        }

        KerberosPrincipal foundPrinc = null;

        for ( KerberosKey kerberosKey : ktm.listKeys() ) {

            if ( foundPrinc == null ) {
                foundPrinc = kerberosKey.getPrincipal();
            }
            else if ( !foundPrinc.equals(kerberosKey.getPrincipal()) ) {
                throw new JobBuilderException("No principal configured, but keytab contains multiple"); //$NON-NLS-1$
            }
        }

        if ( foundPrinc == null ) {
            throw new JobBuilderException("Keytab is empty"); //$NON-NLS-1$
        }

        return foundPrinc;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil#ensureAcceptorCredentials(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext,
     *      eu.agno3.orchestrator.agent.realms.RealmManager, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void ensureAcceptorCredentials ( @NonNull JobBuilder b, RuntimeConfigContext<?, ?> ctx, RealmManager rm, String keytab, String serviceName,
            String defaultServiceName ) throws ServiceManagementException, KerberosException, JobBuilderException, UnixAccountException,
                    UnitInitializationFailedException, ADException {
        UserPrincipal servicePrincipal = ctx.getServiceManager().getServicePrincipal();
        if ( !StringUtils.isBlank(keytab) ) {
            String service = getAcceptorServiceName(rm, serviceName, defaultServiceName);
            KeyTabManager ktm = rm.getKeytabManager(keytab);
            checkKeytab(service, ktm);
            if ( servicePrincipal != null ) {
                b.add(EnsureKeytabAccess.class).type(rm.getType()).realm(rm.getRealmName()).keytab(keytab).user(servicePrincipal);
            }
        }
        else if ( servicePrincipal != null ) {
            b.add(EnsureKeytabAccess.class).type(rm.getType()).realm(rm.getRealmName()).user(servicePrincipal);
        }

        if ( rm.getType() == RealmType.AD ) {
            if ( StringUtils.isBlank(keytab) && ! ( (ADRealmManager) rm ).isJoined() ) {
                throw new JobBuilderException("No keytab given for active directory and domain not joined " + rm.getRealmName()); //$NON-NLS-1$
            }
            ctx.ensureFactory("ad", rm.getRealmName()); //$NON-NLS-1$
        }
        else {
            if ( StringUtils.isBlank(keytab) ) {
                throw new JobBuilderException("No keytab given for kerberos realm"); //$NON-NLS-1$
            }
            ctx.ensureFactory("krb", rm.getRealmName()); //$NON-NLS-1$
        }
    }


    @Override
    public void writeKerberosConfig ( RuntimeConfigContext<?, ?> ctx )
            throws KerberosException, JobBuilderException, InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        KerberosSecurityLevel min = null;

        for ( String pid : ctx.getConfiguredPids() ) {
            String realm;
            if ( pid.startsWith("ad@") ) { //$NON-NLS-1$
                realm = pid.substring(3);
            }
            else if ( pid.startsWith("krb@") ) { //$NON-NLS-1$
                realm = pid.substring(4);
            }
            else {
                continue;
            }

            RealmManager realmManager = getRealmManager(realm);
            try {
                KerberosSecurityLevel sl = KerberosSecurityLevel.valueOf((String) realmManager.getConfig().get("securityLevel")); //$NON-NLS-1$

                if ( min == null || sl.ordinal() > min.ordinal() ) {
                    min = sl;
                }
            }
            catch ( IllegalArgumentException e ) {
                throw new JobBuilderException("Invalid kerberos security level in realm config for " + realm, e); //$NON-NLS-1$
            }
        }

        PropertyConfigBuilder p = PropertyConfigBuilder.get();
        if ( min != null ) {
            p.p("permittedEnctypes", Arrays.asList(min.getEtypes())); //$NON-NLS-1$
        }

        ctx.instance("kerberos", p); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil#getInitiatorName(eu.agno3.orchestrator.agent.realms.RealmManager,
     *      java.lang.String)
     */
    @Override
    public String getInitiatorName ( RealmManager rm, String princName ) {
        return String.format("%s@%s", princName, rm.getRealmName().toUpperCase(Locale.ROOT)); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil#getAcceptorServiceName(eu.agno3.orchestrator.agent.realms.RealmManager,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public String getAcceptorServiceName ( RealmManager rm, String serviceName, String defaultServiceName ) throws KerberosException {
        String actualServiceName = serviceName;
        if ( StringUtils.isBlank(actualServiceName) ) {
            actualServiceName = defaultServiceName; // $NON-NLS-1$
        }

        String hostname = (String) rm.getConfig().get("localHostname"); //$NON-NLS-1$
        if ( StringUtils.isBlank(hostname) ) {
            hostname = LocalHostUtil.guessPrimaryHostName().toLowerCase(Locale.ROOT);
        }
        return String.format("%s/%s@%s", actualServiceName, hostname, rm.getRealmName().toUpperCase(Locale.ROOT)); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil#checkJoin(eu.agno3.orchestrator.agent.realms.RealmManager)
     */
    @Override
    public void checkJoin ( RealmManager rm ) throws ADException, JobBuilderException {
        if ( ! ( rm instanceof ADRealmManager ) ) {
            return;
        }
        ADRealmManager ad = (ADRealmManager) rm;
        if ( !ad.isJoined() ) {
            throw new JobBuilderException("Not joined to domain " + rm.getRealmName()); //$NON-NLS-1$
        }
    }

}
