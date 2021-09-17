/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema;


import java.net.URL;
import java.util.SortedMap;


/**
 * @author mbechler
 * 
 */
public interface ChangeFileProvider {

    /**
     * Get all change files for a datasource
     * 
     * @param dataSource
     * @param onlyModified
     *            only return modified change log files
     * @return the change files registered for the datasource
     */
    SortedMap<URL, SchemaRegistration> getChangeFiles ( String dataSource, boolean onlyModified );


    /**
     * @param dataSourceName
     * @param time
     */
    void trackApplied ( String dataSourceName, long time );

}
