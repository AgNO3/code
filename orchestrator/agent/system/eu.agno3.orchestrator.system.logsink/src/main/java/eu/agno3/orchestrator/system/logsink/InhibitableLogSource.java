/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink;


/**
 * @author mbechler
 *
 */
public interface InhibitableLogSource {

    /**
     * 
     */
    void reenable ();


    /**
     * @param cursor
     */
    void checkpoint ( String cursor );

}
