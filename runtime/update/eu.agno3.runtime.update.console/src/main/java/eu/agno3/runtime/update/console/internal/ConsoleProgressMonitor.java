/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.10.2014 by mbechler
 */
package eu.agno3.runtime.update.console.internal;


import org.apache.karaf.shell.api.console.Session;
import org.eclipse.core.runtime.IProgressMonitor;
import org.fusesource.jansi.Ansi;


class ConsoleProgressMonitor implements IProgressMonitor {

    private Session ci;
    private double at;
    private double totalWork;
    private int lastProgressLen = 0;


    /**
     * @param ci
     */
    public ConsoleProgressMonitor ( Session ci ) {
        this.ci = ci;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
     */
    @Override
    public void beginTask ( String t, int weight ) {
        this.totalWork = weight;
        this.ci.getConsole().print(Ansi.ansi().bold().a(t).boldOff().a(':').a("  ")); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
     */
    @Override
    public void worked ( int w ) {
        this.at += w;
        writeProgress();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
     */
    @Override
    public void internalWorked ( double w ) {
        this.at += w;
        writeProgress();
    }


    private void writeProgress () {
        float progress = 100.0f * ( (float) this.at / (float) this.totalWork );
        String progressString = String.format("%.1f%%", progress); //$NON-NLS-1$
        this.ci.getConsole().print(Ansi.ansi().cursorLeft(this.lastProgressLen));
        this.lastProgressLen = progressString.length();
        this.ci.getConsole().print(progressString);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.IProgressMonitor#done()
     */
    @Override
    public void done () {
        if ( ( this.totalWork - this.at ) > 0.01 ) {
            this.at = this.totalWork;
            this.writeProgress();
            this.ci.getConsole().println();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
     */
    @Override
    public boolean isCanceled () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
     */
    @Override
    public void setCanceled ( boolean c ) {}


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
     */
    @Override
    public void setTaskName ( String t ) {}


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
     */
    @Override
    public void subTask ( String t ) {}

}