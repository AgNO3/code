/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public class SystemUpdateUnit extends AbstractServiceUpdateUnit<SystemUpdateUnit> {

    /**
     * 
     */
    private static final long serialVersionUID = 6609564979847963778L;

    private String repository;
    private Set<SystemPackageTarget> targetProfile = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.update.AbstractServiceUpdateUnit#getType()
     */
    @Override
    public Class<SystemUpdateUnit> getType () {
        return SystemUpdateUnit.class;
    }


    /**
     * @return the targetProfile
     */
    public Set<SystemPackageTarget> getTargetProfile () {
        return this.targetProfile;
    }


    /**
     * @param targetProfile
     *            the targetProfile to set
     */
    public void setTargetProfile ( Set<SystemPackageTarget> targetProfile ) {
        this.targetProfile = targetProfile;
    }


    /**
     * @return the repository
     */
    public String getRepository () {
        return this.repository;
    }


    /**
     * @param repository
     *            the repository to set
     */
    public void setRepository ( String repository ) {
        this.repository = repository;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.update.AbstractServiceUpdateUnit#merge(eu.agno3.orchestrator.system.update.AbstractServiceUpdateUnit)
     */
    @Override
    public SystemUpdateUnit merge ( SystemUpdateUnit next ) {
        SystemUpdateUnit merged = new SystemUpdateUnit();

        if ( !Objects.equals(this.repository, next.repository) ) {
            throw new IllegalArgumentException("Repository mismatch"); //$NON-NLS-1$
        }

        merged.repository = this.repository;

        Map<String, SystemPackageTarget> mergedTargets = new HashMap<>();
        for ( SystemPackageTarget entry : this.targetProfile ) {
            mergedTargets.put(entry.getPackageName(), entry);
        }

        for ( SystemPackageTarget entry : next.targetProfile ) {
            SystemPackageTarget existing = mergedTargets.get(entry.getPackageName());

            if ( existing != null ) {
                mergedTargets.put(entry.getPackageName(), mergePackageTarget(existing, entry));
            }
            else {
                mergedTargets.put(entry.getPackageName(), entry);
            }
        }
        return merged;
    }


    /**
     * @param existing
     * @param systemPackageTarget
     * @return
     */
    private static SystemPackageTarget mergePackageTarget ( SystemPackageTarget existing, SystemPackageTarget next ) {
        SystemPackageTarget merged = new SystemPackageTarget();
        merged.setPackageName(existing.getPackageName());
        merged.setSuggestReboot(existing.getSuggestReboot() | next.getSuggestReboot());
        merged.setTargetRepository(next.getTargetRepository() != null ? next.getTargetRepository() : existing.getTargetRepository());
        merged.setTargetVersions(next.getTargetVersions());
        merged.setAfterInstructions(mergeInstructions(existing.getAfterInstructions(), next.getAfterInstructions()));
        merged.setBeforeInstructions(mergeInstructions(existing.getBeforeInstructions(), next.getBeforeInstructions()));
        return null;
    }


    /**
     * @param existing
     * @param next
     * @return
     */
    private static List<ServiceInstruction> mergeInstructions ( List<ServiceInstruction> existing, List<ServiceInstruction> next ) {
        Map<String, ServiceInstruction> existingByService = makeInstructionMap(existing);
        Map<String, ServiceInstruction> nextByService = makeInstructionMap(existing);

        List<ServiceInstruction> inst = new ArrayList<>();

        Set<String> allServices = new HashSet<>(existingByService.keySet());
        allServices.addAll(nextByService.keySet());

        for ( String service : allServices ) {
            ServiceInstruction existingInst = existingByService.get(service);
            ServiceInstruction nextInst = nextByService.get(service);
            if ( existingInst == null && nextInst == null ) {
                continue;
            }
            else if ( nextInst == null || ( existingInst != null && existingInst.supersedes(nextInst) ) ) {
                inst.add(existingInst);
            }
            else {
                inst.add(nextInst);
            }
        }
        return inst;
    }


    /**
     * @param instructions
     * @return
     */
    private static Map<String, ServiceInstruction> makeInstructionMap ( List<ServiceInstruction> instructions ) {
        Map<String, ServiceInstruction> byService = new HashMap<>();
        for ( ServiceInstruction inst : instructions ) {
            ServiceInstruction oldEntry = byService.get(inst.getServiceName());
            if ( oldEntry == null || inst.supersedes(oldEntry) ) {
                byService.put(inst.getServiceName(), inst);
            }
        }
        return byService;
    }
}
