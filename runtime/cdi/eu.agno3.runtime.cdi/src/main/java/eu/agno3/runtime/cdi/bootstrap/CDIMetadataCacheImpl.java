/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.05.2014 by mbechler
 */
package eu.agno3.runtime.cdi.bootstrap;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.cdi.CDICacheEntry;
import eu.agno3.runtime.cdi.CDIMetadataCache;


/**
 * @author mbechler
 * 
 */
@Component ( service = CDIMetadataCache.class )
public class CDIMetadataCacheImpl implements CDIMetadataCache {

    private Map<String, CDICacheEntry> cache = new ConcurrentHashMap<>();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.cdi.CDIMetadataCache#putCache(org.osgi.framework.Bundle,
     *      eu.agno3.runtime.cdi.CDICacheEntry)
     */
    @Override
    public void putCache ( Bundle b, CDICacheEntry cached ) {
        this.cache.put(b.getSymbolicName(), cached);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.cdi.CDIMetadataCache#getCached(org.osgi.framework.Bundle)
     */
    @Override
    public CDICacheEntry getCached ( Bundle b ) {
        return this.cache.get(b.getSymbolicName());
    }

}
