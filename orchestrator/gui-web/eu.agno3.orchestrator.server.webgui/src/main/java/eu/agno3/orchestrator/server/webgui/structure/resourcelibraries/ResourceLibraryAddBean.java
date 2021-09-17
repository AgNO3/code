/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.config.ConfigServiceProvider;
import eu.agno3.orchestrator.server.webgui.config.TemplateCacheBean;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@Named ( "resourceLibraryAddBean" )
@ViewScoped
public class ResourceLibraryAddBean implements Serializable {

    /**
     * 
     */
    private static final String RAW_TYPE = "raw"; //$NON-NLS-1$

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(ResourceLibraryAddBean.class);
    private static final long serialVersionUID = -7381748672800726546L;
    private String name;
    private String type;

    private List<ResourceLibrary> usableParents;

    private UUID parentId;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private CoreServiceProvider csp;

    @Inject
    private ConfigServiceProvider cfgsp;

    @Inject
    private StructureViewContextBean viewContext;

    @Inject
    private TemplateCacheBean templateCache;


    public String add () {
        try {
            this.ssp.getService(ResourceLibraryService.class)
                    .create(this.viewContext.getSelectedObject(), this.parentId, this.name, this.type, false);
            this.templateCache.flush();
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }


    /**
     * @return the name
     */
    public String getName () {
        return this.name;
    }


    /**
     * @param name
     *            the name to set
     */
    public void setName ( String name ) {
        this.name = name;
    }


    /**
     * @return the type
     */
    public String getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( String type ) {
        this.type = type;
    }


    /**
     * @return the parent
     */
    public UUID getParent () {
        return this.parentId;
    }


    /**
     * @param parent
     *            the parent to set
     */
    public void setParent ( UUID parent ) {
        this.parentId = parent;
    }


    /**
     * @return the usableParents
     */
    public List<ResourceLibrary> getUsableParents () {
        if ( this.usableParents == null ) {
            fetchUsableLibraries(getType());
        }
        return this.usableParents;
    }


    /**
     * @param string
     * 
     */
    private void fetchUsableLibraries ( String t ) {
        try {
            this.usableParents = this.ssp.getService(ResourceLibraryService.class)
                    .getUsableResourceLibraries(this.viewContext.getSelectedObject(), t, true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            this.usableParents = Collections.EMPTY_LIST;
        }
    }


    public void typeChanged ( SelectEvent ev ) {
        fetchUsableLibraries((String) ev.getObject());
    }


    /**
     * @param usableParents
     *            the usableParents to set
     */
    public void setUsableParents ( List<ResourceLibrary> usableParents ) {
        this.usableParents = usableParents;
    }


    /**
     * 
     * @param libraryType
     * @return the localized library name
     */
    public String translateResourceLibraryType ( String libraryType ) {
        if ( RAW_TYPE.equals(libraryType) ) {
            return GuiMessages.get("resourceLibrary.raw"); //$NON-NLS-1$
        }
        try {
            ResourceLibraryDescriptor descriptor = this.cfgsp.getResourceLibraryRegistry().getDescriptor(libraryType);

            if ( descriptor == null || descriptor.getLocalizationBase() == null ) {
                return libraryType;
            }

            ResourceBundle bundle = this.csp.getLocalizationService().getBundle(
                descriptor.getLocalizationBase(),
                FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                descriptor.getClass().getClassLoader());

            return bundle.getString("resourceLibrary." + libraryType); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            log.warn("Failed to get resource library descriptor for " + libraryType, e); //$NON-NLS-1$
            return libraryType;
        }
    }


    /**
     * 
     * @return library types that are usable
     */
    public List<String> getUsableTypes () {
        return new LinkedList<>(this.cfgsp.getResourceLibraryRegistry().getResourceLibraryTypes());
    }
}
