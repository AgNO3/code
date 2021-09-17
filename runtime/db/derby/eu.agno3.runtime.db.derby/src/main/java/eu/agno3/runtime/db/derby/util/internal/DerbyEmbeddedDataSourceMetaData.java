/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.08.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.util.internal;


import org.apache.derby.jdbc.ClientDriver;


/**
 * @author mbechler
 * 
 */
public class DerbyEmbeddedDataSourceMetaData extends AbstractDerbyDataSourceMetaData {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceMetaData#getDriverClassName()
     */
    @Override
    public String getDriverClassName () {
        return ClientDriver.class.getName();
    }

}
