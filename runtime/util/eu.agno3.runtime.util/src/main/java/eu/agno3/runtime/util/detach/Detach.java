/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2015 by mbechler
 */
package eu.agno3.runtime.util.detach;


import java.lang.reflect.Field;
import java.security.AccessControlContext;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class Detach {

    private static final AccessControlContext EMPTY_CONTEXT = new AccessControlContext(new ProtectionDomain[] {});
    private static final Logger log = Logger.getLogger(Detach.class);

    private static Field INHERITABLE_THREAD_LOCALS;


    static {

        try {
            INHERITABLE_THREAD_LOCALS = Thread.class.getDeclaredField("inheritableThreadLocals"); //$NON-NLS-1$
            INHERITABLE_THREAD_LOCALS.setAccessible(true);
        }
        catch (
            NoSuchFieldException |
            SecurityException e ) {
            log.error("Failed to get inhertiableThreadLocals field", e); //$NON-NLS-1$
        }
    }


    /**
     * @param toRun
     * @return return value
     * @throws Exception
     */
    public static <T> T runDetached ( final DetachedRunnable<T> toRun ) throws Exception {
        Thread currentThread = Thread.currentThread();
        ClassLoader oldTCCL = currentThread.getContextClassLoader();
        Object savedThreadLocals = null;
        Field threadLocalField = INHERITABLE_THREAD_LOCALS;
        if ( threadLocalField != null ) {
            savedThreadLocals = threadLocalField.get(currentThread);
        }

        try {
            currentThread.setContextClassLoader(null);
            if ( threadLocalField != null ) {
                threadLocalField.set(currentThread, null);
            }

            return Subject.doAsPrivileged(null, new PrivilegedExceptionAction<T>() {

                @Override
                public T run () throws Exception {
                    return toRun.run();
                }

            }, EMPTY_CONTEXT);

        }
        catch ( PrivilegedActionException e ) {
            if ( e.getCause() instanceof Exception ) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
        finally {
            if ( threadLocalField != null ) {
                threadLocalField.set(currentThread, savedThreadLocals);
            }
            currentThread.setContextClassLoader(oldTCCL);
        }
    }
}
