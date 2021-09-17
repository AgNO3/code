/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:orchestrator:eventlog" )
public interface OrchestratorEventLogConfiguration extends ConfigurationObject {

    /**
     * 
     * @return storage used for event log data
     */
    String getEventStorage ();


    /**
     * 
     * @return number of days to keep log events
     */
    Long getRetainDays ();


    /**
     * 
     * @return number of days to keep log events indexed (accessible through web gui)
     */
    Long getRetainIndexedDays ();


    /**
     * @return whether to write log files (or only index)
     */
    Boolean getWriteLogFiles ();


    /**
     * @return whether not to expire log files
     */
    Boolean getDisableLogExpiration ();

}
