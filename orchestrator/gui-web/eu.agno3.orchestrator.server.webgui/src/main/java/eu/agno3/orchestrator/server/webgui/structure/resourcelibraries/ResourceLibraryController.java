/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ops4j.pax.cdi.api.OsgiService;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryRegistry;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.InstanceStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "resourceLibraryController" )
public class ResourceLibraryController {

    private static final Logger log = Logger.getLogger(ResourceLibraryController.class);

    private static final String CS = "UTF-8"; //$NON-NLS-1$

    @Inject
    private StructureViewContextBean viewContext;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private ResourceLibraryRegistry resourceLibraryRegistry;

    @Inject
    private StructureCacheBean structureCache;

    @Inject
    private StructureViewContextBean structureContextBean;

    @Inject
    private InstanceStateTracker instanceState;


    public String finishedReturnDialog ( SelectEvent ev ) {
        return finishedReturn();
    }


    public String finishedReturn () {

        try {
            InstanceStructuralObject instance = this.structureContextBean.getSelectedInstance();

            if ( instance == null && this.structureContextBean.getSelectedAnchor() instanceof InstanceStructuralObject ) {
                instance = (InstanceStructuralObject) this.structureContextBean.getSelectedAnchor();
            }

            if ( instance != null ) {
                log.debug("Triggering refresh"); //$NON-NLS-1$
                this.instanceState.forceRefresh(instance);
                RequestContext.getCurrentInstance().update("menuForm"); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String editLibrary ( ResourceLibrary obj ) throws UnsupportedEncodingException {
        if ( obj == null ) {
            return null;
        }

        String type = URLEncoder.encode(obj.getType(), CS);

        try {
            ResourceLibraryDescriptor descriptor = this.resourceLibraryRegistry.getDescriptor(type);
            type = descriptor.getEditorType();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }

        if ( this.viewContext.getSelectedAnchorId() != null ) {
            return String.format(
                "/resourceLibrary/edit/%s.xhtml?faces-redirect=true&library=%s&at=%s&anchor=%s", //$NON-NLS-1$
                type,
                obj.getId(),
                this.viewContext.getSelectedObjectId(),
                this.viewContext.getSelectedAnchorId());
        }
        return String.format(
            "/resourceLibrary/edit/%s.xhtml?faces-redirect=true&library=%s&at=%s", //$NON-NLS-1$
            type,
            obj.getId(),
            this.viewContext.getSelectedObjectId());
    }


    public String editLibraryDialog ( UUID anchor, UUID libraryId, String descType ) {
        String type = descType;
        try {
            ResourceLibraryDescriptor descriptor = this.resourceLibraryRegistry.getDescriptor(type);
            type = descriptor.getEditorType();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }

        return String.format(
            "/resourceLibrary/edit/%s.dialog.xhtml?faces-redirect=true&library=%s&at=%s", //$NON-NLS-1$
            type,
            libraryId,
            anchor);
    }


    public String overview () {
        return String.format("/structure/resourceLibraries.xhtml?faces-redirect=true&anchor=%s", this.viewContext.getSelectedObjectId()); //$NON-NLS-1$
    }


    public String makeCreateOrEditArguments ( String type, String name ) throws UnsupportedEncodingException {

        StructuralObject at;
        UUID anchor;
        try {
            at = this.viewContext.getSelectedObject();
            anchor = this.viewContext.getSelectedAnchorId();
            if ( at instanceof ServiceStructuralObject ) {
                StructuralObject parent = this.structureCache.getParentFor(at);
                if ( parent instanceof InstanceStructuralObject ) {
                    at = parent;
                    anchor = null;
                }
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }

        if ( at == null ) {
            return null;
        }

        return String.format(
            "anchor=%s&at=%s&createType=%s&createName=%s", //$NON-NLS-1$
            anchor != null ? anchor : StringUtils.EMPTY,
            at.getId(),
            URLEncoder.encode(type, CS),
            URLEncoder.encode(name, CS));
    }
}
