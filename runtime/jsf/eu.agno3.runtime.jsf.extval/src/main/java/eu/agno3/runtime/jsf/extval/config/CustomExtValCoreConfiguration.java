/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2013 by mbechler
 */
package eu.agno3.runtime.jsf.extval.config;


import javax.annotation.Generated;
import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.apache.myfaces.extensions.validator.ExtValInformation;
import org.apache.myfaces.extensions.validator.core.DefaultExtValCoreConfiguration;
import org.apache.myfaces.extensions.validator.core.ExtValContext;
import org.apache.myfaces.extensions.validator.core.ExtValCoreConfiguration;
import org.apache.myfaces.extensions.validator.core.PhaseIdRecordingPhaseListener;
import org.apache.myfaces.extensions.validator.core.interceptor.FacesMessagePropertyValidationInterceptor;
import org.apache.myfaces.extensions.validator.core.interceptor.HtmlCoreComponentsValidationExceptionInterceptor;
import org.apache.myfaces.extensions.validator.core.interceptor.ValidationInterceptor;
import org.apache.myfaces.extensions.validator.core.interceptor.ViolationExceptionInterceptor;
import org.apache.myfaces.extensions.validator.core.interceptor.ViolationSeverityValidationExceptionInterceptor;
import org.apache.myfaces.extensions.validator.core.metadata.transformer.mapper.BeanValidationStrategyToMetaDataTransformerNameMapper;
import org.apache.myfaces.extensions.validator.core.metadata.transformer.mapper.CustomConfiguredValidationStrategyToMetaDataTransformerNameMapper;
import org.apache.myfaces.extensions.validator.core.metadata.transformer.mapper.CustomConventionValidationStrategyToMetaDataTransformerNameMapper;
import org.apache.myfaces.extensions.validator.core.metadata.transformer.mapper.DefaultValidationStrategyToMetaDataTransformerNameMapper;
import org.apache.myfaces.extensions.validator.core.metadata.transformer.mapper.SimpleValidationStrategyToMetaDataTransformerNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.message.resolver.mapper.CustomConfiguredValidationStrategyToMsgResolverNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.message.resolver.mapper.CustomConventionValidationStrategyToMsgResolverNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.message.resolver.mapper.DefaultModuleValidationStrategyToMsgResolverNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.message.resolver.mapper.DefaultValidationStrategyToMsgResolverNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.message.resolver.mapper.SimpleValidationStrategyToMsgResolverNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.parameter.DefaultViolationSeverityInterpreter;
import org.apache.myfaces.extensions.validator.core.validation.parameter.DisableClientSideValidation;
import org.apache.myfaces.extensions.validator.core.validation.parameter.ViolationSeverity;
import org.apache.myfaces.extensions.validator.core.validation.strategy.mapper.AnnotationToValidationStrategyBeanNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.strategy.mapper.CustomConfiguredAnnotationToValidationStrategyNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.strategy.mapper.CustomConventionAnnotationToValidationStrategyNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.strategy.mapper.DefaultAnnotationToValidationStrategyNameMapper;
import org.apache.myfaces.extensions.validator.core.validation.strategy.mapper.SimpleAnnotationToValidationStrategyNameMapper;
import org.apache.myfaces.extensions.validator.util.ExtValUtils;
import org.apache.myfaces.extensions.validator.util.JsfUtils;

import eu.agno3.runtime.jsf.validation.CustomInformationProviderBean;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Generated ( "thirdparty" )
@SuppressWarnings ( "all" )
public class CustomExtValCoreConfiguration extends DefaultExtValCoreConfiguration {

    private static final Logger log = Logger.getLogger(CustomExtValCoreConfiguration.class);


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.myfaces.extensions.validator.core.DefaultExtValCoreConfiguration#customInformationProviderBeanClassName()
     */
    @Override
    public String customInformationProviderBeanClassName () {
        return CustomInformationProviderBean.class.getName();
    }


    protected void init () {
        if ( ExtValInformation.VERSION != null && !ExtValInformation.VERSION.startsWith("null") ) {
            log.debug("starting up MyFaces Extensions Validator v" + ExtValInformation.VERSION);
        }
        else {
            log.debug("starting up MyFaces Extensions Validator");
        }

        ExtValContext.getContext().registerRendererInterceptor(new ValidationInterceptor());

        initNameMappers();
        initValidationExceptionInterceptors();
        initViolationSeverityInterpreter();
        initPropertyValidationInterceptors();
        initPhaseListeners();
        initViolationSeverityKey();
        initDisableClientSideValidationKey();
        initRequiredInitialization();
    }


