/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage;


import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( StorageInformation.class )
public class StorageInformationImpl implements StorageInformation {

    /**
     * 
     */
    private static final long serialVersionUID = 5349965602310444972L;
    private Set<Drive> drives;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.StorageInformation#getDrives()
     */
    @Override
    public Set<Drive> getDrives () {
        return this.drives;
    }


    /**
     * @param drives
     *            the drives to set
     */
    public void setDrives ( Set<Drive> drives ) {
        this.drives = drives;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.StorageInformation#getDriveById(java.lang.String)
     */
    @Override
    public Drive getDriveById ( String id ) {
        if ( StringUtils.isBlank(id) ) {
            return null;
        }

        for ( Drive d : this.drives ) {
            if ( id.equals(d.getId()) ) {
                return d;
            }
        }

        return null;
    }
}
