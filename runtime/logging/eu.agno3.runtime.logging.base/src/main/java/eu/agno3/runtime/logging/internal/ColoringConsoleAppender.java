/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.internal;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.fusesource.jansi.Ansi;
import org.ops4j.pax.logging.spi.PaxLayout;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import eu.agno3.runtime.logging.Appender;


/**
 * @author mbechler
 * 
 */
public class ColoringConsoleAppender implements Appender {

    private final OutputStream logOutput;
    private final PaxLayout layout;
    private final boolean doColor;
    private final Charset cs;


    /**
     * @param layout
     * @param cs
     * @param doColor
     * @param toStderr
     */
    public ColoringConsoleAppender ( PaxLayout layout, Charset cs, boolean doColor, boolean toStderr ) {
        this.layout = layout;
        this.cs = cs;
        this.doColor = doColor;
        if ( toStderr ) {
            this.logOutput = System.err;
        }
        else {
            this.logOutput = System.out;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.ops4j.pax.logging.spi.PaxAppender#doAppend(org.ops4j.pax.logging.spi.PaxLoggingEvent)
     */
    @Override
    public void doAppend ( PaxLoggingEvent ev ) {
        String layouted = this.layout.doLayout(ev);
        if ( !this.doColor ) {
            writePlain(layouted);
            return;
        }
        writeColored(ev, layouted);
    }


    private void writeColored ( PaxLoggingEvent ev, String layouted ) {
        Level l = Level.toLevel(ev.getLevel().toInt());
        Ansi output = new Ansi();
        setStyle(l, output);

        output.a(layouted);
        output.boldOff();
        output.fg(Ansi.Color.DEFAULT);
        output.newline();

        try {
            this.logOutput.write(output.toString().getBytes(this.cs));
        }
        catch ( IOException e ) {
            // cannot log, cannot throw exception
        }
    }


    private void writePlain ( String layouted ) {
        try {
            this.logOutput.write(layouted.getBytes(this.cs));
            this.logOutput.write(System.lineSeparator().getBytes(this.cs));
        }
        catch ( IOException e ) {
            // cannot log, cannot throw exception
        }
    }


    /**
     * @param l
     * @param output
     */
    private static void setStyle ( Level l, Ansi output ) {
        switch ( l.toInt() ) {
        case Priority.FATAL_INT:
        case Priority.ERROR_INT:
            output.bold();
            output.fgBright(Ansi.Color.RED);
            break;
        case Priority.WARN_INT:
            output.fgBright(Ansi.Color.RED);
            break;
        case Priority.INFO_INT:
            output.bold();
            output.fg(Ansi.Color.BLACK);
            break;

        case Priority.ALL_INT:
        case Priority.DEBUG_INT:
            output.fg(Ansi.Color.BLACK);
            break;

        default:
            output.fgBright(Ansi.Color.BLACK);
            break;

        }
    }

}
