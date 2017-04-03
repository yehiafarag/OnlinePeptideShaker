package com.uib.onlinepeptideshaker.model.util;

import com.uib.onlinepeptideshaker.model.GalaxyDataUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents files that is downloaded from the galaxy server like
 * indexes files
 *
 * @author Yehia Farag
 */
public class LocalIndexFile {

    private final File localFile;

    public LocalIndexFile(String localFileId, String folderURL, String fileURL, String cookiesRequestProperty) {

        localFile = new File(folderURL, localFileId);
        if (localFile.exists()) {
            return;
        }

        FileOutputStream fos = null;
        try {
            URL indexFileUrl = new URL(fileURL);
            System.err.println("at file path "+fileURL);
            URLConnection conn = indexFileUrl.openConnection();
            conn.addRequestProperty("Cookie", cookiesRequestProperty);
            conn.addRequestProperty("Accept", "*/*");
            conn.addRequestProperty("Accept-Encoding", "gzip, deflate, sdch, br");
            conn.addRequestProperty("Accept-Language", "ar,en-US;q=0.8,en;q=0.6,en-GB;q=0.4");
            conn.addRequestProperty("Cache-Control", "no-cache");
            conn.addRequestProperty("Connection", "keep-alive");
            conn.addRequestProperty("DNT", "1");
            conn.addRequestProperty("Pragma", "no-cache");
            conn.setDoInput(true);
            InputStream in = conn.getInputStream();
            try (ReadableByteChannel rbc = Channels.newChannel(in)) {
                fos = new FileOutputStream(localFile);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                rbc.close();
                in.close();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public File getLocalFile() {
        return localFile;
    }

}
