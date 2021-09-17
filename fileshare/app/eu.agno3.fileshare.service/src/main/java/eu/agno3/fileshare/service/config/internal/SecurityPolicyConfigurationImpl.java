/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.shiro.subject.Subject;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.fileshare.exceptions.PolicyNotFoundException;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.service.config.SecurityPolicyConfiguration;
import eu.agno3.runtime.security.password.PasswordType;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = SecurityPolicyConfiguration.class, configurationPid = "policies", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class SecurityPolicyConfigurationImpl implements SecurityPolicyConfiguration {

    private static final Logger log = Logger.getLogger(SecurityPolicyConfigurationImpl.class);

    private Map<String, PolicyConfiguration> policies = new HashMap<>();

    private List<String> sortedPolicyNames;

    private String defaultPolicyName = "DEFAULT"; //$NON-NLS-1$
    private String defaultRootContainerLabel;

    private Map<String, String> defaultUserLabelRules;

    private int sharePasswordBits;

    private PasswordType sharePasswordType;


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, updated = "updatedPolicyConfiguration" )
    protected synchronized void bindPolicyConfiguration ( PolicyConfiguration pc ) {
        PolicyConfiguration old = this.policies.put(pc.getLabel().toLowerCase(), pc);
        if ( old != null ) {
            log.warn("Overriding existing policy for label " + pc.getLabel()); //$NON-NLS-1$
        }
        this.sortedPolicyNames = null;
    }


    protected synchronized void updatedPolicyConfiguration ( PolicyConfiguration pc ) {
        this.sortedPolicyNames = null;
    }


    protected synchronized void unbindPolicyConfiguration ( PolicyConfiguration pc ) {
        this.policies.remove(pc.getLabel());
        this.sortedPolicyNames = null;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    /**
     * @param ctx
     */
    private void parseConfig ( ComponentContext ctx ) {
        Dictionary<String, Object> cfg = ctx.getProperties();
        this.defaultPolicyName = ConfigUtil.parseString(cfg, "defaultPolicy", null); //$NON-NLS-1$
        if ( !hasPolicy(this.defaultPolicyName) ) {
            log.warn("Default policy is not available: " + this.defaultPolicyName); //$NON-NLS-1$
        }

        this.defaultRootContainerLabel = ConfigUtil.parseString(cfg, "defaultRootContainerLabel", null); //$NON-NLS-1$
        if ( this.defaultRootContainerLabel != null && !hasPolicy(this.defaultRootContainerLabel) ) {
            log.warn("Default root container policy is not available: " + this.defaultRootContainerLabel); //$NON-NLS-1$
        }
        else if ( this.defaultRootContainerLabel == null ) {
            this.defaultRootContainerLabel = this.defaultPolicyName;
        }

        this.defaultUserLabelRules = ConfigUtil.parseStringMap(cfg, "defaultUserLabelRules", new HashMap<>()); //$NON-NLS-1$

        this.sharePasswordBits = ConfigUtil.parseInt(cfg, "sharePasswordsBits", 64); //$NON-NLS-1$
        String sharePwType = ConfigUtil.parseString(
            cfg,
            "sharePasswordsType", //$NON-NLS-1$
            "DEFAULT"); //$NON-NLS-1$
        this.sharePasswordType = PasswordType.valueOf(sharePwType);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#getDefinedLabels()
     */
    @Override
    public List<String> getDefinedLabels () {
        List<String> polNames = this.sortedPolicyNames;
        if ( polNames != null ) {
            return polNames;
        }

        List<PolicyConfiguration> sorted = new ArrayList<>(this.policies.values());
        Collections.sort(sorted, new PolicyConfigurationComparator());
        List<String> labels = new LinkedList<>();
        for ( PolicyConfiguration pol : sorted ) {
            labels.add(pol.getLabel());
        }
        this.sortedPolicyNames = labels;
        return labels;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#getDefaultLabel()
     */
    @Override
    public String getDefaultLabel () {
        return this.defaultPolicyName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#getRootContainerLabel()
     */
    @Override
    public String getRootContainerLabel () {
        return this.defaultRootContainerLabel;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#compareLabels(eu.agno3.fileshare.model.SecurityLabel,
     *      eu.agno3.fileshare.model.SecurityLabel)
     */
    @Override
    public int compareLabels ( String a, String b ) {
        List<String> definedLabels = this.getDefinedLabels();
        int idxA = a != null ? definedLabels.indexOf(a) : -1;
        int idxB = b != null ? definedLabels.indexOf(b) : -1;

        if ( idxA < 0 && idxB < 0 ) {
            // a+b are unlabled
            return 0;
        }
        else if ( idxA < 0 ) {
            // a is unlabeled
            return -1;
        }
        else if ( idxB < 0 ) {
            // b is unlabeled
            return 1;
        }

        int res = Integer.compare(idxA, idxB);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("a: %s = %d <-> %s = %d ==> %d", a, idxA, b, idxB, res)); //$NON-NLS-1$
        }

        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#compareLabels(eu.agno3.fileshare.model.SecurityLabel,
     *      eu.agno3.fileshare.model.SecurityLabel)
     */
    @Override
    public int compareLabels ( SecurityLabel a, SecurityLabel b ) {
        if ( a == null && b == null ) {
            return 0;
        }
        else if ( a == null ) {
            return -1;
        }
        else if ( b == null ) {
            return 1;
        }

        return compareLabels(a.getLabel(), b.getLabel());
    }


    /**
     * {@inheritDoc}
     * 
     * @throws PolicyNotFoundException
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#getPolicy(java.lang.String)
     */
    @Override
    public PolicyConfiguration getPolicy ( String label ) throws PolicyNotFoundException {
        PolicyConfiguration policy = this.policies.get(label.toLowerCase());

        if ( policy == null ) {
            throw new PolicyNotFoundException(label, "Policy not found"); //$NON-NLS-1$
        }
        return policy;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#hasPolicy(java.lang.String)
     */
    @Override
    public boolean hasPolicy ( String label ) {
        return this.policies.containsKey(label.toLowerCase());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#getDefaultUserLabelForRoles(java.util.Set)
     */
    @Override
    public String getDefaultUserLabelForRoles ( Set<String> roles ) {
        for ( Entry<String, String> e : this.defaultUserLabelRules.entrySet() ) {
            if ( roles.contains(e.getKey()) ) {
                return e.getValue();
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#getDefaultUserLabelForSubject(org.apache.shiro.subject.Subject)
     */
    @Override
    public String getDefaultUserLabelForSubject ( Subject s ) {
        for ( Entry<String, String> e : this.defaultUserLabelRules.entrySet() ) {
            if ( s.hasRole(e.getKey()) ) {
                return e.getValue();
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#getSharePasswordBits()
     */
    @Override
    public int getSharePasswordBits () {
        return this.sharePasswordBits;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SecurityPolicyConfiguration#getSharePasswordType()
     */
    @Override
    public PasswordType getSharePasswordType () {
        return this.sharePasswordType;
    }
}
