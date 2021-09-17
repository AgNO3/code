/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.09.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.user;


import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;

import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureMembershipConfigurator extends AbstractConfigurator<StatusOnlyResult, EnsureMembership, EnsureMembershipConfigurator> {

    /**
     * @param unit
     */
    protected EnsureMembershipConfigurator ( EnsureMembership unit ) {
        super(unit);
    }


    /**
     * 
     * @param user
     * @return this configurator
     */
    public EnsureMembershipConfigurator user ( String user ) {
        getExecutionUnit().setUser(user);
        return this.self();
    }


    /**
     * 
     * @param up
     * @return this configurator
     */
    public EnsureMembershipConfigurator user ( UserPrincipal up ) {
        return user(up.getName());
    }


    /**
     * 
     * @param group
     * @return this configurator
     */
    public EnsureMembershipConfigurator group ( String group ) {
        this.getExecutionUnit().setGroup(group);
        return this.self();
    }


    /**
     * 
     * @param gp
     * @return this configurator
     */
    public EnsureMembershipConfigurator group ( GroupPrincipal gp ) {
        return group(gp.getName());
    }
}
