/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto;


import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;
import eu.agno3.runtime.jsf.components.crypto.CertificateUtil;


/**
 * @author mbechler
 *
 */
@Named ( "certEntryUtil" )
@ApplicationScoped
public class CertEntryUtil {

    /**
     * 
     * @param e
     * @return the certificate subject
     */
    public String certEntryReadOnlyMapper ( Object e ) {

        if ( ! ( e instanceof X509CertEntry ) || ( (X509CertEntry) e ).getCertificate() == null ) {
            return StringUtils.EMPTY;
        }
        return CertificateUtil.formatPrincipalName( ( (X509CertEntry) e ).getCertificate().getSubjectX500Principal());
    }


    /**
     * 
     * @param obj
     * @return cloned object
     */
    public X509CertEntry cloneX509CertEntry ( Object obj ) {
        if ( ! ( obj instanceof X509CertEntry ) ) {
            return new X509CertEntry();
        }

        X509CertEntry toClone = (X509CertEntry) obj;

        if ( toClone.getCertificate() != null ) {
            return new X509CertEntry(toClone.getCertificate());
        }
        return new X509CertEntry();
    }


    /**
     * 
     * @return empty cert entry
     */
    public X509CertEntry makeCertEntry () {
        return new X509CertEntry();
    }


    /**
     * 
     * @return comparator
     */
    public Comparator<X509CertEntry> getCertEntryComparator () {
        return new CertEntryComparator();
    }
}
