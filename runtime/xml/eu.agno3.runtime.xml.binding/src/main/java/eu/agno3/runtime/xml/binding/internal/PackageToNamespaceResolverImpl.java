/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import javax.xml.bind.annotation.XmlSchema;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.xml.schema.PackageToNamespaceResolver;


/**
 * @author mbechler
 * 
 */
@Component ( service = PackageToNamespaceResolver.class )
public class PackageToNamespaceResolverImpl implements PackageToNamespaceResolver {

    /**
     * 
     * @return the namespace for the given package
     */
    @Override
    public String getNamespaceForPackage ( Package pack ) {

        XmlSchema xmlSchema = pack.getAnnotation(XmlSchema.class);

        if ( xmlSchema == null ) {
            throw new IllegalArgumentException(String.format("Package %s is missing the @XmlSchema annotation", pack.getName())); //$NON-NLS-1$
        }

        return xmlSchema.namespace();
    }

}
