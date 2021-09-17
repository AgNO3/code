/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.test;


import java.net.URLEncoder;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "configTestUtil" )
public class ConfigTestUtil {

    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ConfigTestUtil.class);


    public String makeTestLink ( OuterWrapper<?> ow ) {
        try {
            UUID selectedObjectId = ow.getContext().getAnchor().getId();

            return String.format(
                "%s?path=%s&object=%s&cid=%s&type=%s", //$NON-NLS-1$
                ow.getContext().getTestTemplate(),
                URLEncoder.encode(ow.getEditor().getAbsolutePath(), UTF_8),
                selectedObjectId,
                URLEncoder.encode(ow.getContext().getConversation().getId(), UTF_8),
                URLEncoder.encode(ow.getType(), UTF_8));
        }
        catch ( Exception e ) {
            log.error("Failed to produce test URL", e); //$NON-NLS-1$
            return null;
        }
    }
}
