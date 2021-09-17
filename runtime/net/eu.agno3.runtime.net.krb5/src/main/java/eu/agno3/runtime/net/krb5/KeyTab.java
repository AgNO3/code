/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class KeyTab {

    private int formatVersion;
    private List<KeyTabEntry> entries = new ArrayList<>();


    /**
     * @return the entries
     */
    public List<KeyTabEntry> getEntries () {
        return this.entries;
    }


    /**
     * @param os
     * @throws IOException
     */
    public void write ( OutputStream os ) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeShort(0x502);
        for ( KeyTabEntry entry : this.entries ) {
            entry.write(dos);
        }
    }


    /**
     * @param is
     * @return the paresed keytab
     * @throws IOException
     */
    public static KeyTab parse ( InputStream is ) throws IOException {
        KeyTab kt = new KeyTab();
        DataInputStream dis = new DataInputStream(is);

        kt.formatVersion = dis.readUnsignedShort();

        if ( kt.formatVersion != 0x502 ) {
            throw new IOException("Keytab version is not supported " + kt.formatVersion); //$NON-NLS-1$
        }

        kt.entries = new LinkedList<>();
        while ( true ) {
            KeyTabEntry e = KeyTabEntry.parse(dis);
            if ( e == null ) {
                break;
            }
            kt.entries.add(e);
        }

        return kt;
    }

}
