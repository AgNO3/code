/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal.file;


import java.io.File;

import org.apache.commons.io.filefilter.IOFileFilter;


/**
 * @author mbechler
 *
 */
public class LogFileFilter implements IOFileFilter {

    private String logFilePrefix;


    /**
     * @param logFileName
     */
    public LogFileFilter ( String logFileName ) {
        this.logFilePrefix = logFileName;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.io.filefilter.IOFileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept ( File f ) {
        return f.getName().startsWith(this.logFilePrefix) && f.getName().endsWith(".log"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.io.filefilter.IOFileFilter#accept(java.io.File, java.lang.String)
     */
    @Override
    public boolean accept ( File f, String name ) {
        return name.startsWith(this.logFilePrefix) && name.endsWith(".log"); //$NON-NLS-1$
    }

}
