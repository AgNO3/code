/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import java.util.Map;

import org.eclipse.persistence.jaxb.xmlmodel.XmlBindings;
import org.osgi.framework.Bundle;


/**
 * @author mbechler
 * 
 */
public class BundleBindingDescriptors {

    private Bundle bundle;
    private Map<String, XmlBindings> bindings;


    /**
     * @param b
     * @param bindings
     */
    public BundleBindingDescriptors ( Bundle b, Map<String, XmlBindings> bindings ) {
        this.bundle = b;
        this.bindings = bindings;

    }


    /**
     * @return the bundle
     */
    public Bundle getBundle () {
        return this.bundle;
    }


    /**
     * @return the bindingFiles
     */
    public Map<String, XmlBindings> getBindings () {
        return this.bindings;
    }

}
