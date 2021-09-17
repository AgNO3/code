/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.08.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.util.internal;


import org.apache.derby.jdbc.EmbeddedDriver;


/**
 * @author mbechler
 * 
 */
public class DerbyNetworkDataSourceMetaData extends AbstractDerbyDataSourceMetaData {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceMetaData#getDriverClassName()
     */
    @Override
    public String getDriverClassName () {
        return EmbeddedDriver.class.getName();
    }

}
