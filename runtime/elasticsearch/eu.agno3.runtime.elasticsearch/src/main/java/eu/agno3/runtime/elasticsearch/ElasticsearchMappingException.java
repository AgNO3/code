/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


/**
 * @author mbechler
 *
 */
public class ElasticsearchMappingException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3044646186421634326L;


    /**
     * 
     */
    public ElasticsearchMappingException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public ElasticsearchMappingException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public ElasticsearchMappingException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ElasticsearchMappingException ( Throwable cause ) {
        super(cause);
    }

}
