/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


/**
 * @author mbechler
 *
 */
public interface OrchestratorEventLogConfigurationMutable extends OrchestratorEventLogConfiguration {

    /**
     * 
     * @param retainIndexedDays
     */
    void setRetainIndexedDays ( Long retainIndexedDays );


    /**
     * 
     * @param retainDays
     */
    void setRetainDays ( Long retainDays );


    /**
     * 
     * @param writeLogFiles
     */
    void setWriteLogFiles ( Boolean writeLogFiles );


    /**
     * 
     * @param eventStorage
     */
    void setEventStorage ( String eventStorage );


    /**
     * @param disableLogExpiration
     */
    void setDisableLogExpiration ( Boolean disableLogExpiration );

}
