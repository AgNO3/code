/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author mbechler
 * 
 */
public class IOPump {

    private InputPump inputPump;
    private OutputPump outputPump;
    private OutputPump errorPump;

    private OutputHandler stdoutHandler;
    private OutputHandler stderrHandler;
    private InputProvider stdinProvider;


    /**
     * @param stdinProvider
     * @param stdoutHandler
     * @param stderrHandler
     */
    public IOPump ( InputProvider stdinProvider, OutputHandler stdoutHandler, OutputHandler stderrHandler ) {
        this.stdinProvider = stdinProvider;
        this.stdoutHandler = stdoutHandler;
        this.stderrHandler = stderrHandler;
    }


    /**
     * @param p
     */
    public void connect ( Process p ) {
        this.outputPump = startStreamPump("STDOUT", p.getInputStream(), this.stdoutHandler); //$NON-NLS-1$
        this.errorPump = startStreamPump("STDERR", p.getErrorStream(), this.stderrHandler); //$NON-NLS-1$
        this.inputPump = startStreamPump(p.getOutputStream(), this.stdinProvider);
    }


    /**
     * @param stdin
     * @return
     */
    private static InputPump startStreamPump ( OutputStream stdin, InputProvider h ) {
        InputPump p = new InputPump(stdin, h);
        p.start();
        return p;
    }


    /**
     * @param stdout
     * @return
     */
    private static OutputPump startStreamPump ( String streamName, InputStream stream, OutputHandler h ) {
        OutputPump p = new OutputPump(streamName, stream, h);
        p.start();
        return p;
    }


    /**
     * @param force
     * 
     */
    public void shutdown ( boolean force ) {
        if ( this.inputPump != null ) {
            this.inputPump.shutdown();
        }
        if ( this.outputPump != null ) {
            this.outputPump.shutdown(force);
        }
        if ( this.errorPump != null ) {
            this.errorPump.shutdown(force);
        }
    }
}
