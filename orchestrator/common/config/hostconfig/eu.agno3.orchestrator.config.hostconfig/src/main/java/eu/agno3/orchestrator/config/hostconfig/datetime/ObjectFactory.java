/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.datetime;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * @return default date time config impl
     */
    public DateTimeConfiguration createDateTimeConfiguration () {
        return new DateTimeConfigurationImpl();
    }
}
