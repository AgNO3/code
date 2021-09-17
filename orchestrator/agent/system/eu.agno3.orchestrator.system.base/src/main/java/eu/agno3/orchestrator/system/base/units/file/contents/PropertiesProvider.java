/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.contents;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Properties;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;


/**
 * @author mbechler
 *
 */
public class PropertiesProvider implements ContentProvider {

    /**
     * 
     */
    private static final long serialVersionUID = 9115373182528857647L;

    private Properties props;


    /**
     * @param properties
     * 
     */
    public PropertiesProvider ( Properties properties ) {
        this.props = properties;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.file.contents.ContentProvider#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context ctx ) throws ExecutionException {

        for ( Object e : this.props.keySet() ) {
            if ( ! ( e instanceof String ) ) {
                throw new ExecutionException("Non string property key " + e); //$NON-NLS-1$
            }
            Object val = this.props.get(e);
            if ( ! ( val instanceof String ) ) {
                this.props.setProperty((String) e, String.valueOf(val));
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.file.contents.ContentProvider#transferTo(eu.agno3.orchestrator.system.base.execution.Context,
     *      java.nio.channels.FileChannel)
     */
    @Override
    public void transferTo ( Context ctx, FileChannel c ) throws IOException {
        try ( OutputStream os = Channels.newOutputStream(c) ) {
            this.props.store(os, getComment(ctx));
        }
    }


    /**
     * @param ctx
     * @return
     */
    private static String getComment ( Context ctx ) {
        StringBuilder sb = new StringBuilder();
        sb.append("DO NOT EDIT THIS FILE! "); //$NON-NLS-1$
        sb.append("This file was auto-generated by AgNO3 Orchestrator. "); //$NON-NLS-1$
        sb.append("Doing so will void your warranty and might cause future configuration updates to fail."); //$NON-NLS-1$
        return sb.toString();
    }

}
