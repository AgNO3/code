/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.09.2015 by mbechler
 */
package eu.agno3.runtime.logging.layouts;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ops4j.pax.logging.spi.PaxLayout;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;


/**
 * @author mbechler
 *
 */
public class WithExceptionLayout implements PaxLayout {

    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.logging.spi.PaxLayout#doLayout(org.ops4j.pax.logging.spi.PaxLoggingEvent)
     */
    @Override
    public String doLayout ( PaxLoggingEvent ev ) {

        if ( ev.getThrowableStrRep() == null || ev.getThrowableStrRep().length == 0 ) {
            return ev.getMessage();
        }

        List<String> exceptions = new LinkedList<>();
        for ( String line : ev.getThrowableStrRep() ) {
            if ( line.length() > 1 && ( line.charAt(0) != ' ' ) && ( line.charAt(0) != '\t' ) ) {
                exceptions.add(line);
            }
        }
        return ev.getMessage() + StringUtils.SPACE + StringUtils.join(exceptions, "/"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.logging.spi.PaxLayout#getContentType()
     */
    @Override
    public String getContentType () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.logging.spi.PaxLayout#getFooter()
     */
    @Override
    public String getFooter () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.logging.spi.PaxLayout#getHeader()
     */
    @Override
    public String getHeader () {
        return null;
    }

}
