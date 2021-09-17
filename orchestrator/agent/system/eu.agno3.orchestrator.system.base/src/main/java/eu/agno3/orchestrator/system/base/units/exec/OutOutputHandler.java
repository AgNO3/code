/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import eu.agno3.orchestrator.system.base.execution.output.Out;


/**
 * @author mbechler
 * 
 */
class OutOutputHandler extends AbstractLineBufferingOutputHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -6498164806077831945L;

    private transient Out out;
    private boolean error;


    /**
     * 
     * @param out
     * @param error
     */
    public OutOutputHandler ( Out out, boolean error ) {
        this.out = out;
        this.error = error;

    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.exec.AbstractCharsetDecodingOutputHandler#output(java.nio.CharBuffer)
     */
    @Override
    protected void outputLine ( String line ) {
        if ( this.out == null ) {
            return;
        }

        if ( this.error ) {
            this.out.error(line);
        }
        else {
            this.out.info(line);
        }
    }

}
