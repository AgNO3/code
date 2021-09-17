/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.uri;


import java.net.URI;

import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * @author mbechler
 *
 */
public class XmlURIAdapter extends XmlAdapter<String, URI> {

    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( URI u ) {
        if ( u == null ) {
            return null;
        }
        return u.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public URI unmarshal ( String s ) {
        if ( s == null ) {
            return null;
        }
        return URI.create(s);
    }

}
