/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2014 by mbechler
 */
package eu.agno3.runtime.messaging.marshalling.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

/**
 * @author mbechler
 *
 */
class BytesMessageInputStream extends InputStream {

    /**
     * 
     */
    private final BytesMessage m;


    /**
     * @param m
     */
    BytesMessageInputStream ( BytesMessage m ) {
        this.m = m;
    }


    @Override
      public int read () throws IOException {
          try {
              return this.m.readByte();
          }
          catch ( JMSException e ) {
              throw new IOException(e);
          }
      }
}