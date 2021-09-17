/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2013 by mbechler
 */
package eu.agno3.runtime.jsf.extval.config;


import java.beans.Introspector;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.myfaces.extensions.validator.beanval.ExtValBeanValidationModuleConfiguration;
import org.apache.myfaces.extensions.validator.beanval.interceptor.BeanValidationTagAwareValidationInterceptor;
import org.apache.myfaces.extensions.validator.core.ExtValContext;
import org.apache.myfaces.extensions.validator.core.ExtValCoreConfiguration;
import org.apache.myfaces.extensions.validator.core.factory.DefaultFactoryFinder;

import eu.agno3.runtime.jsf.extval.AbstractStartupListenerFixed;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
public class ExtValConfigurator extends AbstractStartupListenerFixed {

    private static final String VALIDATOR_FACTORY_KEY = "javax.faces.validator.beanValidator.ValidatorFactory"; //$NON-NLS-1$

    private static final long serialVersionUID = 2147888987483769403L;
    private static final Logger log = Logger.getLogger(ExtValConfigurator.class);

    @Inject
    static CustomExtValBeanValidationModuleConfiguration bvConfig;

    @Inject
    static CustomExtValCoreConfiguration evConfig;


    @Override
    protected void init () {
        log.debug("Configuring ExtVal BV"); //$NON-NLS-1$
        clearStaticState();

        ExtValCoreConfiguration.use(evConfig, true);
        evConfig.init();
        ExtValBeanValidationModuleConfiguration.use(bvConfig, true);
        bvConfig.init();
        FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put(VALIDATOR_FACTORY_KEY, bvConfig.customValidatorFactory());
        ExtValContext.getContext().addPropertyValidationInterceptor(new BeanValidationTagAwareValidationInterceptor());
    }


    /**
     * 
     */
    private static void clearStaticState () {
        AbstractStartupListenerFixed.destroy();
        ExtValContext.clearContext();
        ( (DefaultFactoryFinder) DefaultFactoryFinder.getInstance() ).reset();
        Introspector.flushCaches();
    }


    @PreDestroy
    protected void preDestroy () {
        log.debug("Destroying ExtValContext"); //$NON-NLS-1$
        bvConfig = null;
        evConfig = null;
        clearStaticState();
    }
}
