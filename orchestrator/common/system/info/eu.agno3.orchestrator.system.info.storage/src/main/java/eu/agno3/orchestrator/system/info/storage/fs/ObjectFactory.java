/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.fs;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     * @return the default data fs information impl
     */
    public DataFileSystem createDataFileSystem () {
        return new DataFileSystemImpl();
    }


    /**
     * 
     * @return the default raid drive information impl
     */
    public SwapFileSystem createSwapFileSystem () {
        return new SwapFileSystemImpl();
    }

}
