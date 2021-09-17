/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.contents;


import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;

import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * @author mbechler
 * 
 */
public class TemplateContentProvider implements ContentProvider {

    /**
     * 
     */
    private static final long serialVersionUID = 9136905788161423024L;

    private static final String FAILED_TO_BUILD_TEMPLATE = "Failed to build template"; //$NON-NLS-1$
    private static final String TEMPLATE_PROCESSING_FAILED = "Template processing failed:"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(TemplateContentProvider.class);

    private Class<?> callerClass;
    private Serializable data;
    private String tplName;

    private transient Template t;
    private transient Charset cs;


    /**
     * @param callerClass
     * @param tplName
     * @param data
     */
    public TemplateContentProvider ( Class<?> callerClass, String tplName, Serializable data ) {
        this.callerClass = callerClass;
        this.tplName = tplName;
        this.data = data;
    }


    private void ensureTemplate ( Context ctx ) throws ExecutionException {
        if ( this.t == null ) {
            try {
                Template tpl = ctx.getConfig().getService(TemplateBuilder.class).buildTemplate(this.callerClass, this.tplName);
                this.cs = Charset.forName(tpl.getEncoding());
                this.t = tpl;
            }
            catch (
                IOException |
                NoSuchServiceException |
                IllegalArgumentException e ) {
                log.warn("Template building failed", e); //$NON-NLS-1$
                throw new ExecutionException(FAILED_TO_BUILD_TEMPLATE, e);
            }
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.file.contents.ContentProvider#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context ctx ) throws ExecutionException {
        this.ensureTemplate(ctx);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.file.contents.ContentProvider#transferTo(eu.agno3.orchestrator.system.base.execution.Context,
     *      java.nio.channels.FileChannel)
     */
    @Override
    public void transferTo ( Context ctx, FileChannel c ) throws IOException {
        try {
            this.ensureTemplate(ctx);
        }
        catch ( ExecutionException e ) {
            throw new IOException(FAILED_TO_BUILD_TEMPLATE, e);
        }
        try ( Writer w = Channels.newWriter(c, this.cs.newEncoder(), -1) ) {
            this.t.process(this.data, w);
        }
        catch ( TemplateException e ) {
            log.warn(TEMPLATE_PROCESSING_FAILED, e);
            throw new IOException(TEMPLATE_PROCESSING_FAILED, e);
        }
    }

}
