package com.uib.onlinepeptideshaker.model.core;

import com.uib.onlinepeptideshaker.model.beans.IndexPoint;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * This is an abstracted class represents implementation for ReadableFile
 * interface
 *
 * @author Yehia Farag
 */
public abstract class ReadableFile {

    /**
     * Key word for splitting data chunk or offset.
     */
    private String chunkedData;
    /**
     * Re-index the location based on the server type.
     */
    private Long reIndexFactor;
    /**
     * The main cookies (optional) as string.
     */
    private final String cookiesRequestProperty;
    /**
     * The file path in the web (URL).
     */
    private String fileURL;

    /**
     * Update the file settings to be ready for reading the data.
     *
     * @param galaxyURL In use Galaxy server URL
     * @param cookiesRequestProperty the required cookies if exist
     * @param galaxyDatasetHistoryID the requested dataset id
     */
    public ReadableFile(String galaxyURL, String galaxyDatasetHistoryID, String cookiesRequestProperty) {
        this.cookiesRequestProperty = cookiesRequestProperty;
        String url = galaxyURL + "/dataset/display?dataset_id=" + galaxyDatasetHistoryID + "&chunk=1";
        URLConnection conn = initializeConnection(url);
        if (conn == null) {
            return;
        }
        if (conn.getHeaderField("Transfer-Encoding") != null && conn.getHeaderField("Transfer-Encoding").equalsIgnoreCase("chunked")) {
            try {
                chunkedData = "&chunk=";
                InputStream in = conn.getInputStream();
                reIndexFactor = in.skip(Long.MAX_VALUE);
            } catch (IOException ex) {
                Logger.getLogger(ReadableFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            chunkedData = "&offset=";
            reIndexFactor = 1l;
        }
        fileURL = galaxyURL + "/dataset/display?dataset_id=" + galaxyDatasetHistoryID + chunkedData;

    }

    private URLConnection initializeConnection(String url) {
        try {
            URL website = new URL(url);
            URLConnection conn = website.openConnection();
            conn.addRequestProperty("Cookie", cookiesRequestProperty);
            conn.addRequestProperty("Accept", "*/*");
            conn.addRequestProperty("Accept-Encoding", "gzip, deflate, sdch, br");
            conn.addRequestProperty("Accept-Language", "ar,en-US;q=0.8,en;q=0.6,en-GB;q=0.4");
            conn.addRequestProperty("Cache-Control", "no-cache");
            conn.addRequestProperty("Connection", "keep-alive");
            conn.addRequestProperty("DNT", "1");
            conn.addRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.addRequestProperty("Pragma", "no-cache");
            conn.setDoInput(true);
            return conn;
        } catch (MalformedURLException ex) {
            Logger.getLogger(ReadableFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReadableFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Get Data from start index until the end index
     *
     * @param points set of selected points
     * @return Set<String> that can be used for initializing different tables
     */
    public Set<String> getDataAsLines(Set<IndexPoint> points) {
        Set<String> dataLines = new LinkedHashSet<>();
//        System.out.println("at get data from peptides");
        //if offset
        if (chunkedData.equalsIgnoreCase("&offset=")) {
            try {
                InputStream in = null;
                long startPonter = 0;
                String jsonStr;
                for (IndexPoint p : points) {
                    byte[] bytesToRead;
                    URLConnection conn = (URLConnection) initializeConnection(fileURL + p.getStartPoint());
                    in = conn.getInputStream();
                    bytesToRead = new byte[(p.getLength() + 30)];
                    startPonter = p.getStartPoint() + in.read(bytesToRead);
                    jsonStr = new String(bytesToRead) + "\"}";//                      
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    dataLines.add(jsonObject.getString("ck_data"));

                }

            } catch (IOException ex) {
                ex.printStackTrace();
                Logger.getLogger(ReadableFile.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                Logger.getLogger(ReadableFile.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println("is a chunked data");
        }
        //if chunked

        return dataLines;

    }

    /**
     * Get Data from start index until the end index
     *
     * @param startRange start index
     * @param endRange last index
     * @return Set<String> that can be used for initializing different tables
     */
    public Set<String> getDataAsLines(long startRange, String endRangeKeyword) {
        Set<String> dataLines = new LinkedHashSet<>();
//        System.out.println("at get data from peptides");
        //if offset
        if (chunkedData.equalsIgnoreCase("&offset=")) {
            try {
                String jsonStr;
                final long startTime = System.nanoTime();
                URLConnection conn = (URLConnection) initializeConnection(fileURL + startRange);
                InputStream in = conn.getInputStream();
                BufferedReader bin = new BufferedReader(new InputStreamReader(in));
                String line = bin.readLine().split(endRangeKeyword)[0];

                jsonStr = line + "\"}";
                JSONObject jsonObject = new JSONObject(jsonStr);
//                System.out.println("at -- "+ jsonObject.getString("ck_data"));
                final long endTime = System.nanoTime();
                dataLines.addAll(Arrays.asList(jsonObject.getString("ck_data").split("\\+")[1].replaceFirst("\\n", "").split("\n")));
                System.out.println("method 1 : " + (endTime - startTime));

            } catch (IOException ex) {
                ex.printStackTrace();
                Logger.getLogger(ReadableFile.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                Logger.getLogger(ReadableFile.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println("is a chunked data");
        }
        //if chunked

        return dataLines;

    }

    private int checkIndexCover(InputStream in, Iterator<IndexPoint> itr) {

        return -1;
    }

    /**
     * Get Data from start index until the end index
     *
     * @param start start index
     * @param end end index
     * @return Set<Object[]> that can be used for initializing different tables
     */
    public abstract Set<Object[]> getDataFromRange(long start, long end);

//    /**
//     * Skip bytes in file is accepted.
//     */
//    private boolean supportByteSkipping;
//
//    /**
//     * The server support byte serving.
//     */
//    private boolean supportByteServing;
//
//    /**
//     * Skip bytes in file is accepted.
//     *
//     * @return boolean skip bytes supported for such file
//     */
//    public boolean isSupportByteSkipping() {
//        return supportByteSkipping;
//    }
//    /**
//     * The file size in bytes.
//     */
//    private long fileSize = Long.MAX_VALUE;
//
//    /**
//     * The file size in bytes.
//     *
//     * @param fileSize
//     */
//    public void setFileSize(long fileSize) {
//        this.fileSize = fileSize;
//    }
//
//    /**
//     * Key word for splitting data chunk or offset..
//     *
//     * @param chunkedData String keyword (chunk or offset)
//     */
//    public void setChunkedData(String chunkedData) {
//        this.chunkedData = chunkedData;
//    }
//
////    /**
////     * Re-index the requested file location based on the server type.
////     *
////     * @param reIndexFactor 1 in case of offset and size of chunk in case of using chunk keyword
////     */
////    public void setReIndexFactor(int reIndexFactor) {
////        this.reIndexFactor = reIndexFactor;
////    }
//    /**
//     * The file unique id.
//     */
//    private String fileId;
//    /**
//     * The path to user folder in the system.
//     */
//    private String pathToFolder;
//    /**
//     * The path to the local file in the system.
//     */
//    private String localFilePath;
//
////    /**
////     * The main cookies to attach to user request.
////     *
////     * @param cookiesRequestProperty cookies as string to be added to requests
////     */
////    public void setCookiesRequestProperty(String cookiesRequestProperty) {
////        this.cookiesRequestProperty = cookiesRequestProperty;
////    }
//    /**
//     ** The Galaxy server support byte Serving (Range header)
//     *
//     * @param supportByteServing boolean server support byte serving
//     */
//    public void setSupportByteServing(boolean supportByteServing) {
//        this.supportByteServing = supportByteServing;
//    }
//
//    /**
//     ** The Galaxy server support byte Serving (Range header)
//     *
//     * @return supportByteServing boolean server support byte serving
//     */
//    public boolean isSupportByteServing() {
//        return supportByteServing;
//    }
//
//    /**
//     * The total size of the file on Galaxy server
//     *
//     * @return
//     * @retun fileSize the file size in bytes
//     */
//    public long getFileSize() {
//        return fileSize;
//    }
//
//    /**
//     * The web address of the file in Galaxy server
//     *
//     * @return fileURL String web address of the file location
//     */
//    public String getFileURL() {
//        return fileURL;
//    }
//
//    /**
//     * The web address of the file in Galaxy server
//     *
//     * @return fileURL String web address of the file location
//     */
//    public String getFileId() {
//        return fileId;
//    }
//
//    /**
//     * The the path to the container folder on the server
//     *
//     * @return pathToFolder String web address of the file location
//     */
//    public String getPathToFolder() {
//        return pathToFolder;
//    }
//
//    /**
//     * The web address of the file in Galaxy server
//     *
//     * @param fileURL String web address of the file location
//     */
//    public void setFileURL(String fileURL) {
//        this.fileURL = fileURL;
//    }
//
//    /**
//     * The the path to the container folder on the server
//     *
//     * @param pathToFolder String web address of the file location
//     */
//    public void setPathToFolder(String pathToFolder) {
//        this.pathToFolder = pathToFolder;
//    }
//
//    /**
//     * The web address of the file in Galaxy server
//     *
//     * @param fileId String unique file id
//     */
//    public void setFileId(String fileId) {
//        this.fileId = fileId;
//    }
//
//
//    
//
////
////    ;
//    /**
//     * Read bytes from server (case of byte serving support)
//     *
//     * @param start start pointer index
//     * @param end last index for the requested data
//     * @return the data as string
//     */
//    protected Set<String> readRemoteOffsetBytes(Set<IndexPoint> points) {
//        BufferedReader br = null;
//        Set<String> dataLines = new LinkedHashSet<>();
//
//        try {
//
//            long pointer = 0;
//            for (IndexPoint point : points) {
//                URL proteinsFileUrl = new URL(fileURL + point.getStartPoint());
//                System.err.println("at peptide url " + fileURL + point.getStartPoint());
//                URLConnection conn = proteinsFileUrl.openConnection();
//                conn.addRequestProperty("Cookie", cookiesRequestProperty);
//                conn.addRequestProperty("Accept", "*/*");
//                conn.addRequestProperty("Accept-Encoding", "gzip, deflate, sdch, br");
//                conn.addRequestProperty("Accept-Language", "ar,en-US;q=0.8,en;q=0.6,en-GB;q=0.4");
//                conn.addRequestProperty("Cache-Control", "no-cache");
//                conn.addRequestProperty("Connection", "keep-alive");
//                conn.addRequestProperty("DNT", "1");
//                conn.addRequestProperty("X-Requested-With", "XMLHttpRequest");
//                conn.addRequestProperty("Pragma", "no-cache");
//                conn.setDoInput(true);
//                InputStream inputStream = conn.getInputStream();
//                byte[] data = new byte[point.getLength()];
//                inputStream.read(data);
////                char c;
////                String s = "";
////                do {
////                    c = (char) inputStream.read();
////                    if (c == '\n') {
////                        break;
////                    }
////                    s += c + "";
////                } while (c != -1 || s.length()==100);
//
////                dataLines.add(s.trim());
//                inputStream.close();
//                System.out.println("at peptide line " + new String(data));
//
//            }
//
////            System.out.println("at read line " + new String(data));
//        } catch (MalformedURLException ex) {
//            ex.printStackTrace();
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + e.getLocalizedMessage());
//                }
//            }
//        }
//        return dataLines;
//    }
//
//    /**
//     * Read bytes from server (case of byte serving support)
//     *
//     * @param start start pointer index
//     * @param end last index for the requested data
//     * @return the data as string
//     */
//    protected Set<String> readRemoteServerBytes(Set<IndexPoint> points) {
//        BufferedReader br = null;
//        Set<String> dataLines = new LinkedHashSet<>();
//
//        try {
//            URL proteinsFileUrl = new URL(fileURL);
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("https://test-fe.cbu.uib.no/", 80));
//            try {
//                List<Proxy> l = ProxySelector.getDefault().select(new URI("https://test-fe.cbu.uib.no"));
//                for (Proxy p : l) {
//                    System.out.println("at proxis " + p.address());
//                }
//            } catch (URISyntaxException ex) {
//                Logger.getLogger(ReadableFile.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            URLConnection conn = proteinsFileUrl.openConnection();
//            conn.addRequestProperty("Cookie", cookiesRequestProperty);
//            conn.addRequestProperty("Content-Type", "multipart/byteranges;");
//
//            long pointer = 0;
//            String rangeValue = "bytes=";
//
//            for (IndexPoint point : points) {
////                len += point.getLength();
//                rangeValue += point.getStartPoint() + "-" + (point.getStartPoint() + point.getLength()) + " ";
//            }
//            rangeValue = rangeValue.trim().replace(" ", ",");
//
//            conn.addRequestProperty("Range", rangeValue);
//            conn.setDoInput(true);
//            InputStream inputStream = conn.getInputStream();
//            br = new BufferedReader(new InputStreamReader(inputStream));
//            int len = Integer.parseInt(conn.getHeaderField("Content-Length"));
////            System.out.println("at Range is " + len+"   length "+ conn.getHeaderField("Content-Length"));
//
//            char[] data = new char[len];
//            br.read(data);
//            String line = new String(data);
//            String regex;
//            if (line.contains("Content-Range:")) {
//                regex = line.split("Content-Range:")[1].split("\n")[0].split("/")[1];
//                String[] dataArr = line.split(regex);
//                for (String str : dataArr) {
//                    String finalLine = str.split("--")[0].trim();
//                    if (finalLine.length() == 0) {
//                        continue;
//                    }
//                    dataLines.add(finalLine);
//                }
//            } else {
//                dataLines.add(line.trim());
//            }
//            br.close();
//            inputStream.close();
//
////            System.out.println("at read line " + new String(data));
//        } catch (MalformedURLException ex) {
//            ex.printStackTrace();
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + e.getLocalizedMessage());
//                }
//            }
//        }
//        return dataLines;
//    }
//
//    /**
//     * Read bytes from server (case of byte serving support)
//     *
//     * @param start start pointer index
//     * @param end last index for the requested data
//     * @return the data as string
//     */
//    protected Set<String> readLocalFileBytes(Set<IndexPoint> points) {
//        Set<String> linesSet = new LinkedHashSet<>();
//        BufferedRandomAccessFile bufferedRandomAccessFile;
//        try {//           
//            bufferedRandomAccessFile = new BufferedRandomAccessFile(getLocalFilePath(), "r", 1024 * 100);
//            String line;
//            for (IndexPoint point : points) {
//                bufferedRandomAccessFile.seek(point.getStartPoint());
//                byte[] data = new byte[point.getLength()];
//                bufferedRandomAccessFile.read(data);
//                line = new String(data);
//                linesSet.add(line);
//
//            }
//
//            bufferedRandomAccessFile.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return linesSet;
//
//    }
//
//    /**
//     * Read bytes from server (case of byte serving support)
//     *
//     * @param start start pointer index
//     * @param end last index for the requested data
//     * @return the data as string
//     *
//     */
//    private Set<String> skipReadServerBytes(Set<IndexPoint> points) {
//        BufferedReader br = null;
//        Set<String> dataLines = new LinkedHashSet<>();
//
//        try {
//            URL proteinsFileUrl = new URL(fileURL);
//            URLConnection conn = proteinsFileUrl.openConnection();
//            conn.addRequestProperty("Cookie", cookiesRequestProperty);
//            conn.setDoInput(true);
//            InputStream inputStream = conn.getInputStream();
//            long pointer = 0;
//            br = new BufferedReader(new InputStreamReader(inputStream));
//            for (IndexPoint point : points) {
//                br.skip(point.getStartPoint() - pointer);
//                char[] data = new char[point.getLength()];
//                br.read(data);
//                dataLines.add(new String(data));
//                pointer = point.getStartPoint() + point.getLength();
//
//            }
//
//        } catch (MalformedURLException ex) {
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } catch (FileNotFoundException ex) {
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } catch (IOException ex) {
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + e.getLocalizedMessage());
//                }
//            }
//        }
//        return dataLines;
//
//    }
//
//    /**
//     * Path to the local file in system.
//     *
//     * @return
//     */
//    protected String getLocalFilePath() {
//        return localFilePath;
//    }
//
//    private void initFile(File file, String urlResource) {
//        FileOutputStream fos = null;
//        try {
////            new Proxy(Proxy.Type.HTTP, new InetSocketAddress("test-fe.cbu.uib.no", 3128))
//            System.out.println("at updated headers method");
//            URL proteinsFileUrl = new URL(urlResource);
//            URLConnection conn = proteinsFileUrl.openConnection();
//            conn.addRequestProperty("Cookie", cookiesRequestProperty);
//
//            conn.addRequestProperty("Accept", "*/*");
//            conn.addRequestProperty("Accept-Encoding", "gzip, deflate, sdch, br");
//            conn.addRequestProperty("Accept-Language", "ar,en-US;q=0.8,en;q=0.6,en-GB;q=0.4");
//            conn.addRequestProperty("Cache-Control", "no-cache");
//            conn.addRequestProperty("Range", "bytes=1000-2000");
//            conn.addRequestProperty("Connection", "keep-alive");
//            conn.addRequestProperty("DNT", "1");
////            conn.addRequestProperty("Host", "test-fe.cbu.uib.no");
//            conn.addRequestProperty("Pragma", "no-cache");
////            conn.addRequestProperty("Referer", "https://test-fe.cbu.uib.no/galaxy/");
////            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
//
//            conn.setDoInput(true);
//            InputStream in = conn.getInputStream();
////            System.out.println("at update heders " + conn.getHeaderFields());
//            try (ReadableByteChannel rbc = Channels.newChannel(in)) {
//
//                fos = new FileOutputStream(file);
//                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//                fos.close();
//                rbc.close();
//                in.close();
//
//            } catch (MalformedURLException ex) {
//                ex.printStackTrace();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            } finally {
//                if (fos != null) {
//                    try {
//                        fos.close();
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//
//            }
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
