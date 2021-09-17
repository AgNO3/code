/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit;


import java.io.Serializable;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class PackageUpdate implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6761060371049904914L;

    private List<PackageId> old;
    private PackageId update;


    /**
     * @return the old
     */
    public List<PackageId> getOld () {
        return this.old;
    }


    /**
     * @param old
     *            the old to set
     */
    public void setOld ( List<PackageId> old ) {
        this.old = old;
    }


    /**
     * @return the package that is updated by this
     */
    public PackageId getUpdate () {
        return this.update;
    }


    /**
     * @param updates
     *            the update to set
     */
    public void setUpdate ( PackageId updates ) {
        this.update = updates;
    }
}
