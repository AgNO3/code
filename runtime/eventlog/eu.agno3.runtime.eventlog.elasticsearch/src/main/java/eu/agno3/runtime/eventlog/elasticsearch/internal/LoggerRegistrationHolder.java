/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.09.2016 by mbechler
 */
package eu.agno3.runtime.eventlog.elasticsearch.internal;


import java.io.IOException;

import org.osgi.service.cm.Configuration;


/**
 * @author mbechler
 *
 */
class LoggerRegistrationHolder {

    private Configuration backend;
    private Configuration reader;


    /**
     * @throws IOException
     * 
     */
    public synchronized void unregister () throws IOException {
        try {
            if ( this.reader != null ) {
                this.reader.delete();
                this.reader = null;
            }
        }
        finally {
            if ( this.backend != null ) {
                this.backend.delete();
                this.backend = null;
            }
        }
    }


    /**
     * @param backend
     *            the backend to set
     */
    public void setBackend ( Configuration backend ) {
        this.backend = backend;
    }


    /**
     * @param reader
     *            the reader to set
     */
    public void setReader ( Configuration reader ) {
        this.reader = reader;
    }
}
