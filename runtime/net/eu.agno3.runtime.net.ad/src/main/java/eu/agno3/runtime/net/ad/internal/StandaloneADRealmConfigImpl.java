/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 31, 2017 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public class StandaloneADRealmConfigImpl extends AbstractADRealmConfigImpl {

    /**
     * @param realm
     * @param stateDir
     * @param cfg
     */
    public StandaloneADRealmConfigImpl ( String realm, Path stateDir, Map<String, String> cfg ) {
        super(realm, stateDir);
        loadProperties(cfg);
    }


    /**
     * @param realm
     * @param stateDir
     * @param cfg
     */
    public StandaloneADRealmConfigImpl ( String realm, Path stateDir, Dictionary<String, Object> cfg ) {
        this(realm, stateDir, toMap(cfg));
    }


    /**
     * @param properties
     * @return
     */
    private static Map<String, String> toMap ( Dictionary<String, Object> properties ) {
        Map<String, String> m = new LinkedHashMap<>();
        Enumeration<String> keys = properties.keys();
        while ( keys.hasMoreElements() ) {
            String k = keys.nextElement();
            Object val = properties.get(k);
            if ( val instanceof String ) {
                m.put(k, (String) val);
            }
        }
        return m;
    }
}
