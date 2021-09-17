/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.progress.ProgressEventImpl;
import eu.agno3.orchestrator.system.packagekit.PackageKitProgressListener;


/**
 * @author mbechler
 *
 */
public class SystemUpdateProgressBridge implements PackageKitProgressListener {

    private Context context;
    private int scale;
    private int offset;


    /**
     * @param context
     * @param offset
     * @param scale
     */
    public SystemUpdateProgressBridge ( Context context, int scale, int offset ) {
        this.context = context;
        this.scale = scale;
        this.offset = offset;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitProgressListener#haveProgress(int, float)
     */
    @Override
    public void haveProgress ( int status, float percent ) {
        float scaled = percent * ( this.scale / 100f ) + this.offset;
        this.context.publishEvent(new ProgressEventImpl(this.context, scaled));
    }
}
