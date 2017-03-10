package com.uib.onlinepeptideshaker.model.core;

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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is an abstracted class represents implementation for ReadableFile
 * interface
 *
 * @author Yehia Farag
 */
public abstract class ReadableFile {

    /**
     * The server support byte serving.
     */
    private boolean supportByteServing;
    /**
     * The file size in bytes.
     */
    private long fileSize;
    /**
     * The file path in the web (URL).
     */
    private String fileURL;
    /**
     * The file unique id.
     */
    private String fileId;
    /**
     * The path to user folder in the system.
     */
    private String pathToFolder;
    /**
     * The path to the local file in the system.
     */
    private String localFilePath;
    /**
     * The main cookies (optional) as string.
     */
    private String cookiesRequestProperty;

     /**
     * The main  cookies to attach to user request.
     * @param cookiesRequestProperty  cookies as string to be added to requests
     */
    public void setCookiesRequestProperty(String cookiesRequestProperty) {
        this.cookiesRequestProperty = cookiesRequestProperty;
    }

    /**
     ** The Galaxy server support byte Serving (Range header)
     *
     * @param supportByteServing boolean server support byte serving
     */
    public void setSupportByteServing(boolean supportByteServing) {
        this.supportByteServing = supportByteServing;
    }

    /**
     ** The Galaxy server support byte Serving (Range header)
     *
     * @return supportByteServing boolean server support byte serving
     */
    public boolean isSupportByteServing() {
        return supportByteServing;
    }

    /**
     * The total size of the file on Galaxy server
     *
     * @retun fileSize the file size in bytes
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * The web address of the file in Galaxy server
     *
     * @return fileURL String web address of the file location
     */
    public String getFileURL() {
        return fileURL;
    }

    /**
     * The web address of the file in Galaxy server
     *
     * @return fileURL String web address of the file location
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * The the path to the container folder on the server
     *
     * @return pathToFolder String web address of the file location
     */
    public String getPathToFolder() {
        return pathToFolder;
    }

    /**
     * The web address of the file in Galaxy server
     *
     * @param fileURL String web address of the file location
     */
    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    /**
     * The the path to the container folder on the server
     *
     * @param pathToFolder String web address of the file location
     */
    public void setPathToFolder(String pathToFolder) {
        this.pathToFolder = pathToFolder;
    }

    /**
     * The web address of the file in Galaxy server
     *
     * @param fileId String unique file id
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Get Data from start index until the end index
     *
     * @param start start index
     * @param end end index
     * @return Set<Object[]> that can be used for initializing different tables
     */
    public abstract Set<Object[]> getDataFromRange(long start, long end);

    /**
     * Update the file settings to be ready for reading the data.
     */
    public void updateFile() {
        if (isSupportByteServing()) {

        } else {
            File proteinsFile = new File(pathToFolder, fileId);
            localFilePath = proteinsFile.getAbsolutePath();
            if (!proteinsFile.exists()) {
                try {
                    proteinsFile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(ReadableFile.class.getName()).log(Level.SEVERE, null, ex);
                }
                initFile(proteinsFile,fileURL);
            }
        }
    }
    protected String ReadFromLocalFile(long start,long end){
    
    return null;
    }

    
    /**
     * Path to the local file in system.
     */
    protected String getLocalFilePath() {
        return localFilePath;
    }
      private void initFile(File file, String urlResource) {
        FileOutputStream fos = null;
        try {
            URL proteinsFileUrl = new URL(urlResource);
            URLConnection conn = proteinsFileUrl.openConnection();
            conn.addRequestProperty("Cookie", cookiesRequestProperty);
            conn.setDoInput(true);
            InputStream in = conn.getInputStream();
            try (ReadableByteChannel rbc = Channels.newChannel(in)) {
                fos = new FileOutputStream(file);
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
}
