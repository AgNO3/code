/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.internal;


import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.framework.Bundle;

import eu.agno3.runtime.db.schema.SchemaRegistration;


/**
 * @author mbechler
 * 
 */
public class SchemaRegistrationImpl implements SchemaRegistration {

    private Bundle bundle;
    private String dataSourceName;
    private SortedSet<URL> changeFiles;


    /**
     * @param dataSourceName
     * @param changeFiles
     */
    SchemaRegistrationImpl ( Bundle b, String dataSourceName, SortedSet<URL> changeFiles ) {
        super();
        this.bundle = b;
        this.dataSourceName = dataSourceName;
        this.changeFiles = changeFiles;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.SchemaRegistration#getDataSourceName()
     */
    @Override
    public String getDataSourceName () {
        return this.dataSourceName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.SchemaRegistration#getChangeFiles()
     */
    @Override
    public SortedSet<URL> getChangeFiles () {
        return new TreeSet<>(this.changeFiles);
    }


    /**
     * @return the bundle
     */
    @Override
    public Bundle getBundle () {
        return this.bundle;
    }
}
