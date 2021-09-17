/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.copy;


import eu.agno3.orchestrator.system.base.units.file.AbstractFileSourceDestConfigurator;


/**
 * @author mbechler
 */
public class CopyConfigurator extends AbstractFileSourceDestConfigurator<Copy, CopyConfigurator> {

    /**
     * @param unit
     */
    public CopyConfigurator ( Copy unit ) {
        super(unit);
    }


    /**
     * Disable copying of file attributes
     * 
     * @return this configurator
     */
    public CopyConfigurator doNotCopyAttributes () {
        this.getExecutionUnit().setCopyAttributes(false);
        return this.self();
    }


    /**
     * Enable following source symlinks.
     * 
     * General note: If target is a symlink and replaceTarget is enabled,
     * the symbolic link itself will be replaced and not it's target.
     * 
     * @return this configurator
     */
    public CopyConfigurator followSymlinks () {
        this.getExecutionUnit().setFollowSymlinks(true);
        return this.self();
    }


    /**
     * Set the copy buffer size
     * 
     * @param bufSize
     *            copy buffer size in bytes
     * @return this configurator
     */
    public CopyConfigurator bufSize ( int bufSize ) {
        this.getExecutionUnit().setBufSize(bufSize);
        return this;
    }

}
