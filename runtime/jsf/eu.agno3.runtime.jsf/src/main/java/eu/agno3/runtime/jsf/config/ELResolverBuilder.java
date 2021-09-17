/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 22, 2016 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.util.Iterator;
import java.util.List;

import javax.el.BeanELResolver;
import javax.el.ELResolver;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.unified.ResolverBuilderForFaces;
import org.apache.myfaces.el.unified.resolver.FacesCompositeELResolver.Scope;


/**
 * @author mbechler
 *
 */
public class ELResolverBuilder extends ResolverBuilderForFaces {

    /**
     * @param config
     */
    public ELResolverBuilder ( RuntimeConfig config ) {
        super(config);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.myfaces.el.unified.ResolverBuilderBase#filterELResolvers(java.util.List,
     *      org.apache.myfaces.el.unified.resolver.FacesCompositeELResolver.Scope)
     */
    @Override
    protected Iterable<ELResolver> filterELResolvers ( List<ELResolver> l, Scope arg1 ) {
        l.add(new CachingBeanELResolver());
        Iterator<ELResolver> iterator = l.iterator();
        while ( iterator.hasNext() ) {
            ELResolver er = iterator.next();
            if ( er instanceof BeanELResolver && ! ( er instanceof CachingBeanELResolver ) ) {
                iterator.remove();
            }
        }
        return super.filterELResolvers(l, arg1);
    }
}
