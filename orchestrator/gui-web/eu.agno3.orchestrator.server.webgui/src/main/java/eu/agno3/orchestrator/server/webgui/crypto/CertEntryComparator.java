/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;


/**
 * @author mbechler
 *
 */
public class CertEntryComparator implements Comparator<X509CertEntry>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2781566837289443613L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( X509CertEntry o1, X509CertEntry o2 ) {

        if ( o1.getDerivedId() == null && o2.getDerivedId() == null ) {
            return 0;
        }
        else if ( o1.getDerivedId() == null ) {
            return -1;
        }
        else if ( o2.getDerivedId() == null ) {
            return 1;
        }

        return o1.getDerivedId().compareTo(o2.getDerivedId());
    }

}
