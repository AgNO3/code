/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfig;


/**
 * @author mbechler
 *
 */
public class KeystoreComparator implements Comparator<KeystoreConfig>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8343940034382473883L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( KeystoreConfig o1, KeystoreConfig o2 ) {

        if ( o1.getAlias() == null && o2.getAlias() == null ) {
            return 0;
        }
        else if ( o1.getAlias() == null ) {
            return -1;
        }
        else if ( o2.getAlias() == null ) {
            return 1;
        }

        return o1.getAlias().compareTo(o2.getAlias());
    }

}
