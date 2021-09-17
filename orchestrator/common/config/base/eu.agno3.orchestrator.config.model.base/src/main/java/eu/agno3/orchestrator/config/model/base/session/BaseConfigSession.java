/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2013 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.session;


import java.util.Collections;
import java.util.Set;

import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class BaseConfigSession implements ConfigSession {

    private static final Logger log = Logger.getLogger(BaseConfigSession.class);

    private boolean validating;


    /**
     * 
     * @param validating
     */
    public BaseConfigSession ( boolean validating ) {
        this.validating = validating;
    }


    /**
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }


    /**
     * 
     */
    public BaseConfigSession () {
        this(true);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.binding.UnmarshallingSession#isValidating()
     */
    @Override
    public boolean isValidating () {
        return this.validating;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.binding.UnmarshallingSession#getAdapters()
     */
    @Override
    public Set<XmlAdapter<?, ?>> getAdapters () {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.binding.UnmarshallingSession#getListener()
     */
    @Override
    public Listener getListener () {
        return null;
    }
}
