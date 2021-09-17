/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


/**
 * @author mbechler
 *
 */
public interface RecursiveModificationListener {

    /**
     * @param modEntry
     */
    void notifyChange ( RuntimeModificationEntry modEntry );

}
