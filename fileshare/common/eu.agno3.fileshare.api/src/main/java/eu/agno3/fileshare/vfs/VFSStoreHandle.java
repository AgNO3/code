/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 9, 2016 by mbechler
 */
package eu.agno3.fileshare.vfs;


import eu.agno3.fileshare.exceptions.FileshareException;


/**
 * @author mbechler
 *
 */
public interface VFSStoreHandle {

    /**
     * 
     * @return the length of the stored data
     */
    long getLength ();


    /**
     * If possible, restore the data for resumption
     * 
     * @throws FileshareException
     */
    void revert () throws FileshareException;


    /**
     * 
     */
    void commit ();
}
