/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 2, 2017 by mbechler
 */
package eu.agno3.runtime.redis.session.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.session.SessionData;


/**
 * @author mbechler
 *
 */
public final class SessionDiffUtil {

    private static final Logger log = Logger.getLogger(SessionDiffUtil.class);


    /**
     * 
     */
    private SessionDiffUtil () {}


    /**
     * @param val
     * @return
     */
    static byte[] dump ( Object val ) {
        try ( ByteArrayOutputStream out = new ByteArrayOutputStream();
              ObjectOutputStream oos = new ObjectOutputStream(out) ) {
            oos.writeObject(val);
            oos.close();
            return out.toByteArray();
        }
        catch ( IOException e ) {
            log.error("Failed to dump object " + val, e); //$NON-NLS-1$
            return new byte[0];
        }
    }


    /**
     * @param oin
     * @param nin
     * @return
     */
    static boolean diffFields ( Object oin, Object nin, Class<?> cl ) throws Exception {
        boolean anychange = false;
        for ( Field f : cl.getDeclaredFields() ) {
            f.setAccessible(true);

            if ( Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()) ) {
                continue;
            }

            Object of = f.get(oin);
            Object nf = f.get(nin);

            byte[] ofb = dump(of);
            byte[] nfb = dump(nf);

            if ( !Arrays.equals(ofb, nfb) ) {
                if ( !f.getType().isPrimitive() && f.getType().getName().startsWith("eu.agno3.") ) { //$NON-NLS-1$
                    if ( SessionDiffUtil.diffFields(of, nf) ) {
                        anychange |= true;
                        log.debug("Field change " + cl.getName() + "." + f.getName()); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                else if ( Objects.equals(of, nf) ) {
                    log.trace("Serialization change " + f.getType()); //$NON-NLS-1$
                }
                else {
                    anychange |= true;
                    log.debug("Value change " + cl.getName() + "." + f.getName() + " - " + f.getType()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
        }

        if ( cl.getSuperclass() != Object.class ) {
            anychange |= diffFields(oin, nin, cl.getSuperclass());
        }

        return anychange;
    }


    /**
     * @param oin
     * @param nin
     * @return
     * @throws Exception
     */
    static boolean diffFields ( Object oin, Object nin ) throws Exception {
        return diffFields(oin, nin, oin.getClass());
    }


    /**
     * @param oldAttrs
     * @param newAttrs
     */
    @SuppressWarnings ( "unchecked" )
    static boolean diffMap ( Map<?, Object> oldAttrs, Map<?, Object> newAttrs ) {

        Set<Object> addedKeys = new HashSet<>(newAttrs.keySet());
        addedKeys.removeAll(oldAttrs.keySet());
        Set<Object> removedKeys = new HashSet<>(oldAttrs.keySet());
        removedKeys.removeAll(newAttrs.keySet());
        Set<Object> commonKeys = new HashSet<>(oldAttrs.keySet());
        commonKeys.retainAll(newAttrs.keySet());

        boolean anychange = !addedKeys.isEmpty() || !removedKeys.isEmpty();

        if ( anychange && log.isDebugEnabled() ) {
            log.debug(String.format("Added: %s Removed: %s", addedKeys, removedKeys)); //$NON-NLS-1$
        }

        for ( Object k : commonKeys ) {
            Object ov = oldAttrs.get(k);
            Object nv = newAttrs.get(k);

            byte[] bov = dump(ov);
            byte[] bnv = dump(nv);

            if ( !Arrays.equals(bov, bnv) && !Objects.equals(ov, nv) ) {
                try {
                    if ( "org.apache.myfaces.application.viewstate.ServerSideStateCacheImpl.SERIALIZED_VIEW".equals(k) ) { //$NON-NLS-1$
                        Field keysF = ov.getClass().getDeclaredField("_keys"); //$NON-NLS-1$
                        keysF.setAccessible(true);
                        Field winKeysF = ov.getClass().getDeclaredField("_lastWindowKeys"); //$NON-NLS-1$
                        winKeysF.setAccessible(true);
                        Field serViewsF = ov.getClass().getDeclaredField("_serializedViews"); //$NON-NLS-1$
                        serViewsF.setAccessible(true);

                        Map<Object, Object> oser = (Map<Object, Object>) serViewsF.get(ov);
                        Map<Object, Object> nser = (Map<Object, Object>) serViewsF.get(nv);
                        if ( diffMap(nser, oser) ) {
                            anychange = true;
                            log.debug("Changed SerializedViews"); //$NON-NLS-1$
                        }
                    }
                    else if ( "org.apache.webbeans.context.SessionContext".equals(ov.getClass().getName()) ) { //$NON-NLS-1$
                        Field instanceF = ov.getClass().getSuperclass().getSuperclass().getDeclaredField("componentInstanceMap"); //$NON-NLS-1$
                        instanceF.setAccessible(true);

                        Map<Object, Object> oldI = (Map<Object, Object>) instanceF.get(ov);
                        Map<Object, Object> newI = (Map<Object, Object>) instanceF.get(nv);

                        if ( diffMap(newI, oldI) ) {
                            anychange = true;
                            log.debug("Changed SessionContext"); //$NON-NLS-1$
                        }
                    }
                    else if ( "org.apache.webbeans.component.ManagedBean".equals(k.getClass().getName()) ) { //$NON-NLS-1$
                        Class<?> cl = k.getClass().getSuperclass().getSuperclass();
                        Field clazzF = cl.getDeclaredField("beanClass"); //$NON-NLS-1$
                        clazzF.setAccessible(true);

                        Class<?> clz = (Class<?>) clazzF.get(k);

                        if ( !"org.apache.myfaces.cdi.view.ViewScopeBeanHolder".equals(clz.getName()) ) { //$NON-NLS-1$
                            continue;
                        }

                        Field instanceF = ov.getClass().getDeclaredField("beanInstance"); //$NON-NLS-1$
                        instanceF.setAccessible(true);

                        Object oo = instanceF.get(ov);
                        Object no = instanceF.get(nv);

                        Field storageF = oo.getClass().getDeclaredField("storageMap"); //$NON-NLS-1$
                        storageF.setAccessible(true);

                        Map<Object, Object> oldI = (Map<Object, Object>) storageF.get(oo);
                        Map<Object, Object> newI = (Map<Object, Object>) storageF.get(no);
                        if ( !Objects.equals(oldI, newI) ) {
                            if ( diffMap(newI, oldI) ) {
                                anychange = true;
                                log.debug("Changed ViewScope"); //$NON-NLS-1$
                            }
                        }
                    }
                    else if ( "org.apache.myfaces.cdi.view.ViewScopeContextualStorage".equals(ov.getClass().getName()) ) { //$NON-NLS-1$
                        Field nameKeyF = ov.getClass().getDeclaredField("nameBeanKeyMap"); //$NON-NLS-1$
                        nameKeyF.setAccessible(true);

                        Object onk = nameKeyF.get(ov);
                        Object nnk = nameKeyF.get(nv);

                        if ( !Objects.equals(onk, nnk) ) {
                            log.debug("Name key change"); //$NON-NLS-1$
                        }

                        Field instanceF = ov.getClass().getDeclaredField("contextualInstances"); //$NON-NLS-1$
                        instanceF.setAccessible(true);

                        Map<Object, Object> oin = (Map<Object, Object>) instanceF.get(ov);
                        Map<Object, Object> nin = (Map<Object, Object>) instanceF.get(nv);

                        if ( diffMap(oin, nin) ) {
                            log.debug("Changed contextualInstances"); //$NON-NLS-1$
                            anychange |= true;
                        }
                    }
                    else if ( "org.apache.myfaces.cdi.util.ContextualInstanceInfo".equals(ov.getClass().getName()) ) { //$NON-NLS-1$
                        Field instanceF = ov.getClass().getDeclaredField("contextualInstance"); //$NON-NLS-1$
                        instanceF.setAccessible(true);

                        Object oin = instanceF.get(ov);
                        Object nin = instanceF.get(nv);

                        byte[] ob = dump(oin);
                        byte[] nb = dump(nin);

                        if ( !Objects.equals(ob, nb) && !Arrays.equals(ob, nb) ) {
                            if ( diffFields(oin, nin) ) {
                                anychange |= true;
                                log.debug("Instance changed " + k); //$NON-NLS-1$
                            }
                        }
                    }
                    else {
                        log.debug(String.format("Changed %s: (%d->%d) %s -> %s", k, bov.length, bnv.length, ov, nv)); //$NON-NLS-1$
                    }
                }
                catch ( Exception e ) {
                    log.error("Failed", e); //$NON-NLS-1$
                }
            }
        }

        return anychange;
    }


    /**
     * @param n
     * @param o
     */
    static void diff ( SessionData n, SessionData o ) {
        diffMap(o.getAllAttributes(), n.getAllAttributes());
    }

}
