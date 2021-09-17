/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2015 by mbechler
 */
package eu.agno3.runtime.xml.schema.internal;


import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.xml.schema.SchemaValidationConfig;
import eu.agno3.runtime.xml.schema.SchemaValidationLevel;


/**
 * @author mbechler
 *
 */
@Component ( service = SchemaValidationConfig.class, configurationPid = "xml.schema" )
public class SchemaValidationConfigImpl implements SchemaValidationConfig {

    private static final Logger log = Logger.getLogger(SchemaValidationConfigImpl.class);

    private SchemaValidationLevel level;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

        String levelStr = ConfigUtil.parseString(ctx.getProperties(), "schemaValidation", SchemaValidationLevel.VALIDATE.name()); //$NON-NLS-1$
        try {
            this.level = SchemaValidationLevel.valueOf(levelStr);
        }
        catch ( IllegalArgumentException e ) {
            log.warn("Invalid schema validation level", e); //$NON-NLS-1$
            this.level = SchemaValidationLevel.VALIDATE;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.xml.schema.SchemaValidationConfig#getLevel()
     */
    @Override
    public SchemaValidationLevel getLevel () {
        return this.level;
    }

}
