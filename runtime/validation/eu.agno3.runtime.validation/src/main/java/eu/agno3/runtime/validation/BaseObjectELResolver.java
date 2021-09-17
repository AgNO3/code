/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2017 by mbechler
 */
package eu.agno3.runtime.validation;


import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;


/**
 * @author mbechler
 *
 */
public class BaseObjectELResolver extends ELResolver {

    private ELResolver delegate;
    private Object obj;


    /**
     * @param cel
     * @param obj
     */
    public BaseObjectELResolver ( CompositeELResolver cel, Object obj ) {
        this.delegate = cel;
        this.obj = obj;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.el.ELResolver#getCommonPropertyType(javax.el.ELContext, java.lang.Object)
     */
    @Override
    public Class<?> getCommonPropertyType ( ELContext ctx, Object base ) {
        return this.delegate.getCommonPropertyType(ctx, wrapBase(base));
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.el.ELResolver#getFeatureDescriptors(javax.el.ELContext, java.lang.Object)
     */
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( ELContext ctx, Object base ) {
        return this.delegate.getFeatureDescriptors(ctx, wrapBase(base));
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.el.ELResolver#getType(javax.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public Class<?> getType ( ELContext ctx, Object base, Object property ) {
        return this.delegate.getType(ctx, wrapBase(base), property);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.el.ELResolver#getValue(javax.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object getValue ( ELContext ctx, Object base, Object property ) {
        return this.delegate.getValue(ctx, wrapBase(base), property);
    }


    /**
     * @param base
     * @return
     */
    private final Object wrapBase ( Object base ) {
        if ( base == null ) {
            return this.obj;
        }
        return base;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.el.ELResolver#isReadOnly(javax.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean isReadOnly ( ELContext arg0, Object arg1, Object arg2 ) {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.el.ELResolver#setValue(javax.el.ELContext, java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public void setValue ( ELContext arg0, Object arg1, Object arg2, Object arg3 ) {

    }

}
