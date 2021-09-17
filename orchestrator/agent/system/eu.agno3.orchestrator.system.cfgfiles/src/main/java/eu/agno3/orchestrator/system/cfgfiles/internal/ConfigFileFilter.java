/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles.internal;


import java.io.File;
import java.io.FileFilter;


/**
 * @author mbechler
 *
 */
public class ConfigFileFilter implements FileFilter {

    /**
     * 
     */
    public static final String CONF_SUFFIX = ".conf"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see java.io.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept ( File f ) {

        if ( !f.isFile() || !f.canRead() ) {
            return false;
        }

        if ( !f.getName().endsWith(CONF_SUFFIX) ) {
            return false;
        }

        return true;
    }

}
