/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "resourceLibraryDeleteBean" )
public class ResourceLibraryDeleteBean implements Serializable {

    private static final Logger log = Logger.getLogger(ResourceLibraryDeleteBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 4639189888633891753L;
    private String selectedPath;


    /**
     * @return the selectedPath
     */
    public String getSelectedPath () {
        return this.selectedPath;
    }


    /**
     * @param selectedPath
     *            the selectedPath to set
     */
    public void setSelectedPath ( String selectedPath ) {
        try {
            this.selectedPath = URLDecoder.decode(selectedPath, "UTF-8"); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            log.error("Unsupported encoding", e); //$NON-NLS-1$
        }
    }
}
