/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase;


import java.net.URL;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.db.schema.SchemaException;
import eu.agno3.runtime.db.schema.SchemaRegistration;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;


/**
 * @author mbechler
 * 
 */

@Component ( service = {
    LiquibaseChangeLogFactory.class
} )
public class LiquibaseChangeLogFactory {

    private static final Logger log = Logger.getLogger(LiquibaseChangeLogFactory.class);


    /**
     * @param changeFiles
     * @param params
     * @return a merged change set
     * @throws SchemaException
     */
    public DatabaseChangeLog parseChangeLogs ( SortedMap<URL, SchemaRegistration> changeFiles, ChangeLogParameters params ) throws SchemaException {

        DatabaseChangeLog global = new DatabaseChangeLog();

        for ( Entry<URL, SchemaRegistration> changeFile : changeFiles.entrySet() ) {
            try {
                String path = changeFile.getKey().getPath();
                ResourceAccessor resourceAccessor = new BundleResourceAccessor(changeFile.getValue().getBundle());
                if ( log.isDebugEnabled() ) {
                    log.debug("Adding change file " + path); //$NON-NLS-1$
                }
                ChangeLogParser p = ChangeLogParserFactory.getInstance().getParser(path, resourceAccessor);

                DatabaseChangeLog changeLog = p.parse(path, params, resourceAccessor);

                for ( ChangeSet changeSet : changeLog.getChangeSets() ) {
                    changeSet.setFilePath(changeFile.getValue().getBundle().getSymbolicName() + path);
                    global.addChangeSet(changeSet);
                }
            }
            catch ( LiquibaseException e ) {
                log.error("Failed to parse liquibase change log", e); //$NON-NLS-1$
                throw new SchemaException(e);
            }
        }

        return global;
    }
}
