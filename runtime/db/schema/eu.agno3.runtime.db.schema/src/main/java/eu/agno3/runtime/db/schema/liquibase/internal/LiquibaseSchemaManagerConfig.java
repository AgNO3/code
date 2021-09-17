/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.01.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.jdbc.DataSourceFactory;

import liquibase.Contexts;
import liquibase.LabelExpression;


/**
 * @author mbechler
 * 
 */
@Component (
    service = LiquibaseSchemaManagerConfig.class,
    configurationPid = LiquibaseSchemaManagerConfig.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class LiquibaseSchemaManagerConfig {

    /**
     * 
     */
    public static final String MANAGEMENT_SCHEMA_PROPERTY = "managementSchema"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String CONTEXTS_PROPERTY = "contexts"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String MUST_BE_SET = "Must be set."; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(LiquibaseSchemaManagerConfig.class);

    /**
     * 
     */
    public static final String PID = "db.schema"; //$NON-NLS-1$

    private String dataSourceName;
    private String driverClass;

    private String[] contexts;
    private String managementSchema = "app_schema"; //$NON-NLS-1$


    /**
     * @return the data source to apply schema management to
     */
    public String getDataSourceName () {
        return this.dataSourceName;
    }


    /**
     * @return the JDBC driver class of the specified datasource
     */
    public String getDriverClass () {
        return this.driverClass;
    }


    /**
     * @return the configuration contexts to apply
     */
    public Contexts getContexts () {
        if ( this.contexts == null ) {
            return new Contexts();
        }
        return new Contexts(this.contexts);
    }


    /**
     * @return the internal database schema to store management information
     */
    public String getManagementSchema () {
        return this.managementSchema;
    }


    @Activate
    protected void activate ( ComponentContext context ) throws ConfigurationException {

        String dataSourceNameAttr = (String) context.getProperties().get(DataSourceFactory.JDBC_DATASOURCE_NAME);
        String driverClassAttr = (String) context.getProperties().get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String contextsAttr = (String) context.getProperties().get(CONTEXTS_PROPERTY);
        String managementSchemaAttr = (String) context.getProperties().get(MANAGEMENT_SCHEMA_PROPERTY);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Configuring schema management for datasource %s using driver %s", //$NON-NLS-1$
                dataSourceNameAttr,
                driverClassAttr));
        }

        if ( dataSourceNameAttr == null || dataSourceNameAttr.trim().isEmpty() ) {
            throw new ConfigurationException(DataSourceFactory.JDBC_DATASOURCE_NAME, MUST_BE_SET);
        }
        this.dataSourceName = dataSourceNameAttr.trim();

        if ( driverClassAttr == null || driverClassAttr.trim().isEmpty() ) {
            throw new ConfigurationException(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, MUST_BE_SET);
        }
        this.driverClass = driverClassAttr.trim();

        parseConfigProperties(contextsAttr, managementSchemaAttr);

    }


    /**
     * @param contextsAttr
     * @param managementSchemaAttr
     */
    void parseConfigProperties ( String contextsAttr, String managementSchemaAttr ) {
        if ( managementSchemaAttr != null ) {
            this.managementSchema = managementSchemaAttr;
        }

        if ( contextsAttr != null ) {
            String[] contextsTmp = contextsAttr.split(Pattern.quote(",")); //$NON-NLS-1$

            for ( int i = 0; i < contextsTmp.length; i++ ) {
                contextsTmp[ i ] = contextsTmp[ i ].trim();
            }

            this.contexts = contextsTmp;
        }
    }


    /**
     * @return a label expression (?)
     */
    public LabelExpression getLabelExpression () {
        return new LabelExpression();
    }

}
