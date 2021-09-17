/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.04.2015 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import java.lang.reflect.Type;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.eclipse.persistence.exceptions.XMLMarshalException;
import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jaxb.TypeMappingInfo;
import org.eclipse.persistence.jaxb.compiler.Generator;
import org.eclipse.persistence.jaxb.compiler.TypeInfo;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLDescriptor;

import eu.agno3.runtime.xml.binding.ModularContext;


/**
 * @author mbechler
 *
 */
public class MOXYJAXBContext extends JAXBContext implements ModularContext {

    /**
     * @param contextInput
     * @throws JAXBException
     */
    public MOXYJAXBContext ( JAXBContextInput contextInput ) throws javax.xml.bind.JAXBException {
        super(contextInput);

    }


    /**
     * 
     * @return context state
     */
    public MOXYJAXBContextState getContextState () {
        return (MOXYJAXBContextState) this.contextState;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.xml.binding.ModularContext#getDelegate()
     */
    @Override
    public JAXBContext getDelegate () {
        return getContextState().getDelegate();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.xml.binding.ModularContext#clearState()
     */
    @Override
    public void clearState () {
        getContextState().clearState();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.xml.binding.ModularContext#lookupDescriptor(java.lang.Class)
     */
    @Override
    public XMLDescriptor lookupDescriptor ( Class<?> cls ) {
        TypeInfo typeInfo = this.getContextState().getGenerator().getAnnotationsProcessor().getTypeInfos().get(cls.getName());
        if ( typeInfo != null && typeInfo.getDescriptor() != null ) {
            return (XMLDescriptor) typeInfo.getDescriptor();
        }
        MOXYJAXBContext delegate = getContextState().getDelegate();
        if ( delegate == null ) {
            throw XMLMarshalException.descriptorNotFoundInProject(cls.getName());
        }
        typeInfo = delegate.getContextState().getGenerator().getAnnotationsProcessor().getTypeInfos().get(cls.getName());
        if ( typeInfo != null && typeInfo.getDescriptor() != null ) {
            return (XMLDescriptor) typeInfo.getDescriptor();
        }

        throw XMLMarshalException.descriptorNotFoundInProject(cls.getName());
    }

    /**
     * @author mbechler
     *
     */
    public static class MOXYJAXBContextState extends JAXBContextState {

        private Generator generator;
        private MOXYJAXBContext delegate;


        /**
         * 
         */
        public MOXYJAXBContextState () {
            super();
        }


        /**
         * 
         */
        public void clearState () {
            if ( this.delegate != null ) {
                this.generator.postInitialize();
            }
        }


        /**
         * @param delegate
         * @param context
         * @param generator
         * @param boundTypes
         * @param properties
         */
        public MOXYJAXBContextState ( MOXYJAXBContext delegate, XMLContext context, Generator generator, Type[] boundTypes,
                Map<String, Object> properties ) {
            super(context, generator, boundTypes, properties);
            this.delegate = delegate;
            this.generator = generator;
        }


        /**
         * @param delegate
         * @param context
         * @param generator
         * @param boundTypes
         * @param properties
         */
        public MOXYJAXBContextState ( MOXYJAXBContext delegate, XMLContext context, Generator generator, TypeMappingInfo[] boundTypes,
                Map<String, Object> properties ) {
            super(context, generator, boundTypes, properties);
            this.delegate = delegate;
            this.generator = generator;
        }


        /**
         * @param delegate
         * @param context
         */
        public MOXYJAXBContextState ( MOXYJAXBContext delegate, XMLContext context ) {
            super(context);
            this.delegate = delegate;
        }


        /**
         * @return the generator
         */
        public Generator getGenerator () {
            return this.generator;
        }


        /**
         * @return the delegate
         */
        public MOXYJAXBContext getDelegate () {
            return this.delegate;
        }

    }
}
