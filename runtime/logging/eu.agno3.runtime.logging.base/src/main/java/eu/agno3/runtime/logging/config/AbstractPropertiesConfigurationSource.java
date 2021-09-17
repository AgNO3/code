/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractPropertiesConfigurationSource extends AbstractLoggerConfigurationSource {

    /**
     * @param prio
     */
    protected AbstractPropertiesConfigurationSource ( int prio ) {
        super(prio);
    }


    /**
     * Build configuration from a Properties object
     * 
     * @param props
     *            A properties object to convert
     * @return the converted Map
     * 
     */
    public Map<String, ?> getConfig ( Properties props ) {
        Map<String, Object> res = new HashMap<>();

        for ( Entry<Object, Object> entry : props.entrySet() ) {
            if ( entry.getKey() != null ) {
                res.put(entry.getKey().toString(), entry.getValue());
            }
        }

        return res;

    }

}