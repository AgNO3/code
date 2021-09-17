/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.util.Properties;

import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.TimestampsRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "deprecation" )
@Component ( service = CacheRegionFactory.class, immediate = true )
public class CacheRegionFactory implements RegionFactory {

    /**
     * 
     */
    private static final long serialVersionUID = -3637044635927505790L;
    private transient static org.hibernate.cache.spi.RegionFactory delegate;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void setRegionFactory ( RegionFactory rf ) {
        delegate = rf;
    }


    protected synchronized void unsetRegionFactory ( RegionFactory rf ) {
        if ( delegate == rf ) {
            delegate = null;
        }
    }


    /**
     * @return whether a cache implementation is available
     */
    public static boolean isInitialized () {
        return delegate != null;
    }


    @Override
    public CollectionRegion buildCollectionRegion ( String arg0, Properties arg1, CacheDataDescription arg2 ) throws CacheException {
        return delegate.buildCollectionRegion(arg0, arg1, arg2);
    }


    @Override
    public EntityRegion buildEntityRegion ( String arg0, Properties arg1, CacheDataDescription arg2 ) throws CacheException {
        return delegate.buildEntityRegion(arg0, arg1, arg2);
    }


    @Override
    public NaturalIdRegion buildNaturalIdRegion ( String arg0, Properties arg1, CacheDataDescription arg2 ) throws CacheException {
        return delegate.buildNaturalIdRegion(arg0, arg1, arg2);
    }


    @Override
    public QueryResultsRegion buildQueryResultsRegion ( String arg0, Properties arg1 ) throws CacheException {
        return delegate.buildQueryResultsRegion(arg0, arg1);
    }


    @Override
    public TimestampsRegion buildTimestampsRegion ( String arg0, Properties arg1 ) throws CacheException {
        return delegate.buildTimestampsRegion(arg0, arg1);
    }


    @Override
    public AccessType getDefaultAccessType () {
        return delegate.getDefaultAccessType();
    }


    @Override
    public boolean isMinimalPutsEnabledByDefault () {
        return delegate.isMinimalPutsEnabledByDefault();
    }


    @Override
    public long nextTimestamp () {
        return delegate.nextTimestamp();
    }


    @Override
    public void start ( SessionFactoryOptions arg0, Properties arg1 ) throws CacheException {
        delegate.start(arg0, arg1);
    }


    @Override
    public void stop () {
        delegate.stop();
    }

}
