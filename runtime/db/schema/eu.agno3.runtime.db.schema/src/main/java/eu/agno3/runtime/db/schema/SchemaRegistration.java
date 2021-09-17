/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema;


import java.net.URL;
import java.util.SortedSet;

import org.osgi.framework.Bundle;


/**
 * @author mbechler
 * 
 */
public interface SchemaRegistration {

    /**
     * The datasource to which to apply
     * 
     * @return datasource name
     */
    String getDataSourceName ();


    /**
     * Ordered list of change files to apply
     * 
     * @return change files to apply
     */
    SortedSet<URL> getChangeFiles ();


    /**
     * @return the registering bundle
     */
    Bundle getBundle ();

}
