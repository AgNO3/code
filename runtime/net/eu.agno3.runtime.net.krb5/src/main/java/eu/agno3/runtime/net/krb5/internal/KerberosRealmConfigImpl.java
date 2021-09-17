/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5.internal;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KeyTab;

import org.apache.log4j.Logger;

import eu.agno3.runtime.net.krb5.KerberosException;


/**
 * @author mbechler
 *
 */
public class KerberosRealmConfigImpl extends AbstractKerberosRealmConfigImpl implements RealmManagerKerberosRealmConfig {

    private static final Logger log = Logger.getLogger(KerberosRealmConfigImpl.class);
    static final String REALM_PROPERTY_FILE = "realm.properties"; //$NON-NLS-1$

    private Path realmDir;
    private long lastModifiedTime;


    /**
     * @param realm
     * @param realmDir
     * @throws KerberosException
     */
    public KerberosRealmConfigImpl ( String realm, Path realmDir ) throws KerberosException {
        super(realm);
        this.realmDir = realmDir;

        Path file = realmDir.resolve(REALM_PROPERTY_FILE);

        if ( !Files.exists(file) || !Files.isReadable(file) ) {
            throw new KerberosException("Realm configuration file is not readable"); //$NON-NLS-1$
        }

        try {
            FileTime lastMod = Files.getLastModifiedTime(file);
            this.lastModifiedTime = lastMod.toMillis();
            readProperties(file);
        }
        catch ( IOException e ) {
            throw new KerberosException("Failed to read config file", e); //$NON-NLS-1$
        }

    }


    /**
     * 
     * @return modified time when the configuraiton was loaded
     */
    public long getLoadLastModified () {
        return this.lastModifiedTime;
    }


    /**
     * 
     * @return realm configuration file last modified time
     */
    public long getCurrentLastModified () {
        try {
            return Files.getLastModifiedTime(this.realmDir.resolve(REALM_PROPERTY_FILE)).toMillis();
        }
        catch ( IOException e ) {
            log.debug("Failed to get last modified time", e); //$NON-NLS-1$
            return -1;
        }
    }


    /**
     * 
     */
    @Override
    public void reload () {
        Path file = this.realmDir.resolve(REALM_PROPERTY_FILE);

        if ( !Files.exists(file) || !Files.isReadable(file) ) {
            return;
        }

        try {
            FileTime lastMod = Files.getLastModifiedTime(file);
            this.lastModifiedTime = lastMod.toMillis();
            readProperties(file);
        }
        catch ( IOException e ) {
            log.warn("Failed to read realm config file", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.internal.AbstractKerberosRealmConfigImpl#isModified()
     */
    @Override
    public boolean isModified () {
        return getLoadLastModified() != getCurrentLastModified();
    }


    /**
     * @param id
     * @param princ
     * @return the given keytab bound to the principal
     * @throws KerberosException
     */
    @Override
    public KeyTab getBoundKeyTab ( String id, KerberosPrincipal princ ) throws KerberosException {
        return KeyTab.getInstance(princ, getKeytabFile(id).toFile());
    }


    /**
     * @param id
     * @return the given keytab unbound
     * @throws KerberosException
     */
    @Override
    public KeyTab getUnboundKeyTab ( String id ) throws KerberosException {
        return KeyTab.getUnboundInstance(getKeytabFile(id).toFile());
    }


    /**
     * @param id
     * @return
     * @throws KerberosException
     */
    private Path getKeytabFile ( String id ) throws KerberosException {
        Path ktFile = getKeyTabPath(id);

        if ( !Files.exists(ktFile) || !Files.isReadable(ktFile) ) {
            throw new KerberosException("Keytab does not exist or is not readable"); //$NON-NLS-1$
        }

        return ktFile;
    }


    /**
     * @return the path where the keytabs are stored
     */
    @Override
    public Path getKeyTabsPath () {
        return this.realmDir.resolve("keytabs/"); //$NON-NLS-1$
    }


    /**
     * @param keyTabId
     * @return the keytab file path, even if non existant
     */
    @Override
    public Path getKeyTabPath ( String keyTabId ) {
        return this.realmDir.resolve(String.format("keytabs/%s.keytab", keyTabId)); //$NON-NLS-1$ ;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // + GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + super.hashCode();
        result = prime * result + (int) ( this.lastModifiedTime ^ ( this.lastModifiedTime >>> 32 ) );
        result = prime * result + ( ( this.realmDir == null ) ? 0 : this.realmDir.hashCode() );
        return result;
    }


    // - GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // + GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        KerberosRealmConfigImpl other = (KerberosRealmConfigImpl) obj;
        if ( this.lastModifiedTime != other.lastModifiedTime )
            return false;
        if ( !super.equals(obj) ) {
            return false;
        }
        if ( this.realmDir == null ) {
            if ( other.realmDir != null )
                return false;
        }
        else if ( !this.realmDir.equals(other.realmDir) )
            return false;
        return true;
    }

    // - GENERATED

}
