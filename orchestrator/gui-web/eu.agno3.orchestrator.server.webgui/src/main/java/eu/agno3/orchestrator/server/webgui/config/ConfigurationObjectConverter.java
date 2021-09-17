/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.util.UUID;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;


/**
 * @author mbechler
 * 
 */
@Named ( "configurationObjectConverter" )
@ApplicationScoped
public class ConfigurationObjectConverter implements Converter {

    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject ( FacesContext ctx, UIComponent component, String value ) {
        if ( value == null ) {
            return null;
        }

        String[] tokens = value.split(Pattern.quote("|"), -1); //$NON-NLS-1$

        if ( tokens.length != 6 ) {
            throw new ConverterException("Illegal object reference " + value); //$NON-NLS-1$
        }

        String objType = tokens[ 0 ];
        String idSpec = tokens[ 1 ];
        String versionSpec = tokens[ 2 ];
        String revisionSpec = tokens[ 3 ];
        String localName = tokens[ 4 ];
        String displayName = tokens[ 5 ];

        ConfigurationObjectReference ref = new ConfigurationObjectReference();

        ref.setObjectTypeName(objType);

        if ( !idSpec.isEmpty() ) {
            UUID fromString = UUID.fromString(idSpec);

            if ( fromString != null ) {
                ref.setId(fromString);
            }
        }

        if ( !versionSpec.isEmpty() ) {
            ref.setVersion(Long.parseLong(versionSpec));
        }

        if ( !revisionSpec.isEmpty() ) {
            ref.setRevision(Long.valueOf(revisionSpec));
        }

        if ( !localName.isEmpty() ) {
            ref.setName(localName);
        }

        if ( !displayName.isEmpty() ) {
            ref.setDisplayName(displayName);
        }

        return ref;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext ctx, UIComponent component, Object object ) {
        if ( ! ( object instanceof ConfigurationObject ) ) {
            return null;
        }

        ConfigurationObjectReference ref;

        if ( object instanceof ConfigurationObjectReference ) {
            ref = (ConfigurationObjectReference) object;
        }
        else {
            ref = new ConfigurationObjectReference((ConfigurationObject) object);
        }

        String[] parts = new String[] {
            ref.getObjectTypeName(), ref.getId() != null ? ref.getId().toString() : StringUtils.EMPTY,
            ( ref.getVersion() != null && ref.getVersion() != 0 ) ? String.valueOf(ref.getVersion()) : StringUtils.EMPTY,
            ref.getRevision() != null ? String.valueOf(ref.getRevision()) : StringUtils.EMPTY,
            ref.getName() != null ? ref.getName() : StringUtils.EMPTY, ref.getDisplayName() != null ? ref.getDisplayName() : StringUtils.EMPTY
        };

        return StringUtils.join(parts, '|');

    }
}
