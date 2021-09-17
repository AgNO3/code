/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.security.web.internal;


import java.util.Iterator;

import org.apache.log4j.Logger;

import eu.agno3.runtime.security.web.config.FilterChainEntry;


/**
 * @author mbechler
 * 
 */
public class PathMappingIterator implements Iterator<String> {

    private static final Logger log = Logger.getLogger(PathMappingIterator.class);

    /**
     * 
     */
    private final Iterator<FilterChainEntry> i;


    /**
     * @param i
     */
    public PathMappingIterator ( Iterator<FilterChainEntry> i ) {
        this.i = i;
    }


    @Override
    public boolean hasNext () {
        return this.i.hasNext();
    }


    @Override
    public String next () {
        FilterChainEntry e = this.i.next();
        log.debug(e.getPath());
        return e.getPath();
    }


    @Override
    public void remove () {
        this.i.remove();
    }
}