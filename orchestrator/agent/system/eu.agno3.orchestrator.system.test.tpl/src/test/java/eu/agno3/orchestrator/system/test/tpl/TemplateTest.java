/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.orchestrator.system.test.tpl;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.runtime.util.log.LogWriter;
import eu.agno3.runtime.util.test.TestUtil;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
@SuppressWarnings ( {
    "javadoc", "nls"
} )
public class TemplateTest {

    private static final Logger log = Logger.getLogger(TemplateTest.class);

    private static Semaphore wait = new Semaphore(0);
    private static ComponentContext componentContext;
    private static RunnerFactory runnerFactory;
    private static Configuration tplConfig;


    /**
     * @return the componentContext
     */
    public static ComponentContext getComponentContext () {
        return componentContext;
    }


    @Activate
    protected synchronized void servicesSetUp ( ComponentContext context ) {
        componentContext = context;
        wait.release();
    }


    @Reference
    protected synchronized void setRunnerFactory ( RunnerFactory factory ) {
        runnerFactory = factory;
    }


    protected synchronized void unsetRunnerFactory ( RunnerFactory factory ) {
        runnerFactory = null;
    }


    @Reference
    protected synchronized void setTplConfig ( Configuration config ) {
        tplConfig = config;
    }


    protected synchronized void unsetTplConfig ( Configuration config ) {
        tplConfig = null;
    }


    @BeforeClass
    public static void waitForServices () {
        TestUtil.waitForServices(wait);
    }


    /**
     * @return
     */
    private static LogWriter getLogWriter () {
        return new LogWriter(log, Level.DEBUG);
    }


    @Test
    public void testSetup () {
        assertNotNull(runnerFactory);
        assertNotNull(tplConfig);
    }


    @Test ( expected = IOException.class )
    public void testConfigNonExistant () throws IOException {
        tplConfig.getTemplate("test-nonexistant.ftl");
    }


    @Test
    public void testSimpleTemplate () throws IOException, TemplateException {
        Template t = tplConfig.getTemplate("test-simple.ftl");
        t.process(null, getLogWriter());
    }


    @Test
    public void testSubdirTemplate () throws IOException, TemplateException {
        Template t = tplConfig.getTemplate("subdir/test.ftl");
        t.process(null, getLogWriter());
    }


    @Test
    public void testTemplateBasicMapModel () throws IOException, TemplateException {
        Template t = tplConfig.getTemplate("test-basic-model.ftl");

        Map<String, Object> mapModel = new HashMap<>();
        mapModel.put("string", "This is a string");
        mapModel.put("boolTrue", true);
        mapModel.put("boolFalse", false);
        mapModel.put("integer", 23);
        mapModel.put("floatval", 23.3f);
        mapModel.put("nil", null);
        mapModel.put("list", Arrays.asList("A", "B", "C"));
        mapModel.put("set", new HashSet<>(Arrays.asList("A", "B", "C")));
        mapModel.put("enumVal", Level.FATAL);
        mapModel.put("array", new int[] {
            1, 2, 3, 4, 5, 6
        });

        ByteArrayOutputStream mapOutput = new ByteArrayOutputStream();
        try ( Writer w = new OutputStreamWriter(mapOutput, "UTF-8") ) {
            t.process(mapModel, w);
        }

        Object beanModel = new TestBean();
        ByteArrayOutputStream beanOutput = new ByteArrayOutputStream();

        try ( Writer w = new OutputStreamWriter(beanOutput, "UTF-8") ) {
            t.process(beanModel, w);
        }

        assertTrue(Arrays.equals(mapOutput.toByteArray(), beanOutput.toByteArray()));
    }

    public static class TestBean {

        private String string = "This is a string";
        private boolean boolTrue = true;
        private boolean boolFalse = false;
        private int integer = 23;
        private float floatval = 23.3f;
        private Object nil = null;
        private List<String> list = Arrays.asList("A", "B", "C");
        private Set<String> set = new HashSet<>(Arrays.asList("A", "B", "C"));
        private Level enumVal = Level.FATAL;
        private int[] array = new int[] {
            1, 2, 3, 4, 5, 6
        };


        public String getString () {
            return this.string;
        }


        public boolean isBoolTrue () {
            return this.boolTrue;
        }


        public boolean isBoolFalse () {
            return this.boolFalse;
        }


        public int getInteger () {
            return this.integer;
        }


        /**
         * @return the floatval
         */
        public float getFloatval () {
            return this.floatval;
        }


        public Object getNil () {
            return this.nil;
        }


        public List<String> getList () {
            return this.list;
        }


        public Set<String> getSet () {
            return this.set;
        }


        public Level getEnumVal () {
            return this.enumVal;
        }


        public int[] getArray () {
            return this.array;
        }

    }

}
