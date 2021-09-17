/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure;


import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
@Named ( "structuralObjectConverter" )
@ApplicationScoped
public class StructuralObjectConverter implements Converter {

    private static final Logger log = Logger.getLogger(StructuralObjectConverter.class);

    @Inject
    private ServerServiceProvider ssp;


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
        try {
            return this.ssp.getService(StructuralObjectService.class).fetchById(UUID.fromString(value));
        }
        catch ( Exception e ) {
            log.error("Failed to load structural object:", e); //$NON-NLS-1$
            throw new ConverterException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext ctx, UIComponent component, Object object ) {
        if ( object instanceof StructuralObject ) {
            return ( (StructuralObject) object ).getId().toString();
        }

        return null;

    }

}
