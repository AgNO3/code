/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file;


import java.nio.file.Path;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;


/**
 * @author mbechler
 * 
 */
public final class PrefixUtil {

    private PrefixUtil () {}


    /**
     * @param ctx
     * @param p
     * @return the path below the prefix
     */
    public static Path resolvePrefix ( Context ctx, Path p ) {
        return resolvePrefix(ctx.getConfig(), p);
    }


    /**
     * @param cfg
     * @param p
     * @return the path below the prefix
     */
    public static Path resolvePrefix ( ExecutionConfigProperties cfg, Path p ) {
        if ( !p.isAbsolute() ) {
            throw new IllegalArgumentException("Must be an absolute path"); //$NON-NLS-1$
        }

        Path resolved = cfg.getPrefix().resolve(p.toString().substring(1));

        if ( !resolved.startsWith(cfg.getPrefix()) ) {
            throw new IllegalArgumentException("Path tried to escape prefix"); //$NON-NLS-1$
        }

        return resolved;
    }
}
