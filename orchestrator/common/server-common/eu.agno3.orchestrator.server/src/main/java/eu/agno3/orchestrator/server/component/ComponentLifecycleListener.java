/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public interface ComponentLifecycleListener <T extends ComponentConfig> {

    /**
     * @param c
     */
    void connecting ( T c );


    /**
     * @param c
     */
    void connected ( T c );


    /**
     * @param c
     */
    void disconnecting ( T c );


    /**
     * @param c
     */
    void failed ( T c );

}
