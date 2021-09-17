/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.directory;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import eu.agno3.fileshare.model.Group;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "directoryCreateBean" )
public class DirectoryCreateBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8712547776812477741L;

    private String directoryName;

    private boolean inGroupRoot;

    private Group group;


    /**
     * @return the directoryName
     */
    public String getDirectoryName () {
        return this.directoryName;
    }


    /**
     * @param directoryName
     *            the directoryName to set
     */
    public void setDirectoryName ( String directoryName ) {
        this.directoryName = directoryName;
    }


    /**
     * @return the inGroupRoot
     */
    public boolean getInGroupRoot () {
        return this.inGroupRoot;
    }


    /**
     * @param inGroupRoot
     *            the inGroupRoot to set
     */
    public void setInGroupRoot ( boolean inGroupRoot ) {
        this.inGroupRoot = inGroupRoot;
    }


    /**
     * @return the group
     */
    public Group getGroup () {
        return this.group;
    }


    /**
     * @param group
     *            the group to set
     */
    public void setGroup ( Group group ) {
        this.group = group;
    }
}
