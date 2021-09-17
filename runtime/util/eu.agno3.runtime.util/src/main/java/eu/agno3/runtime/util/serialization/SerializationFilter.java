/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.runtime.util.serialization;


import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class SerializationFilter {

    private static final Logger log = Logger.getLogger(SerializationFilter.class);


    /**
     * 
     */
    private SerializationFilter () {}

    private static final Set<String> WHITELIST_PACKAGES = new HashSet<>(Arrays.asList(
        "java.rmi", //$NON-NLS-1$
        "java.lang", //$NON-NLS-1$
        "java.util", //$NON-NLS-1$
        "javax.management", //$NON-NLS-1$
        "javax.security.auth", //$NON-NLS-1$
        "javax.management.openmbean", //$NON-NLS-1$
        "org.joda.time", //$NON-NLS-1$
        "org.joda.time.base", //$NON-NLS-1$
        "org.joda.time.chrono", //$NON-NLS-1$
        "org.joda.time.field" //$NON-NLS-1$
    ));

    private static final Set<String> WHITELIST_CLASSES = new HashSet<>();

    private static final Set<String> BLACKLIST_CLASSES = new HashSet<>();

    private static final Set<Class<?>> SAFE_ROOTS = new HashSet<>(Arrays.asList(Throwable.class));


    /**
     * 
     * @param cl
     */
    public static void whitelistClass ( Class<?> cl ) {
        if ( cl == null ) {
            return;
        }
        WHITELIST_CLASSES.add(cl.getName());
    }


    /**
     * 
     * @param pkg
     */
    public static void whitelistPackage ( String pkg ) {
        WHITELIST_PACKAGES.add(pkg);
    }


    /**
     * @param name
     * @param classLoader
     * @return loaded clazz
     * @throws ClassNotFoundException
     * @throws UnsafeSerializableException
     */
    public static Class<?> checkAndLoadClass ( String name, ClassLoader classLoader ) throws UnsafeSerializableException, ClassNotFoundException {
        if ( BLACKLIST_CLASSES.contains(name) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Rejected blacklisted class load for deserialization " + name); //$NON-NLS-1$
            }
            throw new UnsafeSerializableException("Rejected unsafe class " + name); //$NON-NLS-1$
        }

        String toCheck = name;
        int typePos = 0;

        while ( name.charAt(typePos) == '[' ) {
            typePos++;
        }

        if ( typePos > 0 && name.charAt(typePos) == 'L' ) {
            toCheck = name.substring(typePos + 1);
        }
        else if ( typePos > 0 && typePos == name.length() - 1 ) {
            // primitive array
            return loadClass(name, classLoader);
        }

        int sepPos = toCheck.lastIndexOf('.');
        if ( sepPos < 0 && Character.isLowerCase(toCheck.charAt(0)) ) {
            // primitive
            return loadClass(name, classLoader);
        }
        else if ( sepPos < 0 ) {
            throw new UnsafeSerializableException("Invalid class: " + name); //$NON-NLS-1$
        }

        return checkAndLoadWhitelisted(name, classLoader, sepPos);
    }


    /**
     * @param name
     * @param classLoader
     * @param sepPos
     * @return
     * @throws ClassNotFoundException
     * @throws UnsafeSerializableException
     */
    protected static Class<?> checkAndLoadWhitelisted ( String name, ClassLoader classLoader, int sepPos )
            throws ClassNotFoundException, UnsafeSerializableException {
        String pkg = name.substring(0, sepPos);
        if ( !WHITELIST_PACKAGES.contains(pkg) ) {
            return checkClass(loadClass(name, classLoader));
        }

        return loadClass(name, classLoader);
    }


    /**
     * Checks whether a class is whitelisted
     * 
     * @param cl
     * @return the class
     * @throws UnsafeSerializableException
     */
    public static Class<?> checkWhitelist ( Class<?> cl ) throws UnsafeSerializableException {
        if ( WHITELIST_PACKAGES.contains(cl.getPackage().getName()) ) {
            return cl;
        }

        return checkClass(cl);
    }


    /**
     * @param name
     * @param classLoader
     * @return
     * @throws UnsafeSerializableException
     */
    static Class<?> checkClass ( Class<?> cl ) throws UnsafeSerializableException {
        Class<?> safe = checkSafeImpl(cl);
        if ( safe != null ) {
            return safe;
        }

        String msg = "Rejected unsafe class " + cl.getName(); //$NON-NLS-1$
        if ( log.isDebugEnabled() ) {
            log.debug(msg);
        }
        throw new UnsafeSerializableException(msg);
    }


    private static Class<?> loadClass ( String name, ClassLoader classLoader ) throws ClassNotFoundException {
        return Class.forName(name, false, classLoader);
    }


    /**
     * @param cl
     * @param cur
     * @return
     * @throws UnsafeSerializableException
     */
    private static Class<?> checkSafeImpl ( Class<?> cl ) throws UnsafeSerializableException {
        Class<?> cur = cl;

        if ( SafeSerializable.class.isAssignableFrom(cl) || SafeExternalizable.class.isAssignableFrom(cl) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Class is marked safe via interface " + cl.getName()); //$NON-NLS-1$
            }
            return cl;
        }

        boolean serializable = Serializable.class.isAssignableFrom(cl);
        boolean externalizable = Externalizable.class.isAssignableFrom(cl);
        if ( !serializable ) {
            return cl;
        }

        while ( cur != null ) {
            SafeSerialization annotation = cur.getAnnotation(SafeSerialization.class);
            if ( annotation != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Class is marked safe via annotation " + cl.getName()); //$NON-NLS-1$
                }
                return cl;
            }
            cur = cur.getSuperclass();
        }

        cur = cl;
        while ( cur != null ) {
            if ( SAFE_ROOTS.contains(cur) ) {
                return cl;
            }
            checkMethodNotExists(cur, "readObject", ObjectInputStream.class); //$NON-NLS-1$
            checkMethodNotExists(cur, "readResolve", ObjectInputStream.class); //$NON-NLS-1$
            if ( externalizable ) {
                checkMethodNotExists(cur, "readExternal", ObjectInput.class); //$NON-NLS-1$
            }

            cur = cur.getSuperclass();
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Class does not override deserialization " + cl.getName()); //$NON-NLS-1$
        }
        return cl;
    }


    /**
     * @param cur
     * @param name
     * @throws UnsafeSerializableException
     */
    private static void checkMethodNotExists ( Class<?> cur, String name, Class<?>... args ) throws UnsafeSerializableException {
        try {
            cur.getMethod(name, args);
        }
        catch (
            NoSuchMethodException |
            SecurityException e ) {
            log.trace("Expected exception", e); //$NON-NLS-1$
            // expected
            return;
        }
        throw new UnsafeSerializableException("Unsafe class " + cur.getName()); //$NON-NLS-1$
    }
}
