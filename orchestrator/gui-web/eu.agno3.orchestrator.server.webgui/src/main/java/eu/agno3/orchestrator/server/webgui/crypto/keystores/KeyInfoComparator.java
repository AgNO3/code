/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.text.Collator;
import java.util.Comparator;
import java.util.Objects;

import javax.faces.context.FacesContext;

import eu.agno3.orchestrator.crypto.keystore.KeyInfo;


/**
 * @author mbechler
 *
 */
public class KeyInfoComparator implements Comparator<KeyInfo> {

    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( KeyInfo o1, KeyInfo o2 ) {
        if ( ( o1.getCertificateChain() == null || o1.getCertificateChain().isEmpty() )
                && ( o2.getCertificateChain() == null || o2.getCertificateChain().isEmpty() ) ) {
            // falltrough
        }
        else if ( o1.getCertificateChain() == null || o1.getCertificateChain().isEmpty() ) {
            return -1;
        }
        else if ( o2.getCertificateChain() == null || o2.getCertificateChain().isEmpty() ) {
            return 1;
        }
        return Objects.compare(o1.getKeyAlias(), o2.getKeyAlias(), Collator.getInstance(FacesContext.getCurrentInstance().getViewRoot().getLocale()));
    }

}