    private void initNameMappers () {
        if ( ExtValCoreConfiguration.get().deactivateDefaultNameMappers() ) {
            return;
        }

        // register metadata to validation strategy name mapper
        ExtValUtils.registerMetaDataToValidationStrategyNameMapper(new CustomConfiguredAnnotationToValidationStrategyNameMapper());
        ExtValUtils.registerMetaDataToValidationStrategyNameMapper(new CustomConventionAnnotationToValidationStrategyNameMapper());
        ExtValUtils.registerMetaDataToValidationStrategyNameMapper(new DefaultAnnotationToValidationStrategyNameMapper());
        ExtValUtils.registerMetaDataToValidationStrategyNameMapper(new SimpleAnnotationToValidationStrategyNameMapper());

        ExtValUtils.registerMetaDataToValidationStrategyNameMapper(new AnnotationToValidationStrategyBeanNameMapper(
            new CustomConfiguredAnnotationToValidationStrategyNameMapper()));
        ExtValUtils.registerMetaDataToValidationStrategyNameMapper(new AnnotationToValidationStrategyBeanNameMapper(
            new CustomConventionAnnotationToValidationStrategyNameMapper()));
        ExtValUtils.registerMetaDataToValidationStrategyNameMapper(new AnnotationToValidationStrategyBeanNameMapper(
            new DefaultAnnotationToValidationStrategyNameMapper()));
        ExtValUtils.registerMetaDataToValidationStrategyNameMapper(new AnnotationToValidationStrategyBeanNameMapper(
            new SimpleAnnotationToValidationStrategyNameMapper()));

        // register validation strategy to message resolver name mapper
        ExtValUtils.registerValidationStrategyToMessageResolverNameMapper(new CustomConfiguredValidationStrategyToMsgResolverNameMapper());
        ExtValUtils.registerValidationStrategyToMessageResolverNameMapper(new CustomConventionValidationStrategyToMsgResolverNameMapper());
        ExtValUtils.registerValidationStrategyToMessageResolverNameMapper(new DefaultValidationStrategyToMsgResolverNameMapper());
        ExtValUtils.registerValidationStrategyToMessageResolverNameMapper(new DefaultModuleValidationStrategyToMsgResolverNameMapper());
        ExtValUtils.registerValidationStrategyToMessageResolverNameMapper(new SimpleValidationStrategyToMsgResolverNameMapper());

        // register validation strategy to metadata transformer name mapper
        ExtValUtils
                .registerValidationStrategyToMetaDataTransformerNameMapper(new CustomConfiguredValidationStrategyToMetaDataTransformerNameMapper());
        ExtValUtils
                .registerValidationStrategyToMetaDataTransformerNameMapper(new CustomConventionValidationStrategyToMetaDataTransformerNameMapper());
        ExtValUtils.registerValidationStrategyToMetaDataTransformerNameMapper(new DefaultValidationStrategyToMetaDataTransformerNameMapper());
        ExtValUtils.registerValidationStrategyToMetaDataTransformerNameMapper(new SimpleValidationStrategyToMetaDataTransformerNameMapper());
        ExtValUtils.registerValidationStrategyToMetaDataTransformerNameMapper(new BeanValidationStrategyToMetaDataTransformerNameMapper());
    }


    private void initValidationExceptionInterceptors () {
        ExtValContext.getContext().addValidationExceptionInterceptor(new HtmlCoreComponentsValidationExceptionInterceptor());
        ExtValContext.getContext().addValidationExceptionInterceptor(new ViolationSeverityValidationExceptionInterceptor());

        ExtValContext.getContext().addValidationExceptionInterceptor(new ViolationExceptionInterceptor());
    }


    private void initViolationSeverityInterpreter () {
        ExtValContext.getContext().setViolationSeverityInterpreter(new DefaultViolationSeverityInterpreter(), false);
    }


    private void initPropertyValidationInterceptors () {
        ExtValContext.getContext().addPropertyValidationInterceptor(new FacesMessagePropertyValidationInterceptor());
    }


    private void initPhaseListeners () {
        JsfUtils.registerPhaseListener(new PhaseIdRecordingPhaseListener());
    }


    @Deprecated
    private void initViolationSeverityKey () {
        ExtValContext.getContext().addGlobalProperty(ViolationSeverity.class.getName(), ExtValCoreConfiguration.get().violationSeverity(), false);
    }


    @Deprecated
    private void initDisableClientSideValidationKey () {
        ExtValContext.getContext().addGlobalProperty(
            DisableClientSideValidation.class.getName(),
            ExtValCoreConfiguration.get().disableClientSideValidationValidationParameter(),
            false);
    }


    private void initRequiredInitialization () {
        boolean requiredInitialization = ExtValCoreConfiguration.get().activateRequiredInitialization();

        // noinspection deprecation
        addRequiredInitializationAsGlobalProperty(requiredInitialization);

        initRequiredAttributeSupport();
    }


    @Deprecated
    private void addRequiredInitializationAsGlobalProperty ( boolean requiredInitialization ) {
        ExtValContext.getContext().addGlobalProperty("mode:init:required", requiredInitialization, false);
    }


    /**
     * if it's configured that required init should happen,
     * it's required to deactivate the support for the required attribute
     */
    @Deprecated
    private void initRequiredAttributeSupport () {
        ExtValContext.getContext()
                .addGlobalProperty("mode:reset:required", ExtValCoreConfiguration.get().deactivateRequiredAttributeSupport(), false);
    }
}
