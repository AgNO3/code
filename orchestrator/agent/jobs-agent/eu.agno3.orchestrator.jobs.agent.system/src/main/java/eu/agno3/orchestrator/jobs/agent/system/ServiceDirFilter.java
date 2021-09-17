/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import java.io.File;
import java.io.FileFilter;


/**
 * @author mbechler
 *
 */
public class ServiceDirFilter implements FileFilter {

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

        return !f.getName().startsWith("."); //$NON-NLS-1$
    }

}
