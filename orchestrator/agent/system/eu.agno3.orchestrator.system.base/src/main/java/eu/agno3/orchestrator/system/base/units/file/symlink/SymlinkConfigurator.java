/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.symlink;


import java.io.File;
import java.nio.file.Path;

import eu.agno3.orchestrator.system.base.units.file.AbstractFileConfigurator;


/**
 * @author mbechler
 * 
 */
public class SymlinkConfigurator extends AbstractFileConfigurator<Symlink, SymlinkConfigurator> {

    /**
     * @param unit
     */
    protected SymlinkConfigurator ( Symlink unit ) {
        super(unit);
    }


    /**
     * 
     * @param p
     * @return this configurator
     */
    public SymlinkConfigurator source ( Path p ) {
        getExecutionUnit().setSource(p);
        return this.self();
    }


    /**
     * 
     * @param f
     * @return this configurator
     */
    public SymlinkConfigurator source ( File f ) {
        return source(f.toPath());
    }
}
