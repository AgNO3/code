/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class SystemPackageTarget implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6561597293391847225L;
    private String packageName;
    private List<String> targetVersions;
    private String targetRepository;

    private List<ServiceInstruction> beforeInstructions = new ArrayList<>();
    private List<ServiceInstruction> afterInstructions = new ArrayList<>();

    private Boolean suggestReboot;


    /**
     * @return the packageName
     */
    public String getPackageName () {
        return this.packageName;
    }


    /**
     * @param packageName
     *            the packageName to set
     */
    public void setPackageName ( String packageName ) {
        this.packageName = packageName;
    }


    /**
     * @return the targetRepository
     */
    public String getTargetRepository () {
        return this.targetRepository;
    }


    /**
     * @param targetRepository
     *            the targetRepository to set
     */
    public void setTargetRepository ( String targetRepository ) {
        this.targetRepository = targetRepository;
    }


    /**
     * @return the targetVersion
     */
    public List<String> getTargetVersions () {
        return this.targetVersions;
    }


    /**
     * @param targetVersion
     *            the targetVersion to set
     */
    public void setTargetVersions ( List<String> targetVersion ) {
        this.targetVersions = targetVersion;
    }


    /**
     * @return the beforeInstructions
     */
    public List<ServiceInstruction> getBeforeInstructions () {
        return this.beforeInstructions;
    }


    /**
     * @param beforeInstructions
     *            the beforeInstructions to set
     */
    public void setBeforeInstructions ( List<ServiceInstruction> beforeInstructions ) {
        this.beforeInstructions = beforeInstructions;
    }


    /**
     * @return the afterInstructions
     */
    public List<ServiceInstruction> getAfterInstructions () {
        return this.afterInstructions;
    }


    /**
     * @param afterInstructions
     *            the afterInstructions to set
     */
    public void setAfterInstructions ( List<ServiceInstruction> afterInstructions ) {
        this.afterInstructions = afterInstructions;
    }


    /**
     * @return the suggestReboot
     */
    public Boolean getSuggestReboot () {
        return this.suggestReboot;
    }


    /**
     * @param suggestReboot
     *            the suggestReboot to set
     */
    public void setSuggestReboot ( Boolean suggestReboot ) {
        this.suggestReboot = suggestReboot;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("%s-%s", this.getPackageName(), this.getTargetVersions()); //$NON-NLS-1$
    }
}
