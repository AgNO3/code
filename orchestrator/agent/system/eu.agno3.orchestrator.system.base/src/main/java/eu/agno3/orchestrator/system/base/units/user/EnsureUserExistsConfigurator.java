/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.09.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.user;


import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureUserExistsConfigurator extends AbstractConfigurator<StatusOnlyResult, EnsureUserExists, EnsureUserExistsConfigurator> {

    /**
     * @param unit
     */
    protected EnsureUserExistsConfigurator ( EnsureUserExists unit ) {
        super(unit);
    }


    /**
     * 
     * @param name
     * @return this configurator
     */
    public EnsureUserExistsConfigurator user ( String name ) {
        this.getExecutionUnit().setName(name);
        return this.self();
    }


    /**
     * 
     * @return this configurator
     */
    public EnsureUserExistsConfigurator system () {
        this.getExecutionUnit().setSystem(true);
        return this.self();
    }


    /**
     * 
     * @param uid
     * @return this configurator
     */
    public EnsureUserExistsConfigurator uid ( int uid ) {
        this.getExecutionUnit().setUid(uid);
        return this.self();
    }


    /**
     * 
     * @param group
     * @return this configurator
     */
    public EnsureUserExistsConfigurator primaryGroup ( String group ) {
        this.getExecutionUnit().setPrimaryGroup(group);
        return this.self();
    }


    /**
     * 
     * @param groups
     * @return this configurator
     */
    public EnsureUserExistsConfigurator groups ( List<String> groups ) {
        this.getExecutionUnit().setGroups(new ArrayList<>(groups));
        return this.self();
    }


    /**
     * 
     * @param groups
     * @return this configurator
     */
    public EnsureUserExistsConfigurator groups ( String... groups ) {
        return this.groups(Arrays.asList(groups));
    }


    /**
     * 
     * @param home
     * @return this configurator
     */
    public EnsureUserExistsConfigurator home ( String home ) {
        this.getExecutionUnit().setHome(home);
        return this.self();
    }


    /**
     * @param home
     * @return this configurator
     */
    public EnsureUserExistsConfigurator home ( File home ) {
        return home(home.toString());
    }


    /**
     * @param home
     * @return this configurator
     */
    public EnsureUserExistsConfigurator home ( Path home ) {
        return home(home.toString());
    }


    /**
     * @param shell
     * @return this configurator
     */
    public EnsureUserExistsConfigurator shell ( String shell ) {
        this.getExecutionUnit().setShell(shell);
        return this.self();
    }

}
