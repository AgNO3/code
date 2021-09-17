/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2017 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import java.util.Objects;

import org.apache.jackrabbit.webdav.Status;
import org.w3c.dom.Element;


/**
 * @author mbechler
 *
 */
public class ExtendedStatus extends Status {

    private final Element error;


    /**
     * @param code
     */
    public ExtendedStatus ( int code ) {
        super(code);
        this.error = null;
    }


    /**
     * @param code
     * @param error
     * 
     */
    public ExtendedStatus ( int code, Element error ) {
        super(code);
        this.error = error;
    }


    /**
     * @param version
     * @param code
     * @param phrase
     */
    public ExtendedStatus ( String version, int code, String phrase ) {
        super(version, code, phrase);
        this.error = null;
    }


    /**
     * @param version
     * @param code
     * @param phrase
     * @param error
     */
    public ExtendedStatus ( String version, int code, String phrase, Element error ) {
        super(version, code, phrase);
        this.error = error;
    }


    /**
     * @return the error
     */
    public Element getError () {
        return this.error;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.getStatusCode();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( ! ( obj instanceof Status ) ) {
            return false;
        }

        Status st = (Status) obj;

        if ( this.getStatusCode() != st.getStatusCode() ) {
            return false;
        }

        if ( this.error != null && ! ( obj instanceof ExtendedStatus ) ) {
            return false;
        }
        else if ( obj instanceof ExtendedStatus ) {
            return Objects.equals(this.error, ( (ExtendedStatus) obj ).error);
        }
        return true;
    }
}
