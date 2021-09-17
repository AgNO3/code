/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.09.2014 by mbechler
 */
package eu.agno3.runtime.configloader.contribs;


import java.util.LinkedHashMap;
import java.util.Map;

import eu.agno3.runtime.configloader.ConfigContribution;
import eu.agno3.runtime.configloader.FactoryContribution;


/**
 * @author mbechler
 * 
 */
public class FactoryContributionImpl implements FactoryContribution {

    private final ConfigContribution source;
    private final String factoryPid;
    private final String instanceId;
    private Map<String, Object> properties = new LinkedHashMap<>();


    /**
     * @param source
     * @param factoryPid
     * @param instanceId
     */
    public FactoryContributionImpl ( ConfigContribution source, String factoryPid, String instanceId ) {
        this.source = source;
        this.factoryPid = factoryPid;
        this.instanceId = instanceId;
    }


    /**
     * @return the source
     */
    @Override
    public ConfigContribution getSource () {
        return this.source;
    }


    /**
     * @return the instanceId
     */
    @Override
    public String getInstanceId () {
        return this.instanceId;
    }


    /**
     * @return the factoryPid
     */
    @Override
    public String getFactoryPid () {
        return this.factoryPid;
    }


    /**
     * @return the properties
     */
    @Override
    public Map<String, Object> getProperties () {
        return this.properties;
    }

}
