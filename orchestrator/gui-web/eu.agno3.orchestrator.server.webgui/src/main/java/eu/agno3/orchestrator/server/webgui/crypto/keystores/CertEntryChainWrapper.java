/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntryMutable;
import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;


/**
 * @author mbechler
 *
 */
public class CertEntryChainWrapper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4808197652432672682L;
    private ImportKeyPairEntryMutable ke;


    /**
     * @param ke
     */
    public CertEntryChainWrapper ( ImportKeyPairEntryMutable ke ) {
        this.ke = ke;
    }


    public List<Certificate> getChain () {

        if ( this.ke == null ) {
            return null;
        }

        List<X509CertEntry> certificateChain = this.ke.getCertificateChain();
        if ( certificateChain == null ) {
            return null;
        }

        List<Certificate> certs = new ArrayList<>();

        for ( X509CertEntry e : certificateChain ) {
            certs.add(e.getCertificate());
        }

        return certs;
    }


    public Certificate[] getChainArray () {
        return getChain().toArray(new Certificate[0]);
    }


    public void setChain ( List<Certificate> chain ) {

        if ( this.ke == null ) {
            return;
        }

        if ( chain == null ) {
            this.ke.setCertificateChain(null);
            return;
        }

        List<X509CertEntry> entries = new ArrayList<>();
        for ( Object cert : chain ) {
            if ( cert instanceof X509Certificate ) {
                entries.add(new X509CertEntry((X509Certificate) cert));
            }
        }

        this.ke.setCertificateChain(entries);
    }


    public void setChainArray ( Certificate[] chain ) {
        setChain(chain != null ? Arrays.asList(chain) : null);
    }
}
