/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.usage;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.server.webgui.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
public final class ConfigurationObjectDisplayComparator implements Comparator<ConfigurationObject>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1564836641330809633L;


    @Override
    public int compare ( ConfigurationObject o1, ConfigurationObject o2 ) {
        String name1 = ConfigUtil.getNonLocalizedDisplayNameFor(o1);
        String name2 = ConfigUtil.getNonLocalizedDisplayNameFor(o2);

        if ( name1 == null && name2 == null ) {
            // ignore
        }
        else if ( name1 == null ) {
            return -1;
        }
        else if ( name2 == null ) {
            return 1;
        }
        else {
            int res = name1.compareTo(name2);

            if ( res != 0 ) {
                return res;
            }
        }

        return ConfigUtil.getObjectTypeName(o1).compareTo(ConfigUtil.getObjectTypeName(o2));
    }
}