/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2016 by mbechler
 */
package eu.agno3.runtime.security.terms.internal;


import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.web.util.WebUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.configloader.file.ConfigFileLoader;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.db.DataSourceUtil;
import eu.agno3.runtime.db.schema.SchemaManagedDataSource;
import eu.agno3.runtime.security.DynamicModularRealmAuthorizer;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.terms.TermsDefinition;
import eu.agno3.runtime.security.terms.TermsService;
import eu.agno3.runtime.security.web.cookie.CookieCryptor;
import eu.agno3.runtime.security.web.cookie.CookieType;


/**
 * @author mbechler
 *
 */
@Component ( service = TermsService.class )
public class TermsServiceImpl implements TermsService {

    private static final Logger log = Logger.getLogger(TermsServiceImpl.class);

    private static final String TERMS_KEY_PREFIX = "terms.accept."; //$NON-NLS-1$
    private static final String ALL_ACCEPTED = "terms.all"; //$NON-NLS-1$
    private static final String TERMS_PREFIX = "/terms/"; //$NON-NLS-1$

    private Map<String, TermsDefinition> termsById = new HashMap<>();
    private Set<TermsDefinition> orderedTerms = new TreeSet<>(new TermsComparator());
    private SchemaManagedDataSource dataSource;
    private CookieCryptor cookieCyptor;

    private ConfigFileLoader cfLoader;

    private Control ctrl = new ResourceBundle.Control() {

    };

    private DynamicModularRealmAuthorizer authorizer;

    private DataSourceUtil dataSourceUtil;


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindTermDefinition ( TermsDefinition def ) {
        this.termsById.put(def.getId(), def);
        this.orderedTerms.add(def);
    }


    protected synchronized void unbindTermDefinition ( TermsDefinition def ) {
        this.termsById.remove(def.getId(), def);
        this.orderedTerms.remove(def);
    }


    @Reference ( target = "(dataSourceName=auth)" )
    protected synchronized void setDataSource ( SchemaManagedDataSource ds ) {
        this.dataSource = ds;
    }


    protected synchronized void unsetDataSource ( SchemaManagedDataSource ds ) {
        if ( this.dataSource == ds ) {
            this.dataSource = null;
        }
    }


    @Reference ( target = "(dataSourceName=auth)" )
    protected synchronized void setDataSourceUtil ( DataSourceUtil dsu ) {
        this.dataSourceUtil = dsu;
    }


