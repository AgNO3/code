/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.04.2015 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.internal.jaxb.JaxbClassLoader;
import org.eclipse.persistence.jaxb.JAXBContext.JAXBContextInput;
import org.eclipse.persistence.jaxb.TypeMappingInfo;
import org.eclipse.persistence.jaxb.compiler.AnnotationsProcessor;
import org.eclipse.persistence.jaxb.compiler.ElementDeclaration;
import org.eclipse.persistence.jaxb.compiler.Generator;
import org.eclipse.persistence.jaxb.compiler.MappingsGenerator;
import org.eclipse.persistence.jaxb.compiler.PackageInfo;
import org.eclipse.persistence.jaxb.compiler.TypeInfo;
import org.eclipse.persistence.jaxb.javamodel.Helper;
import org.eclipse.persistence.jaxb.javamodel.JavaClass;
import org.eclipse.persistence.jaxb.javamodel.reflection.JavaModelImpl;
import org.eclipse.persistence.jaxb.javamodel.reflection.JavaModelInputImpl;
import org.eclipse.persistence.jaxb.xmlmodel.XmlBindings;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLLogin;
import org.eclipse.persistence.oxm.platform.SAXPlatform;
import org.eclipse.persistence.oxm.platform.XMLPlatform;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.sessions.SessionEventListener;

import eu.agno3.runtime.xml.binding.internal.MOXYJAXBContext.MOXYJAXBContextState;


/**
 * @author mbechler
 *
 */
public class MOXYJAXBContextInput extends JAXBContextInput {

    private static final boolean USE_MODULAR_BINDING = true;
    private Set<Class<?>> classes;
    private Map<String, XmlBindings> xmlBindings;

    private MOXYJAXBContext delegate;
    private Collection<SessionEventListener> sessionEventListeners;


