/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.06.2015 by mbechler
 */
package eu.agno3.fileshare.mail.tpl;


import java.util.Arrays;
import java.util.List;

import eu.agno3.runtime.mail.tpl.MailTemplateBuilder;


/**
 * @author mbechler
 *
 */
public interface FileshareMailTemplateBuilder extends MailTemplateBuilder {

    /**
     * Available template ids
     */
    public static final List<String> TEMPLATE_IDS = Arrays.asList("mailShare", //$NON-NLS-1$
        "share", //$NON-NLS-1$
        "registration", //$NON-NLS-1$
        "invitation", //$NON-NLS-1$
        "entityExpiry", //$NON-NLS-1$
        "userExpiry", //$NON-NLS-1$
        "resetPassword", //$NON-NLS-1$
        "mail-footer" //$NON-NLS-1$
    );

}
