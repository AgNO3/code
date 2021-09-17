/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.09.2016 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import org.apache.log4j.Logger;
import org.eclipse.persistence.core.sessions.CoreProject;
import org.eclipse.persistence.internal.oxm.mappings.Descriptor;
import org.eclipse.persistence.jaxb.compiler.MappingsGenerator;
import org.eclipse.persistence.jaxb.compiler.NamespaceInfo;
import org.eclipse.persistence.jaxb.compiler.TypeInfo;
import org.eclipse.persistence.jaxb.javamodel.Helper;
import org.eclipse.persistence.jaxb.javamodel.JavaClass;
import org.eclipse.persistence.oxm.XMLDescriptor;


/**
 * @author mbechler
 *
 */
public class ModularGenerator extends MappingsGenerator {

    private static final Logger log = Logger.getLogger(ModularGenerator.class);
    private MOXYJAXBContext delegate;


    /**
     * @param helper
     * @param delegate
     */
    public ModularGenerator ( Helper helper, MOXYJAXBContext delegate ) {
        super(helper);
        this.delegate = delegate;
    }


    @Override
    public void generateDescriptor ( JavaClass javaClass, CoreProject project ) {
        TypeInfo ti = this.delegate.getContextState().getGenerator().getAnnotationsProcessor().getTypeInfos().get(javaClass.getQualifiedName());
        if ( ti == null || ti.isTransient() || ! ( ti.getDescriptor() instanceof XMLDescriptor ) ) {
            super.generateDescriptor(javaClass, project);
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug("Adding existing descriptor for " + javaClass.getQualifiedName()); //$NON-NLS-1$
            }
            project.addDescriptor((XMLDescriptor) ti.getDescriptor());
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.persistence.jaxb.compiler.MappingsGenerator#generateMappings(org.eclipse.persistence.jaxb.compiler.TypeInfo,
     *      org.eclipse.persistence.internal.oxm.mappings.Descriptor, org.eclipse.persistence.jaxb.javamodel.JavaClass,
     *      org.eclipse.persistence.jaxb.compiler.NamespaceInfo)
     */
    @Override
    public void generateMappings ( TypeInfo info, Descriptor descriptor, JavaClass descriptorJavaClass, NamespaceInfo namespaceInfo ) {
        if ( this.delegate.getContextState().getGenerator().getAnnotationsProcessor().getTypeInfos()
                .containsKey(descriptorJavaClass.getQualifiedName()) ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Not generating mappings for existing descriptor for " + descriptorJavaClass.getQualifiedName()); //$NON-NLS-1$
            }
            return;
        }
        super.generateMappings(info, descriptor, descriptorJavaClass, namespaceInfo);
    }

}
