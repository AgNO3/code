/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 30, 2017 by mbechler
 */
package eu.agno3.runtime.net.krb5.internal;


import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public class StandaloneKerberosRealmConfigImpl extends AbstractKerberosRealmConfigImpl {

    /**
     * @param realm
     * @param props
     */
    public StandaloneKerberosRealmConfigImpl ( String realm, Map<String, String> props ) {
        super(realm);
        loadProperties(props);
    }


    /**
     * @param realm
     * @param properties
     */
    public StandaloneKerberosRealmConfigImpl ( String realm, Dictionary<String, Object> properties ) {
        this(realm, toMap(properties));
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
