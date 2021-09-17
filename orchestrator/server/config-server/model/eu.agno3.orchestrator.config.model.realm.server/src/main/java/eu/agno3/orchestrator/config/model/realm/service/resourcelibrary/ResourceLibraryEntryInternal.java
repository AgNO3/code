/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.resourcelibrary;


import java.io.File;

import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryEntry;


/**
 * @author mbechler
 *
 */
public class ResourceLibraryEntryInternal extends ResourceLibraryEntry {

    /**
     * 
     */
    private static final long serialVersionUID = -4669522119054977486L;
    private transient File file;


    /**
     * @return the file
     */
    public File getFile () {
        return this.file;
    }


    /**
     * @param file
     *            the file to set
     */
    public void setFile ( File file ) {
        this.file = file;
    }

}
