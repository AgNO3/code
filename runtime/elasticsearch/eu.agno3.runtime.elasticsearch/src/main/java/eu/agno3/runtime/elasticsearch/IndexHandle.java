/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 6, 2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


/**
 * @author mbechler
 *
 */
public interface IndexHandle {

    /**
     * @return index identifier
     */
    String getId ();


    /**
     * @return the index name to use for reading
     */
    String getReadName ();


    /**
     * 
     * @return the index name to use for writing
     */
    String getWriteName ();


    /**
     * 
     * @return the index name that is currently backing the data
     */
    String getBacking ();

}
