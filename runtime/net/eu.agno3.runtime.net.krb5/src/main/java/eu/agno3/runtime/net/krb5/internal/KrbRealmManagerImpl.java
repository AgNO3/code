/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5.internal;


import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KeyTab;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosRealm;
import eu.agno3.runtime.net.krb5.KrbRealmManager;
import eu.agno3.runtime.net.krb5.RealmType;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    KrbRealmManagerImpl.class, KrbRealmManager.class
}, configurationPid = "krbrealm", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class KrbRealmManagerImpl implements KrbRealmManager {

    private static final Logger log = Logger.getLogger(KrbRealmManagerImpl.class);

    private Path realmBase;
    private KerberosConfig kerberosConfig;


    @Reference
    protected void setKerberosConfig ( KerberosConfig kc ) {
        this.kerberosConfig = kc;
    }


    protected void unsetKerberosConfig ( KerberosConfig kc ) {
        if ( this.kerberosConfig == kc ) {
            this.kerberosConfig = null;
        }
    }


    /**
     * @return the kerberosConfig
     */
    public KerberosConfig getKerberosConfig () {
        return this.kerberosConfig;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        Dictionary<String, Object> cfg = ctx.getProperties();
        String defaultPath = "/etc/krbrealms"; //$NON-NLS-1$

        try {
            parseConfig(cfg, defaultPath);
        }
        catch ( KerberosException e ) {
            log.error("Failed to setup realm manager", e); //$NON-NLS-1$
        }
    }


    /**
     * @param cfg
     * @param defaultPath
     * @throws KerberosException
     */
    protected void parseConfig ( Dictionary<String, Object> cfg, String defaultPath ) throws KerberosException {
        String realmPath = ConfigUtil.parseString(
            cfg,
            "realmPath", //$NON-NLS-1$
            defaultPath);

        if ( StringUtils.isBlank(realmPath) ) {
            throw new KerberosException("No realm path configured"); //$NON-NLS-1$
        }

        this.realmBase = Paths.get(realmPath);

        if ( !Files.isDirectory(this.realmBase) || !Files.isReadable(this.realmBase) ) {
            log.error("Cannot read realm configuration path " + this.realmBase.toString()); //$NON-NLS-1$
        }
    }


    /**
     * @return the realmBase
     */
    @Override
    public Path getRealmBase () {
        return this.realmBase;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KrbRealmManager#getPermittedETypeAlgos()
     */
    @Override
    public Collection<Integer> getPermittedETypeAlgos () {
        return this.kerberosConfig.getPermittedETypes();
    }


    @Override
    public KerberosRealm getRealmInstance ( String realm ) throws KerberosException {
        return new KerberosRealmImpl(realm, this);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws KerberosException
     *
     * @see eu.agno3.runtime.net.krb5.KrbRealmManager#getKeytabData(java.lang.String, java.lang.String)
     */
    @Override
    public eu.agno3.runtime.net.krb5.KeyTab getKeytabData ( String realm, String ktId ) throws KerberosException {

        Path keyTabPath = getRealmConfig(realm).getKeyTabPath(ktId);

        try ( FileChannel fc = FileChannel.open(keyTabPath, StandardOpenOption.READ);
              InputStream fis = Channels.newInputStream(fc) ) {
            return eu.agno3.runtime.net.krb5.KeyTab.parse(fis);
        }
        catch ( IOException e ) {
            throw new KerberosException("Failed to load keytab " + keyTabPath, e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KrbRealmManager#getKeytab(java.lang.String, java.lang.String,
     *      javax.security.auth.kerberos.KerberosPrincipal)
     */
    @Override
    public KeyTab getKeytab ( String realm, String id, KerberosPrincipal servicePrincipal ) throws KerberosException {
        KeyTab kt = getRealmConfig(realm).getBoundKeyTab(id, servicePrincipal);
        KerberosKey[] keys = kt.getKeys(servicePrincipal);
        checkKeys(keys);
        return kt;
    }


    /**
     * @param keys
     * @throws KerberosException
     */
    private void checkKeys ( KerberosKey[] keys ) throws KerberosException {
        if ( keys == null || keys.length == 0 ) {
            throw new KerberosException("Keytab does not contain any usable keys"); //$NON-NLS-1$
        }

        boolean foundSupported = false;
        for ( KerberosKey key : keys ) {
            if ( !this.kerberosConfig.isSupported(key.getKeyType()) ) {
                continue;
            }
            foundSupported = true;
            break;
        }

        if ( !foundSupported ) {
            throw new KerberosException("Found no usable key in keytab"); //$NON-NLS-1$
        }
    }


    @Override
    public KeyTab getUnboundKeytab ( String realm, String id ) throws KerberosException {
        return getRealmConfig(realm).getUnboundKeyTab(id);
    }


    /**
     * @param realm
     * @return kerberos realm configuration
     * @throws KerberosException
     */
    @Override
    public RealmManagerKerberosRealmConfig getRealmConfig ( String realm ) throws KerberosException {
        Path realmDir = getRealmPath(realm);
        KerberosRealmConfigImpl rc = new KerberosRealmConfigImpl(realm, realmDir);
        this.kerberosConfig.ensureConfigured(rc);
        return rc;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws KerberosException
     *
     * @see eu.agno3.runtime.net.krb5.KrbRealmManager#getRealmType(java.lang.String)
     */
    @Override
    public RealmType getRealmType ( String name ) throws KerberosException {
        return getRealmConfig(name).getRealmType();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws KerberosException
     *
     * @see eu.agno3.runtime.net.krb5.KrbRealmManager#getProperties(java.lang.String)
     */
    @Override
    public Map<String, String> getProperties ( String name ) throws KerberosException {
        return getRealmConfig(name).getProperties();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KrbRealmManager#exists(java.lang.String)
     */
    @Override
    public boolean exists ( String realmName ) {
        Path realmDir = getRealmDir(realmName);
        return Files.exists(realmDir) && Files.isDirectory(realmDir);
    }


    /**
     * @param realmName
     * @return
     */
    protected Path getRealmDir ( String realmName ) {
        return this.getRealmBase().resolve(realmName.toUpperCase(Locale.ROOT));
    }


    @Override
    public Path getRealmPath ( String realm ) throws KerberosException {
        Path realmDir = getRealmDir(realm);
        if ( !Files.exists(realmDir) || !Files.isDirectory(realmDir) || !Files.isReadable(realmDir) ) {
            throw new KerberosException("Realm not configured " + realm); //$NON-NLS-1$
        }
        return realmDir;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws KerberosException
     *
     * @see eu.agno3.runtime.net.krb5.KrbRealmManager#listKeytabs(java.lang.String)
     */
    @Override
    public Collection<String> listKeytabs ( String krbRealm ) throws KerberosException {
        Path keyTabDir = getRealmConfig(krbRealm).getKeyTabsPath();
        try {
            return Files.list(keyTabDir).filter(x -> {
                return Files.isRegularFile(x) && x.getFileName().toString().endsWith(".keytab"); //$NON-NLS-1$
            }).map(x -> {
                String fname = x.getFileName().toString();
                return fname.substring(0, fname.length() - 7);
            }).collect(Collectors.toList());
        }
        catch ( IOException e ) {
            throw new KerberosException("Failed to enumerate keytabs", e); //$NON-NLS-1$
        }
    }

}
