/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.05.2014 by mbechler
 */
package eu.agno3.runtime.xml.schema.internal;


import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;

import org.w3c.dom.ls.LSResourceResolver;


/**
 * @author mbechler
 * 
 */
public class ResourceResolverSettingProxy extends Schema {

    private Schema delegate;
    private LSResourceResolver resolver;


    /**
     * @param schema
     * @param resolver
     */
    public ResourceResolverSettingProxy ( Schema schema, LSResourceResolver resolver ) {
        this.delegate = schema;
        this.resolver = resolver;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.validation.Schema#newValidator()
     */
    @Override
    public Validator newValidator () {
        Validator v = this.delegate.newValidator();
        v.setResourceResolver(this.resolver);
        return v;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.validation.Schema#newValidatorHandler()
     */
    @Override
    public ValidatorHandler newValidatorHandler () {
        ValidatorHandler h = this.delegate.newValidatorHandler();
        h.setResourceResolver(this.resolver);
        return h;
    }

}