    protected synchronized void unsetDataSourceUtil ( DataSourceUtil dsu ) {
        if ( this.dataSourceUtil == dsu ) {
            this.dataSourceUtil = null;
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void setAuthorizer ( DynamicModularRealmAuthorizer aip ) {
        this.authorizer = aip;
    }


    protected synchronized void unsetAuthorizer ( DynamicModularRealmAuthorizer aip ) {
        if ( this.authorizer == aip ) {
            this.authorizer = null;
        }
    }


    @Reference
    protected synchronized void setConfigFileLoader ( ConfigFileLoader cflb ) {
        this.cfLoader = cflb;
    }


    protected synchronized void unsetConfigFileLoader ( ConfigFileLoader cflb ) {
        if ( this.cfLoader == cflb ) {
            this.cfLoader = null;
        }
    }


    @Reference
    protected synchronized void setCookieCryptor ( CookieCryptor cc ) {
        this.cookieCyptor = cc;
    }


    protected synchronized void unsetCookieCryptor ( CookieCryptor cc ) {
        if ( this.cookieCyptor == cc ) {
            this.cookieCyptor = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.terms.TermsService#getRequiredTerms(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public Collection<TermsDefinition> getRequiredTerms ( UserPrincipal up ) {

        Session session = SecurityUtils.getSubject().getSession(false);
        if ( session != null && session.getAttribute(ALL_ACCEPTED) instanceof Boolean && (boolean) session.getAttribute(ALL_ACCEPTED) ) {
            return Collections.EMPTY_LIST;
        }

        Map<String, DateTime> acceptedTerms = getAcceptedTerms(up);
        Collection<TermsDefinition> requiredTerms = new LinkedList<>();

        for ( TermsDefinition terms : getAllTerms(up) ) {
            String tid = terms.getId();
            DateTime acceptanceDate = getAcceptanceDate(acceptedTerms, up, terms, tid);
            if ( acceptanceDate != null && ( terms.getLastModified() == null || terms.getLastModified().isBefore(acceptanceDate) ) ) {
                // terms have been accepted and not modified since acceptance date
                if ( log.isDebugEnabled() ) {
                    log.debug("Skipping as already accepted " + tid); //$NON-NLS-1$
                }
                continue;
            }
            requiredTerms.add(terms);
        }

        if ( session != null && requiredTerms.isEmpty() ) {
            session.setAttribute(ALL_ACCEPTED, true);
        }

        return requiredTerms;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.terms.TermsService#getAllTerms(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public Collection<TermsDefinition> getAllTerms ( UserPrincipal up ) {
        Collection<TermsDefinition> terms = new LinkedList<>();

        Set<String> roles = resolveRoles(up);
        if ( log.isDebugEnabled() ) {
            log.debug(roles);
        }

        for ( TermsDefinition def : this.orderedTerms ) {
            if ( def.isApplicable(up, roles) ) {
                terms.add(def);
            }
        }
        return terms;
    }


    /**
     * @param up
     * @return
     */
    Set<String> resolveRoles ( UserPrincipal up ) {
        DynamicModularRealmAuthorizer auth = this.authorizer;
        Set<String> roles = null;
        if ( auth != null && up != null ) {
            SimpleAuthorizationInfo ai = auth.getAuthorizationInfo(new SimplePrincipalCollection(up, up.getRealmName()));
            if ( ai != null ) {
                roles = ai.getRoles();
            }
        }
        return roles;
    }


    /**
     * @param acceptedTerms
     * @param up
     * @param terms
     * @param tid
     * @return
     */
    private DateTime getAcceptanceDate ( Map<String, DateTime> acceptedTerms, UserPrincipal up, TermsDefinition terms, String tid ) {

        if ( up != null && terms.isPersistAcceptance() ) {
            DateTime dateTime = acceptedTerms.get(tid);
            if ( dateTime != null ) {
                return dateTime;
            }
        }

        String key = TERMS_KEY_PREFIX + tid;
        Session session = SecurityUtils.getSubject().getSession(false);
        if ( session != null ) {
            Object sessAccept = session.getAttribute(key);
            if ( sessAccept instanceof DateTime ) {
                log.debug("Found acceptance in session"); //$NON-NLS-1$
                return (DateTime) sessAccept;
            }
        }

        if ( up == null && terms.isPersistAcceptance() ) {
            return getAcceptanceDateFromCookie(key);
        }
        return null;
    }


    /**
     * @param key
     * @return
     */
    private DateTime getAcceptanceDateFromCookie ( String key ) {
        HttpServletRequest req = WebUtils.getHttpRequest(SecurityUtils.getSubject());
        if ( req != null ) {
            Cookie[] cookies = req.getCookies();
            if ( cookies != null ) {
                for ( Cookie cookie : cookies ) {
                    if ( key.equals(cookie.getName()) ) {
                        try {
                            log.debug("Found acceptance in cookie " + cookie.getValue()); //$NON-NLS-1$
                            String val = this.cookieCyptor.decodeCookie(cookie, CookieType.SIGN);
                            if ( !StringUtils.isBlank(val) ) {
                                return DateTime.parse(val);
                            }
                        }
                        catch ( Exception e ) {
                            log.debug("Failed to parse acceptance date", e); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
        return null;
    }


    /**
     * @param up
     * @return
     */
    private Map<String, DateTime> getAcceptedTerms ( UserPrincipal up ) {
        if ( up == null || up.getUserId() == null ) {
            return Collections.EMPTY_MAP;
        }
        Map<String, DateTime> accepted = new HashMap<>();
        try ( Connection c = this.dataSource.getConnection() ) {
            try ( PreparedStatement ps = c.prepareStatement("SELECT terms_id, acceptance_date FROM " + //$NON-NLS-1$
                    this.dataSourceUtil.quoteIdentifier(c, "terms_acceptance") + //$NON-NLS-1$
                    " WHERE user_id = ?") ) { //$NON-NLS-1$
                this.dataSourceUtil.setParameter(ps, 1, up.getUserId());
                try ( ResultSet rs = ps.executeQuery() ) {
                    while ( rs.next() ) {
                        String tid = rs.getString(1);
                        Timestamp acceptanceTs = rs.getTimestamp(2);
                        DateTime acceptanceDate = new DateTime(acceptanceTs, DateTimeZone.UTC);
                        accepted.put(tid, acceptanceDate);
                    }
                }
            }
        }
        catch ( SQLException e ) {
            log.warn("Failed to fetch accepted terms", e); //$NON-NLS-1$
        }
        return accepted;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.terms.TermsService#persistTemporary(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public void persistTemporary ( UserPrincipal principal ) {
        for ( TermsDefinition terms : this.orderedTerms ) {
            String tid = terms.getId();
            DateTime acceptanceDate = getAcceptanceDate(Collections.EMPTY_MAP, null, terms, tid);
            if ( acceptanceDate == null || ( terms.getLastModified() != null && terms.getLastModified().isAfter(acceptanceDate) ) ) {
                // terms have been accepted and not modified since
                if ( log.isDebugEnabled() ) {
                    log.debug("Skipping as already accepted " + tid); //$NON-NLS-1$
                }
                continue;
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Marking terms as accepted " + tid); //$NON-NLS-1$
            }
            markAcceptedInternal(principal, acceptanceDate, terms);
        }
    }


    @Override
    public void markAccepted ( UserPrincipal up, String id ) {
        markAcceptedInternal(up, DateTime.now(), getTermsById(id));
    }


    /**
     * @param up
     * @param id
     * @param acceptanceDate
     * @param def
     */
    private void markAcceptedInternal ( UserPrincipal up, DateTime acceptanceDate, TermsDefinition def ) {
        if ( def == null ) {
            return;
        }

        String id = def.getId();
        String key = TERMS_KEY_PREFIX + id;
        Session session = SecurityUtils.getSubject().getSession(false);
        if ( session != null ) {
            log.debug("Marking acceptance in session"); //$NON-NLS-1$
            session.setAttribute(key, acceptanceDate); // $NON-NLS-1$
        }
        else {
            log.debug("No session found"); //$NON-NLS-1$
        }

        if ( up == null && def.isPersistAcceptance() ) {
            HttpServletRequest req = WebUtils.getHttpRequest(SecurityUtils.getSubject());
            HttpServletResponse resp = WebUtils.getHttpResponse(SecurityUtils.getSubject());
            if ( !resp.isCommitted() ) {
                log.debug("Setting cookie"); //$NON-NLS-1$

                try {
                    Cookie cookie = this.cookieCyptor.encodeCookie(key, acceptanceDate.toString(), CookieType.SIGN);
                    cookie.setPath(!StringUtils.isBlank(req.getContextPath()) ? req.getContextPath() : "/"); //$NON-NLS-1$
                    cookie.setMaxAge((int) def.getUnauthPersistenceMaxAge().getStandardSeconds());
                    resp.addCookie(cookie);
                }
                catch ( CryptoException e ) {
                    log.warn("Failed to create cookie", e); //$NON-NLS-1$
                }
            }
            else {
                log.debug("Cannot add cookie, response is commited"); //$NON-NLS-1$
            }
        }

        if ( up == null || !def.isPersistAcceptance() ) {
            return;
        }

        try ( Connection c = this.dataSource.getConnection() ) {
            try ( PreparedStatement ps = c.prepareStatement("UPDATE " + //$NON-NLS-1$
                    this.dataSourceUtil.quoteIdentifier(c, "terms_acceptance") //$NON-NLS-1$
                    + " SET acceptance_date = ?, version = version + 1 WHERE user_id = ? AND terms_id = ?") ) { //$NON-NLS-1$
                Timestamp acceptTs = new Timestamp(acceptanceDate.getMillis());
                ps.setTimestamp(1, acceptTs);
                this.dataSourceUtil.setParameter(ps, 2, up.getUserId());
                ps.setString(3, id);
                if ( ps.executeUpdate() == 0 ) {
                    try ( PreparedStatement cs = c.prepareStatement("INSERT INTO " + //$NON-NLS-1$
                            this.dataSourceUtil.quoteIdentifier(c, "terms_acceptance") + //$NON-NLS-1$
                            " (id, version, user_id, terms_id, acceptance_date) VALUES (?,?,?,?,?)") ) { //$NON-NLS-1$
                        this.dataSourceUtil.setParameter(cs, 1, UUID.randomUUID());
                        cs.setInt(2, 0);
                        this.dataSourceUtil.setParameter(cs, 3, up.getUserId());
                        cs.setString(4, id);
                        cs.setTimestamp(5, acceptTs);
                        cs.executeUpdate();
                    }
                }
            }
        }
        catch ( SQLException e ) {
            log.warn("Failed to mark accepted terms", e); //$NON-NLS-1$
        }
    }


    @Override
    public URL getContents ( String id, String format, Locale l ) throws IOException {
        TermsDefinition def = getTermsById(id);
        if ( def == null ) {
            return null;
        }
        String contentId = def.getContentId();
        for ( Locale candidate : this.ctrl.getCandidateLocales(contentId, l) ) {
            String lookup = TERMS_PREFIX + this.ctrl.toBundleName(contentId, candidate) + '.' + format;
            if ( this.cfLoader.exists(lookup) ) {
                return this.cfLoader.getURL(lookup);
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.terms.TermsService#getTermsById(java.lang.String)
     */
    @Override
    public TermsDefinition getTermsById ( String id ) {
        return this.termsById.get(id);
    }
}
