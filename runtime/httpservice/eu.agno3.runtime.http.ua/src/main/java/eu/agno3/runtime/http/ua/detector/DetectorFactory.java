/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2015 by mbechler
 */
package eu.agno3.runtime.http.ua.detector;


import java.net.URL;

import org.apache.log4j.Logger;

import eu.agno3.runtime.http.ua.UADetector;
import eu.agno3.runtime.http.ua.internal.UADetectorImpl;

import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.datastore.DataStore;
import net.sf.uadetector.datastore.SimpleXmlDataStore;
import net.sf.uadetector.parser.UserAgentStringParserImpl;


/**
 * @author mbechler
 *
 */
public final class DetectorFactory {

    private static final Logger log = Logger.getLogger(DetectorFactory.class);

    private static DataStore DATASTORE;


    /**
     * 
     */
    private DetectorFactory () {}


    /**
     * @return an user agent parser
     */
    public static UserAgentStringParser getParser () {
        DataStore ds = getDataStore();

        if ( ds == null ) {
            return null;
        }

        return new UserAgentStringParserImpl<>(ds);
    }


    /**
     * @return an ua detector
     */
    public static UADetector getDetector () {
        return new UADetectorImpl(getParser());
    }


    /**
     * @return
     */
    private static DataStore getDataStore () {

        if ( DATASTORE != null ) {
            return DATASTORE;
        }

        URL uasData = DetectorFactory.class.getClassLoader().getResource("/uas.xml"); //$NON-NLS-1$
        URL uasVersion = UADetectorImpl.class.getClassLoader().getResource("/uas.version"); //$NON-NLS-1$

        if ( uasData == null || uasVersion == null ) {
            log.error("Failed to load user agent data"); //$NON-NLS-1$
            return null;
        }

        DataStore ds = new SimpleXmlDataStore(uasData, uasVersion);
        DATASTORE = ds;
        return ds;
    }
}
