/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2017 by mbechler
 */
package eu.agno3.runtime.validation;


import java.lang.reflect.Method;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;


/**
 * @author mbechler
 *
 */
public class ValidationELContext extends ELContext {

    private ELResolver resolver;
    private FunctionMapper functionMapper;
    private VariableMapper variableMapper;


    /**
     * @param obj
     * 
     */
    public ValidationELContext ( Object obj ) {
        CompositeELResolver cel = new CompositeELResolver();

        cel.add(new ArrayELResolver(true));
        cel.add(new ListELResolver(true));
        cel.add(new BeanELResolver(true));
        cel.add(new MapELResolver(true));

        this.resolver = new BaseObjectELResolver(cel, obj);
        this.variableMapper = new VariableMapper() {

            @Override
            public ValueExpression setVariable ( String var, ValueExpression exp ) {
                return null;
            }


            @Override
            public ValueExpression resolveVariable ( String var ) {
                return null;
            }
        };

        this.functionMapper = new FunctionMapper() {

            @Override
            public Method resolveFunction ( String arg0, String arg1 ) {
                return null;
            }
        };

    }


    /**
     * {@inheritDoc}
     *
     * @see javax.el.ELContext#getELResolver()
     */
    @Override
    public ELResolver getELResolver () {
        return this.resolver;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.el.ELContext#getFunctionMapper()
     */
    @Override
    public FunctionMapper getFunctionMapper () {
        return this.functionMapper;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.el.ELContext#getVariableMapper()
     */
    @Override
    public VariableMapper getVariableMapper () {
        return this.variableMapper;
    }
}
