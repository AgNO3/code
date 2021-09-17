/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.internal;


import javax.xml.stream.Location;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class LoggingXMLReporter implements XMLReporter {

    private final Logger log;


    /**
     * @param log
     * 
     */
    public LoggingXMLReporter ( Logger log ) {
        this.log = log;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.stream.XMLReporter#report(java.lang.String, java.lang.String, java.lang.Object,
     *      javax.xml.stream.Location)
     */
    @Override
    public void report ( String message, String errorType, Object relatedInformation, Location location ) throws XMLStreamException {
        this.log.warn(String.format("%s: %s @ %s", errorType, message, location.toString())); //$NON-NLS-1$
    }
}