    /**
     * @param classes
     * @param delegate
     * @param bindings
     * @param properties
     * @param classLoader
     * @param eventListeners
     */
    public MOXYJAXBContextInput ( Set<Class<?>> classes, MOXYJAXBContext delegate, Map<String, XmlBindings> bindings, Map<?, ?> properties,
            ClassLoader classLoader, Collection<SessionEventListener> eventListeners ) {
        super(properties, classLoader);
        this.classes = classes;
        this.delegate = delegate;
        this.xmlBindings = bindings;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.persistence.jaxb.JAXBContext.JAXBContextInput#createContextState()
     */
    @Override
    protected MOXYJAXBContextState createContextState () throws JAXBException {
        Class<?>[] localClasses = this.classes.toArray(new Class[] {});
        JaxbClassLoader loader = new JaxbClassLoader(this.classLoader, localClasses);
        JavaModelImpl jModel = new JavaModelImpl(loader);

        // create Map of package names to metadata complete indicators
        Map<String, Boolean> metadataComplete = new HashMap<>();
        for ( String packageName : this.xmlBindings.keySet() ) {
            if ( this.xmlBindings.get(packageName).isXmlMappingMetadataComplete() ) {
                metadataComplete.put(packageName, true);
            }
        }
        if ( metadataComplete.size() > 0 ) {
            jModel.setMetadataCompletePackageMap(metadataComplete);
        }

        jModel.setHasXmlBindings(true);
        JavaModelInputImpl inputImpl = new JavaModelInputImpl(localClasses, jModel);

        try {
            Generator generator = new Generator(inputImpl, this.xmlBindings, loader, null, false);
            MappingsGenerator mappingsGenerator;

            if ( USE_MODULAR_BINDING && this.delegate != null ) {
                mappingsGenerator = new ModularGenerator(new Helper(inputImpl.getJavaModel()), this.delegate);
            }
            else {
                mappingsGenerator = generator.getMappingsGenerator();
            }
            MOXYJAXBContextState state = createContextState(generator, mappingsGenerator, loader, localClasses);
            return state;
        }
        catch ( Exception ex ) {
            throw new javax.xml.bind.JAXBException(ex.getMessage(), ex);
        }
    }


    @SuppressWarnings ( "rawtypes" )
    private Project generateProject ( AnnotationsProcessor annotationsProcessor, MappingsGenerator mappingsGenerator ) throws Exception {

        Map<String, Class> arrayClassesToGeneratedClasses = annotationsProcessor.getArrayClassesToGeneratedClasses();
        List<JavaClass> typeInfoClasses = annotationsProcessor.getTypeInfoClasses();
        Map<String, TypeInfo> typeInfo = annotationsProcessor.getTypeInfos();
        Map<String, QName> userDefinedSchemaTypes = annotationsProcessor.getUserDefinedSchemaTypes();
        Map<String, PackageInfo> packageToPackageInfoMappings = annotationsProcessor.getPackageToPackageInfoMappings();
        Map<QName, ElementDeclaration> globalElements = annotationsProcessor.getGlobalElements();
        List<ElementDeclaration> localElements = annotationsProcessor.getLocalElements();
        Map<TypeMappingInfo, Class> typeMappingInfoToGeneratedClasses = annotationsProcessor.getTypeMappingInfosToGeneratedClasses();
        boolean defaultNamespaceAllowed = annotationsProcessor.isDefaultNamespaceAllowed();
        Map<String, Class> classToGeneratedClasses = mappingsGenerator.getClassToGeneratedClasses();
        Map<TypeMappingInfo, Class> typeMappingInfoToAdapterClasses = annotationsProcessor.getTypeMappingInfoToAdapterClasses();

        if ( this.delegate != null ) {
            AnnotationsProcessor delegateAnnot = this.delegate.getContextState().getGenerator().getAnnotationsProcessor();
            packageToPackageInfoMappings.putAll(delegateAnnot.getPackageToPackageInfoMappings());
            globalElements.putAll(delegateAnnot.getGlobalElements());
            localElements.addAll(delegateAnnot.getLocalElements());
            typeMappingInfoToGeneratedClasses.putAll(delegateAnnot.getTypeMappingInfosToGeneratedClasses());
            classToGeneratedClasses.putAll(this.delegate.getContextState().getGenerator().getMappingsGenerator().getClassToGeneratedClasses());
            typeMappingInfoToAdapterClasses.putAll(delegateAnnot.getTypeMappingInfoToAdapterClasses());
            userDefinedSchemaTypes.putAll(delegateAnnot.getUserDefinedSchemaTypes());
            typeInfo.putAll(delegateAnnot.getTypeInfos());
            arrayClassesToGeneratedClasses.putAll(delegateAnnot.getArrayClassesToGeneratedClasses());
            typeInfoClasses.addAll(delegateAnnot.getTypeInfoClasses());
        }

        classToGeneratedClasses.putAll(arrayClassesToGeneratedClasses);

        Project p = (Project) mappingsGenerator.generateProject(
            typeInfoClasses,
            typeInfo,
            userDefinedSchemaTypes,
            packageToPackageInfoMappings,
            globalElements,
            localElements,
            typeMappingInfoToGeneratedClasses,
            typeMappingInfoToAdapterClasses,
            defaultNamespaceAllowed);
        arrayClassesToGeneratedClasses.putAll(classToGeneratedClasses);
        return p;
    }


    private MOXYJAXBContextState createContextState ( Generator generator, MappingsGenerator mappingGenerator, JaxbClassLoader loader,
            Type[] localTypes ) throws Exception {
        Project proj = this.generateProject(generator.getAnnotationsProcessor(), mappingGenerator);
        ConversionManager conversionManager = new ConversionManager();
        conversionManager.setLoader(loader);

        convertClassNames(loader, proj);

        XMLPlatform<?> platform = new SAXPlatform();
        platform.getConversionManager().setLoader(loader);

        Collection<SessionEventListener> eventListeners = new LinkedList<>(sessionEventListeners());
        org.eclipse.persistence.internal.jaxb.SessionEventListener eventListener = new org.eclipse.persistence.internal.jaxb.SessionEventListener();
        eventListener.setShouldValidateInstantiationPolicy(false);
        eventListeners.add(eventListener);
        XMLContext xmlContext = new XMLContext(proj, loader, eventListeners);
        ( (XMLLogin) xmlContext.getSession().getDatasourceLogin() ).setEqualNamespaceResolvers(false);
        Set<TypeMappingInfo> tmi = new HashSet<>();
        if ( this.delegate != null ) {
            tmi.addAll(this.delegate.getTypeMappingInfoToSchemaType().keySet());
        }
        for ( Type t : localTypes ) {
            TypeMappingInfo ti = new TypeMappingInfo();
            ti.setType(t);
            tmi.add(ti);
        }

        return new MOXYJAXBContextState(this.delegate, xmlContext, generator, tmi.toArray(new TypeMappingInfo[] {}), this.properties);
    }


    /**
     * @param loader
     * @param proj
     */
    void convertClassNames ( JaxbClassLoader loader, Project proj ) {
        // prevent convertClassNamesToClasses from reinitializing descriptors
        if ( USE_MODULAR_BINDING && this.delegate != null ) {
            List<ClassDescriptor> descs = proj.getOrderedDescriptors();
            proj.setOrderedDescriptors(Collections.EMPTY_LIST);
            proj.convertClassNamesToClasses(loader);
            for ( ClassDescriptor desc : descs ) {
                TypeInfo ti = this.delegate.getContextState().getGenerator().getAnnotationsProcessor().getTypeInfos().get(desc.getJavaClassName());
                if ( ti == null || ti.isTransient() || !desc.equals(ti.getDescriptor()) ) {
                    desc.convertClassNamesToClasses(loader);
                }
            }
            proj.setOrderedDescriptors(descs);
        }
        else {
            proj.convertClassNamesToClasses(loader);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.persistence.jaxb.JAXBContext.JAXBContextInput#sessionEventListeners()
     */
    @Override
    protected Collection<SessionEventListener> sessionEventListeners () {
        if ( this.sessionEventListeners == null ) {
            return Collections.EMPTY_LIST;
        }
        return this.sessionEventListeners;
    }
}
