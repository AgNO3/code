/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.runtime.jsf.windowscope;


import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.ClientWindow;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.Nullable;


/**
 * @author mbechler
 *
 */
@Typed ( )
public class WindowScopedContextImpl implements Context {

    /**
     * {@inheritDoc}
     *
     * @see javax.enterprise.context.spi.Context#get(javax.enterprise.context.spi.Contextual)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T> T get ( Contextual<T> bean ) {
        checkActive();
        checkBean(bean);
        Map<Object, Object> st = getStorage();
        T obj = (T) st.get(getKey(bean));
        return obj;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.enterprise.context.spi.Context#get(javax.enterprise.context.spi.Contextual,
     *      javax.enterprise.context.spi.CreationalContext)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T> T get ( Contextual<T> bean, @Nullable CreationalContext<T> ctx ) {
        checkActive();
        checkBean(bean);

        Map<Object, Object> st = getStorage();
        Object key = getKey(bean);

        Object val = st.get(key);
        if ( val != null ) {
            return (T) val;
        }

        T created = bean.create(ctx);
        st.put(key, created);
        return created;
    }


    /**
     * @param fc
     * @param winId
     */
    public static void closeWindow ( FacesContext fc, String winId ) {
        ExternalContext externalContext = fc.getExternalContext();
        HttpSession sess = (HttpSession) externalContext.getSession(false);
        if ( sess != null ) {
            synchronized ( sess ) {
                String winKey = "window-" + winId; //$NON-NLS-1$
                sess.removeAttribute(winKey);
            }
        }
    }


    /**
     * @param fc
     * 
     */
    public static void closeCurrentWindow ( FacesContext fc ) {
        ExternalContext externalContext = fc.getExternalContext();
        String winId = externalContext.getClientWindow().getId();
        if ( !StringUtils.isBlank(winId) ) {
            closeWindow(fc, winId);
        }
    }


    /**
     * @param bean
     */
    private static <T> void checkBean ( Contextual<T> bean ) {
        if ( ! ( bean instanceof PassivationCapable ) ) {
            throw new IllegalStateException("Bean is not passivation capable " + bean.toString()); //$NON-NLS-1$
        }
    }


    /**
     * @return
     */
    private static Map<Object, Object> getStorage () {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpSession sess = (HttpSession) externalContext.getSession(false);
        String winId = externalContext.getClientWindow().getId();
        Map<Object, Object> st = getWindowStorage(sess, winId);
        return st;
    }


    /**
     * @param bean
     * @param ctx
     * @return
     */
    private static <T> Object getKey ( Contextual<T> bean ) {
        return ( (PassivationCapable) bean ).getId();
    }


    /**
     * @param sess
     * @param winId
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private static Map<Object, Object> getWindowStorage ( HttpSession sess, String winId ) {
        synchronized ( sess ) {
            String winKey = "window-" + winId; //$NON-NLS-1$
            Map<Object, Object> windowBeans = (Map<Object, Object>) sess.getAttribute(winKey);
            if ( windowBeans == null ) {
                windowBeans = new HashMap<>();
                sess.setAttribute(winKey, windowBeans);
            }
            return windowBeans;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.enterprise.context.spi.Context#getScope()
     */
    @Override
    public Class<? extends Annotation> getScope () {
        return WindowScoped.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.enterprise.context.spi.Context#isActive()
     */
    @Override
    public boolean isActive () {
        FacesContext fc = FacesContext.getCurrentInstance();
        if ( fc == null ) {
            return false;
        }

        ExternalContext ec = fc.getExternalContext();
        if ( ec == null ) {
            return false;
        }

        if ( ec.getSession(false) == null ) {
            return false;
        }

        ClientWindow w = ec.getClientWindow();
        if ( w == null ) {
            return false;
        }
        return !StringUtils.isBlank(w.getId());
    }


    protected void checkActive () {
        if ( !isActive() ) {
            throw new ContextNotActiveException("Window scope is not active, check JSF config"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "WINDOW_SCOPED"; //$NON-NLS-1$
    }
}
