/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.config.util;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.security.web.config.FilterChainBuilder;
import eu.agno3.runtime.security.web.config.FilterChainSpecBuilder;


/**
 * @author mbechler
 * 
 */
public class FilterChainSpecBuilderImpl implements FilterChainSpecBuilder {

    private FilterChainBuilder parent;
    private int priority;
    private String path;
    private List<String> elements = new ArrayList<>();


    /**
     * @param parent
     * @param priority
     * @param path
     */
    public FilterChainSpecBuilderImpl ( FilterChainBuilder parent, int priority, String path ) {
        this.parent = parent;
        this.priority = priority;
        this.path = path;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainSpecBuilder#append(java.lang.String)
     */
    @Override
    public FilterChainSpecBuilder append ( String filterName ) {
        this.elements.add(filterName);
        return this;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainSpecBuilder#append(java.lang.String, java.lang.String)
     */
    @Override
    public FilterChainSpecBuilder append ( String filterName, String filterConfigSpec ) {
        this.elements.add(String.format("%s[%s]", filterName, filterConfigSpec)); //$NON-NLS-1$
        return this;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainSpecBuilder#complete()
     */
    @Override
    public FilterChainBuilder complete () {
        this.parent.withChain(this.priority, this.path, StringUtils.join(this.elements, ",")); //$NON-NLS-1$
        return this.parent;
    }

}
