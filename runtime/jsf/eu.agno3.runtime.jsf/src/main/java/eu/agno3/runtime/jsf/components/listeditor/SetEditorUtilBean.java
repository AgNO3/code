/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.runtime.jsf.components.listeditor;


import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "setEditorUtilBean" )
public class SetEditorUtilBean {

    private static final Logger log = Logger.getLogger(SetEditorUtilBean.class);


    /**
     * @return a comparator for strings
     */
    public Comparator<String> getStringComparator () {
        return new StringComparator();
    }


    /**
     * 
     * @return an empty string
     */
    public String makeEmptyString () {
        return StringUtils.EMPTY;
    }


    /**
     * 
     * @return an URI comparator
     */
    public Comparator<URI> getUriComparator () {

        return new URIComparator();
    }


    /**
     * 
     * @return null
     */
    public URI makeEmptyURI () {
        try {
            return new URI(""); //$NON-NLS-1$
        }
        catch ( URISyntaxException e ) {
            log.warn("Failed to create empty URI", e); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * @author mbechler
     *
     */
    private static final class URIComparator implements Comparator<URI>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -8142742112427346374L;


        /**
         * 
         */
        public URIComparator () {}


        @Override
        public int compare ( URI o1, URI o2 ) {
            if ( o1 == null && o2 == null ) {
                return 0;
            }
            else if ( o1 == null ) {
                return -1;
            }
            else if ( o2 == null ) {
                return 1;
            }
            return o1.compareTo(o2);
        }
    }

    /**
     * @author mbechler
     *
     */
    private static final class StringComparator implements Comparator<String>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 5786165605689891104L;


        /**
         * 
         */
        public StringComparator () {}


        @Override
        public int compare ( String o1, String o2 ) {
            if ( o1 == null && o2 == null ) {
                return 0;
            }
            else if ( o1 == null ) {
                return -1;
            }
            else if ( o2 == null ) {
                return 1;
            }
            return o1.compareTo(o2);
        }
    }
}
