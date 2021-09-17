/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles.internal;


import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public class FactoryDirFilter implements FileFilter {

    /**
     * 
     */
    public static final Set<String> NON_FACTORY_DIRS = new HashSet<>(Arrays.asList("files", //$NON-NLS-1$
        "key", //$NON-NLS-1$
        "defaults")); //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see java.io.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept ( File f ) {

        if ( !f.isDirectory() || !f.canRead() ) {
            return false;
        }

        if ( NON_FACTORY_DIRS.contains(f.getName()) ) {
            return false;
        }

        return true;
    }

}
