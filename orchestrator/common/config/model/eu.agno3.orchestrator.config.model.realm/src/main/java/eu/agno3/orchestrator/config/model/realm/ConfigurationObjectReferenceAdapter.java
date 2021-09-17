/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * @author mbechler
 * 
 */
public class ConfigurationObjectReferenceAdapter extends XmlAdapter<ConfigurationObject, ConfigurationObject> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public ConfigurationObject marshal ( ConfigurationObject obj ) {
        if ( obj == null || obj instanceof ConfigurationObjectReference ) {
            return obj;
        }

        return new ConfigurationObjectReference(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public ConfigurationObject unmarshal ( ConfigurationObject obj ) {
        return obj;
    }

}
