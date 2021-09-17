/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby;


import java.io.PrintWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import eu.agno3.runtime.util.log.LogWriter;


/**
 * @author mbechler
 * 
 */
public final class DerbyLog extends LogWriter {

    private static final Logger log = Logger.getLogger(DerbyLog.class);

    /**
     * Derby logging wrapper
     */
    public static final PrintWriter LOG = createWriter();

    private boolean started;


    private DerbyLog () {
        super(log, Level.INFO);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.util.log.LogWriter#shouldIgnore(char[], int, int)
     */
    @Override
    protected boolean shouldIgnore ( char[] data, int off, int len ) {

        if ( !isStarted() ) {
            return true;
        }

        for ( int i = off; i < off + len; i++ ) {
            if ( data[ i ] != '-' ) {
                return false;
            }
        }
        return true;
    }


    /**
     * @return
     */
    private boolean isStarted () {
        if ( this.started ) {
            return true;
        }
        Bundle bundle = FrameworkUtil.getBundle(DerbyLog.class);
        if ( bundle == null ) {
            return false;
        }
        Bundle sysbundle = bundle.getBundleContext().getBundle(0);

        if ( sysbundle.adapt(FrameworkStartLevel.class).getStartLevel() >= 10 ) {
            this.started = true;
            return true;
        }

        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.util.log.LogWriter#processMessage(java.lang.String)
     */
    @Override
    protected String processMessage ( String line ) {
        return super.processMessage(line);
    }


    /**
     * @param logger
     * @param info
     * @return
     */
    private static PrintWriter createWriter () {
        return new PrintWriter(new DerbyLog());
    }
}
