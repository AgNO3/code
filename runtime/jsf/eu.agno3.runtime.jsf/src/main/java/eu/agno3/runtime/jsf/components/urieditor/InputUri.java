/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.urieditor;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.webbeans.util.StringUtil;

import eu.agno3.runtime.jsf.components.ResettableComponent;
import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 *
 */
public class InputUri extends UIInput implements NamingContainer, ResettableComponent {

    /**
     * 
     */
    private static final String SCHEME = "scheme"; //$NON-NLS-1$
    private static final String DEFAULT_SCHEME = "defaultScheme"; //$NON-NLS-1$

    private static final String USERINFO = "userinfo"; //$NON-NLS-1$
    private static final String DEFAULT_USERINFO = "defaultUserInfo"; //$NON-NLS-1$

    private static final String HOST = "host"; //$NON-NLS-1$
    private static final String DEFAULT_HOST = "defaultHost"; //$NON-NLS-1$

    private static final String PORT = "port"; //$NON-NLS-1$
    private static final String DEFAULT_PORT = "defaultPort"; //$NON-NLS-1$

    private static final String PATH = "path"; //$NON-NLS-1$
    private static final String DEFAULT_PATH = "defaultPath"; //$NON-NLS-1$

    private static final String QUERY = "query"; //$NON-NLS-1$
    private static final String DEFAULT_QUERY = "defaultQuery"; //$NON-NLS-1$

    private static final String FRAGMENT = "fragment"; //$NON-NLS-1$
    private static final String DEFAULT_FRAGEMENT = "defaultFragment"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(InputUri.class);

