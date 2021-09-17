/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto;


import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.FingerprintUtil;


/**
 * @author mbechler
 *
 */
@Named ( "pubkeyEntryUtil" )
@ApplicationScoped
public class PubkeyEntryUtil {

    private static final Logger log = Logger.getLogger(PubkeyEntryUtil.class);


    /**
     * 
     * @param e
     * @return the certificate subject
     */
    public String pubkeyEntryReadOnlyMapper ( Object e ) {
        if ( ! ( e instanceof PublicKeyEntry ) || ( (PublicKeyEntry) e ).getPublicKey() == null ) {
            return StringUtils.EMPTY;
        }
        try {
            return String.format("SHA256:%s", Base64.encodeBase64String(FingerprintUtil.sha256( ( (PublicKeyEntry) e ).getPublicKey()))); //$NON-NLS-1$
        }
        catch ( CryptoException e1 ) {
            log.warn("Failed to produce public key fingerprint", e1); //$NON-NLS-1$
            return e.toString();
        }
    }


    /**
     * 
     * @param obj
     * @return cloned object
     */
    public PublicKeyEntry clonePublicKeyEntry ( Object obj ) {
        if ( ! ( obj instanceof PublicKeyEntry ) ) {
            return new PublicKeyEntry();
        }

        PublicKeyEntry toClone = (PublicKeyEntry) obj;

        if ( toClone.getPublicKey() != null ) {
            return new PublicKeyEntry(toClone.getPublicKey(), toClone.getComment());
        }
        return new PublicKeyEntry();
    }


    public PublicKeyWrapper getWrapper ( PublicKeyEntry pe ) {
        return new PublicKeyWrapper(pe);
    }


    /**
     * 
     * @return empty cert entry
     */
    public PublicKeyEntry makePublicKeyEntry () {
        return new PublicKeyEntry();
    }


    /**
     * 
     * @return comparator
     */
    public Comparator<PublicKeyEntry> getPublicKeyEntryComparator () {
        return new PubkeyEntryComparator();
    }
}
