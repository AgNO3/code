/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;


class DynamicHibernateBundleInfo {

    private Bundle bundle;
    private Map<String, List<Class<? extends Object>>> classRegistrations = new HashMap<>();
    private Map<String, List<URL>> mappingFiles = new HashMap<>();


    /**
     * @param b
     * 
     */
    public DynamicHibernateBundleInfo ( Bundle b ) {
        this.bundle = b;
    }


    /**
     * @return the bundle
     */
    public Bundle getBundle () {
        return this.bundle;
    }


    /**
     * @return the classRegistrations
     */
    public Map<String, List<Class<? extends Object>>> getClassRegistrations () {
        return this.classRegistrations;
    }


    /**
     * @return the mappingFiles
     */
    public Map<String, List<URL>> getMappingFiles () {
        return this.mappingFiles;
    }
}