    private Set<String> readOnly;
    private Set<String> visible;


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#getFamily()
     */
    @Override
    public String getFamily () {
        return UINamingContainer.COMPONENT_FAMILY;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.ResettableComponent#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        log.debug("Resetting components " + this.getStateHelper()); //$NON-NLS-1$
        this.getStateHelper().remove(SCHEME);
        this.getStateHelper().remove(USERINFO);
        this.getStateHelper().remove(HOST);
        this.getStateHelper().remove(PORT);
        this.getStateHelper().remove(QUERY);
        this.getStateHelper().remove(FRAGMENT);
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processValidators(javax.faces.context.FacesContext)
     */
    @Override
    public void processValidators ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processValidators(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processUpdates(javax.faces.context.FacesContext)
     */
    @Override
    public void processUpdates ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processUpdates(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * 
     * @param field
     * @return whether field should be visible
     */
    public boolean isVisible ( String field ) {
        return getVisible().contains(field);
    }


    /**
     * @return
     * 
     */
    private Set<String> getVisible () {
        if ( this.visible == null ) {
            String[] split = StringUtils.split((String) getAttributes().get("fields"), ','); //$NON-NLS-1$
            if ( split == null ) {
                this.visible = new HashSet<>(Arrays.asList(SCHEME, HOST, PORT, PATH));
            }
            else {
                this.visible = new HashSet<>(Arrays.asList(split));
            }
        }
        return this.visible;
    }


    /**
     * 
     * @param field
     * @return whether field should be read only
     */
    public boolean isReadOnly ( String field ) {
        if ( this.readOnly == null ) {
            String[] split = StringUtils.split((String) getAttributes().get("readOnly"), ','); //$NON-NLS-1$
            if ( split == null ) {
                this.readOnly = Collections.EMPTY_SET;
            }
            else {
                this.readOnly = new HashSet<>(Arrays.asList(split));
            }
        }
        return this.readOnly.contains(field);
    }


    /**
     * @return the scheme
     */
    public String getScheme () {
        if ( this.isReadOnly(SCHEME) && this.getAttributes().get(DEFAULT_SCHEME) != null ) {
            return (String) this.getAttributes().get(DEFAULT_SCHEME);
        }

        String local = (String) this.getStateHelper().get(SCHEME);

        if ( !StringUtil.isBlank(local) ) {
            return local;
        }

        URI u = (URI) getValue();
        if ( u != null && !StringUtils.isBlank(u.getScheme()) ) {
            return u.getScheme();
        }

        return (String) this.getAttributes().get(DEFAULT_SCHEME);
    }


    /**
     * @param scheme
     */
    public void setScheme ( String scheme ) {
        if ( !this.isReadOnly(SCHEME) ) {
            this.getStateHelper().put(SCHEME, scheme);
        }
    }


    /**
     * @return user info
     */
    public String getUserInfo () {
        String local = (String) this.getStateHelper().get(USERINFO);

        if ( !StringUtil.isBlank(local) ) {
            return local;
        }

        URI u = (URI) getValue();
        if ( u != null && !StringUtils.isBlank(u.getUserInfo()) ) {
            return u.getUserInfo();
        }

        return (String) this.getAttributes().get(DEFAULT_USERINFO);
    }


    /**
     * 
     * @param userInfo
     */
    public void setUserInfo ( String userInfo ) {
        if ( !this.isReadOnly(USERINFO) ) {
            this.getStateHelper().put(USERINFO, userInfo);
        }
    }


    /**
     * @return fragment
     */
    public String getFragment () {
        String local = (String) this.getStateHelper().get(FRAGMENT);

        if ( !StringUtil.isBlank(local) ) {
            return local;
        }

        URI u = (URI) getValue();
        if ( u != null && !StringUtils.isBlank(u.getFragment()) ) {
            return u.getFragment();
        }

        return (String) this.getAttributes().get(DEFAULT_FRAGEMENT);
    }


    /**
     * 
     * @param fragment
     */
    public void setFragment ( String fragment ) {
        if ( !this.isReadOnly(FRAGMENT) ) {
            this.getStateHelper().put(FRAGMENT, fragment);
        }
    }


    /**
     * @return query
     */
    public String getQuery () {
        String local = (String) this.getStateHelper().get(QUERY);

        if ( !StringUtil.isBlank(local) ) {
            return local;
        }

        URI u = (URI) getValue();
        if ( u != null && !StringUtils.isBlank(u.getQuery()) ) {
            return u.getQuery();
        }

        return (String) this.getAttributes().get(DEFAULT_QUERY);
    }


    /**
     * 
     * @param query
     */
    public void setQuery ( String query ) {
        if ( !this.isReadOnly(QUERY) ) {
            this.getStateHelper().put(QUERY, query);
        }
    }


    /**
     * @return path
     */
    public String getPath () {
        String local = (String) this.getStateHelper().get(PATH);

        if ( !StringUtil.isBlank(local) ) {
            return local;
        }

        URI u = (URI) getValue();
        if ( u != null && !StringUtils.isBlank(u.getPath()) ) {
            return u.getPath();
        }

        return (String) this.getAttributes().get(DEFAULT_PATH);
    }


    /**
     * 
     * @param path
     */
    public void setPath ( String path ) {
        if ( !this.isReadOnly(PATH) ) {
            this.getStateHelper().put(PATH, path);
        }
    }


    /**
     * @return port
     */
    public int getPort () {
        Integer local = (Integer) this.getStateHelper().get(PORT);

        if ( local != null ) {
            return local;
        }

        URI u = (URI) getValue();
        if ( u != null && u.getPort() != -1 ) {
            return u.getPort();
        }

        Integer port = Integer.valueOf((String) this.getAttributes().get(DEFAULT_PORT));
        if ( port == null ) {
            return 0;
        }
        return port;
    }


    /**
     * @param port
     */
    public void setPort ( int port ) {
        if ( !this.isReadOnly(PORT) ) {
            Integer def = Integer.valueOf((String) this.getAttributes().get(DEFAULT_PORT));

            if ( def == null || def != port ) {
                this.getStateHelper().put(PORT, port);
            }
            else if ( def == port ) {
                this.getStateHelper().remove(PORT);
            }
        }
    }


    /**
     * @return host
     */
    public String getHost () {
        String local = (String) this.getStateHelper().get(HOST);

        if ( !StringUtil.isBlank(local) ) {
            return local;
        }

        URI u = (URI) getValue();
        if ( u != null && !StringUtils.isBlank(u.getHost()) ) {
            return u.getHost();
        }

        return (String) this.getAttributes().get(DEFAULT_HOST);
    }


    /**
     * 
     * @param host
     */
    public void setHost ( String host ) {
        if ( !this.isReadOnly(HOST) ) {
            this.getStateHelper().put(HOST, host);
        }
    }


    /**
     * @return
     */
    private URI makeURI ( FacesContext ctx ) {
        try {
            boolean anySet = false;

            for ( String field : getVisible() ) {
                if ( !isReadOnly(field) && this.getStateHelper().get(field) != null ) {
                    anySet = true;
                    break;
                }
            }

            if ( !anySet ) {
                return null;
            }

            return new URI(getScheme(), getUserInfo(), getHost(), getPort(), getPath(), getQuery(), getFragment());
        }
        catch ( URISyntaxException e ) {
            log.debug("Invalid URI", e); //$NON-NLS-1$
            ctx.addMessage(this.getClientId(ctx), new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("uri.invalid"), e.getMessage())); //$NON-NLS-1$
            ctx.validationFailed();
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#updateModel(javax.faces.context.FacesContext)
     */
    @Override
    public void updateModel ( FacesContext context ) {
        // make sure not to pick parts of the old URI
        setValue(null);
        URI actualUri = makeURI(context);
        if ( log.isDebugEnabled() ) {
            log.debug("Actual value " + actualUri); //$NON-NLS-1$
        }
        if ( actualUri == null ) {
            return;
        }
        setValue(actualUri);
        super.updateModel(context);
    }

}
