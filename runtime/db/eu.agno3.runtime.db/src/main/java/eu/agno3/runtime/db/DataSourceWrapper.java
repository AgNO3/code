/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 24, 2017 by mbechler
 */
package eu.agno3.runtime.db;


/**
 * @author mbechler
 *
 */
public interface DataSourceWrapper {

    /**
     * Data source type property
     */
    static final String TYPE = "dataSourceType"; //$NON-NLS-1$

    /**
     * Plain (driver) data source
     */
    static final String TYPE_PLAIN = "plain"; //$NON-NLS-1$

    /**
     * Data source wrapped in pooling one
     */
    static final String TYPE_POOLED = "pooled"; //$NON-NLS-1$

    /**
     * Data source wrapper in XA pool
     */
    static final String TYPE_XA = "xa"; //$NON-NLS-1$
}
