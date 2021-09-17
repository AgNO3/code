/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.09.2014 by mbechler
 */
package eu.agno3.runtime.configloader.contribs;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class SingleFileConfigContribution extends AbstractSinglePropertiesConfigContribution {

    private static final Logger log = Logger.getLogger(SingleFileConfigContribution.class);

    private File source;


    /**
     * 
     * @param source
     * @param prio
     */
    public SingleFileConfigContribution ( File source, int prio ) {
        super(prio);
        this.source = source;
    }


    /**
     * @return the source
     */
    public File getSource () {
        return this.source;
    }


    @Override
    protected InputStream getInputStream () throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Loading configuration file " + this.source.getAbsolutePath()); //$NON-NLS-1$
        }
        return new FileInputStream(this.source);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof SingleFileConfigContribution ) {
            return this.source.equals( ( (SingleFileConfigContribution) obj ).source);
        }
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.source.hashCode();
    }
}
