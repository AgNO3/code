/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KeyTab;

import eu.agno3.runtime.net.krb5.KerberosException;


/**
 * @author mbechler
 *
 */
public class ADRealmConfigImpl extends AbstractADRealmConfigImpl implements RealmManagerADRealmConfig {

    /**
     * @param domain
     * @param base
     * @throws KerberosException
     */
    public ADRealmConfigImpl ( String domain, Path base ) throws KerberosException {
        super(domain.toUpperCase(Locale.ROOT), base);
    }


    /**
     * @param id
     * @param princ
     * @return the given keytab bound to the principal
     * @throws KerberosException
     */
    @Override
    public KeyTab getBoundKeyTab ( String id, KerberosPrincipal princ ) throws KerberosException {
        return KeyTab.getInstance(princ, getKeyTabPath(id).toFile());
    }


    /**
     * @param id
     * @return the given keytab unbound
     * @throws KerberosException
     */
    @Override
    public KeyTab getUnboundKeyTab ( String id ) throws KerberosException {
        return KeyTab.getUnboundInstance(getKeyTabPath(id).toFile());
    }


    @Override
    public Path getKeyTabPath ( String id ) throws KerberosException {
        Path ktFile = getKeyTabsPath().resolve(String.format("%s.keytab", id)); //$NON-NLS-1$ ;

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
        return getStateDir().resolve("keytabs/"); //$NON-NLS-1$
    }

}
