/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2013 by mbechler
 */
package eu.agno3.runtime.jsf.extval.config;


import javax.annotation.Generated;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.apache.myfaces.extensions.validator.beanval.BeanValidationModuleValidationInterceptor;
import org.apache.myfaces.extensions.validator.beanval.DefaultExtValBeanValidationModuleConfiguration;
import org.apache.myfaces.extensions.validator.beanval.ExtValBeanValidationContext;
import org.apache.myfaces.extensions.validator.beanval.HtmlCoreComponentsComponentInitializer;
import org.apache.myfaces.extensions.validator.beanval.MappedConstraintSourceBeanValidationModuleValidationInterceptor;
import org.apache.myfaces.extensions.validator.beanval.ValidatorFactoryProxy;
import org.apache.myfaces.extensions.validator.beanval.interceptor.ExtValBeanValidationMetaDataExtractionInterceptor;
import org.apache.myfaces.extensions.validator.beanval.metadata.transformer.mapper.NotNullNameMapper;
import org.apache.myfaces.extensions.validator.beanval.metadata.transformer.mapper.SizeNameMapper;
import org.apache.myfaces.extensions.validator.beanval.payload.DisableClientSideValidation;
import org.apache.myfaces.extensions.validator.beanval.payload.ViolationSeverity;
import org.apache.myfaces.extensions.validator.beanval.storage.DefaultModelValidationStorageManager;
import org.apache.myfaces.extensions.validator.beanval.storage.ModelValidationStorage;
import org.apache.myfaces.extensions.validator.beanval.storage.mapper.BeanValidationGroupStorageNameMapper;
import org.apache.myfaces.extensions.validator.beanval.storage.mapper.ModelValidationStorageNameMapper;
import org.apache.myfaces.extensions.validator.beanval.validation.ModelValidationPhaseListener;
import org.apache.myfaces.extensions.validator.beanval.validation.strategy.BeanValidationVirtualValidationStrategy;
import org.apache.myfaces.extensions.validator.core.ExtValContext;
import org.apache.myfaces.extensions.validator.core.factory.AbstractNameMapperAwareFactory;
import org.apache.myfaces.extensions.validator.core.factory.FactoryNames;
import org.apache.myfaces.extensions.validator.core.storage.GroupStorage;
import org.apache.myfaces.extensions.validator.core.storage.StorageManager;
import org.apache.myfaces.extensions.validator.core.storage.StorageManagerHolder;
import org.apache.myfaces.extensions.validator.core.validation.message.resolver.DefaultMessageResolverFactory;
import org.apache.myfaces.extensions.validator.core.validation.message.resolver.MessageResolver;
import org.apache.myfaces.extensions.validator.util.ExtValUtils;
import org.apache.myfaces.extensions.validator.util.JsfUtils;
import org.ops4j.pax.cdi.api.OsgiService;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Generated ( "thirdparty" )
@SuppressWarnings ( "all" )
public class CustomExtValBeanValidationModuleConfiguration extends DefaultExtValBeanValidationModuleConfiguration {

    private static final Logger log = Logger.getLogger(CustomExtValBeanValidationModuleConfiguration.class);

    @Inject
    @OsgiService ( dynamic = true )
    private ValidatorFactory validatorFactory;

    private MessageResolver messageResolver;


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.myfaces.extensions.validator.beanval.ExtValBeanValidationModuleConfiguration#customValidatorFactory()
     */
    @Override
    public ValidatorFactory customValidatorFactory () {
        log.debug("Get custom validator factory"); //$NON-NLS-1$
        return this.validatorFactory;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.myfaces.extensions.validator.beanval.DefaultExtValBeanValidationModuleConfiguration#customExtValBeanValidationContext()
     */
    @Override
    public ExtValBeanValidationContext customExtValBeanValidationContext () {
        return new CustomExtValBeanValidationContext(this.messageResolver);
    }


    protected void init () {
        this.messageResolver = new DefaultMessageResolverFactory().create(new BeanValidationVirtualValidationStrategy(null, null));
        registerValidatorFactory();
        registerBeanValidationInterceptors();
        registerMetaDataTransformerNameMapper();
        registerGroupStorageNameMapper();
        registerModelValidationStorageNameMapper();
        registerComponentInitializers();
        registerMetaDataExtractionInterceptors();
        registerPhaseListeners();
        registerViolationSeverityPayload();
        registerDisableClientSideValidationPayload();
    }


    protected void registerValidatorFactory () {
        ExtValContext.getContext().addGlobalProperty(ValidatorFactory.class.getName(), new ValidatorFactoryProxy(), false);
    }


    protected void registerBeanValidationInterceptors () {
        ExtValContext.getContext().registerRendererInterceptor(new BeanValidationModuleValidationInterceptor());
        ExtValContext.getContext().registerRendererInterceptor(new MappedConstraintSourceBeanValidationModuleValidationInterceptor());
    }


    protected void registerMetaDataTransformerNameMapper () {
        ExtValUtils.registerValidationStrategyToMetaDataTransformerNameMapper(new SizeNameMapper());
        ExtValUtils.registerValidationStrategyToMetaDataTransformerNameMapper(new NotNullNameMapper());
    }


    @SuppressWarnings ( {
        "unchecked"
    } )
    protected void registerGroupStorageNameMapper () {
        StorageManager storageManager = getStorageManagerHolder().getStorageManager(GroupStorage.class);

        if ( storageManager instanceof AbstractNameMapperAwareFactory ) {
            ( (AbstractNameMapperAwareFactory<String>) storageManager ).register(new BeanValidationGroupStorageNameMapper());
        }
        else {
            log.warn(
                storageManager.getClass().getName() + " has to implement AbstractNameMapperAwareFactory " + getClass().getName()
                        + " couldn't register " + BeanValidationGroupStorageNameMapper.class.getName());
        }
    }


    protected void registerModelValidationStorageNameMapper () {
        DefaultModelValidationStorageManager modelValidationStorageManager = new DefaultModelValidationStorageManager();
        modelValidationStorageManager.register(new ModelValidationStorageNameMapper());
        getStorageManagerHolder().setStorageManager(ModelValidationStorage.class, modelValidationStorageManager, false);
    }


    protected void registerComponentInitializers () {
        ExtValContext.getContext().addComponentInitializer(new HtmlCoreComponentsComponentInitializer());
    }


    protected StorageManagerHolder getStorageManagerHolder () {
        return ( ExtValContext.getContext().getFactoryFinder().getFactory(FactoryNames.STORAGE_MANAGER_FACTORY, StorageManagerHolder.class) );
    }


    protected void registerMetaDataExtractionInterceptors () {
        ExtValContext.getContext().addMetaDataExtractionInterceptor(new ExtValBeanValidationMetaDataExtractionInterceptor());
    }


    protected void registerPhaseListeners () {
        JsfUtils.registerPhaseListener(new ModelValidationPhaseListener());
    }


    protected void registerViolationSeverityPayload () {
        ExtValContext extValContext = ExtValContext.getContext();

        extValContext.addGlobalProperty(ViolationSeverity.Info.class.getName(), ViolationSeverity.Info.class, false);
        extValContext.addGlobalProperty(ViolationSeverity.Warn.class.getName(), ViolationSeverity.Warn.class, false);
        extValContext.addGlobalProperty(ViolationSeverity.Fatal.class.getName(), ViolationSeverity.Fatal.class, false);

        // no need to register "error" it's the default
    }


    private void registerDisableClientSideValidationPayload () {
        ExtValContext.getContext().addGlobalProperty(DisableClientSideValidation.class.getName(), DisableClientSideValidation.class, false);
    }

}
