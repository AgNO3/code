/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@Named ( "innerObjectEditorBean" )
@ViewScoped
public class InnerObjectEditorBean implements Serializable {

    private static final Logger log = Logger.getLogger(InnerObjectEditorBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 2011474211509571528L;

    private String objectPath;

    private String objectType;

    private boolean readOnly;


    /**
     * @return the objectPath
     */
    public String getObjectPath () {
        return this.objectPath;
    }


    /**
     * @param objectPath
     *            the objectPath to set
     */
    public void setObjectPath ( String objectPath ) {
        try {
            this.objectPath = URLDecoder.decode(objectPath, "UTF-8"); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            throw new FacesException(e);
        }
    }


    /**
     * @return the objectType
     */
    public String getObjectType () {
        return this.objectType;
    }


    /**
     * @param objectType
     *            the objectType to set
     */
    public void setObjectType ( String objectType ) {
        try {
            this.objectType = URLDecoder.decode(objectType, "UTF-8"); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            throw new FacesException(e);
        }
    }


    /**
     * @return the readOnly
     */
    public boolean getReadOnly () {
        return this.readOnly;
    }


    /**
     * @param readOnly
     *            the readOnly to set
     */
    public void setReadOnly ( boolean readOnly ) {
        this.readOnly = readOnly;
    }


    public String closeSave () {
        boolean dirty = false;
        String dirtyParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("dirty"); //$NON-NLS-1$
        if ( log.isDebugEnabled() ) {
            log.debug("Dirty param " + dirtyParam); //$NON-NLS-1$
        }
        if ( dirtyParam != null && Boolean.TRUE.toString().equals(dirtyParam) ) {
            dirty = true;
        }
        return DialogContext.closeDialog(dirty);
    }
}
