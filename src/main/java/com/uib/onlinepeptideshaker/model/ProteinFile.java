package com.uib.onlinepeptideshaker.model;

import com.uib.onlinepeptideshaker.model.core.ReadableFile;
import com.uib.onlinepeptideshaker.model.util.LocalIndexFile;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This class represents protein file utility in the system the class will
 * select best way to handle the interaction with galaxy server based on server
 * capabilities
 *
 * @author Yehia Farag
 */
public class ProteinFile extends LocalIndexFile {

    public ProteinFile(String localFileId, String folderURL, String fileURL, String cookiesRequestProperty) {
        super(localFileId, folderURL, fileURL, cookiesRequestProperty);
    }

   

    /**
     * Read the full file at once
     *
     * @return Set<Object[]> full protein table items
     */
    public Set<Object[]> readFullData() {

        Set<Object[]> proteisnSet = new LinkedHashSet<>();
        BufferedRandomAccessFile bufferedRandomAccessFile=null;
        try {//           
            bufferedRandomAccessFile = new BufferedRandomAccessFile(super.getLocalFile(), "r", 1024 * 100);
            String line;
            /**
             * escape header
             */
            bufferedRandomAccessFile.getNextLine();
            while ((line = bufferedRandomAccessFile.getNextLine()) != null) {
                String[] arr = line.split("\\t");
                Object[] obj = new Object[]{arr[0], arr[1], arr[2], arr[3], arr[5], arr[6], arr[17]};
                proteisnSet.add(obj);
            }
            bufferedRandomAccessFile.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (bufferedRandomAccessFile != null) {
                try {
                    bufferedRandomAccessFile.close(
                    );
                } catch (IOException ex1) {
                    Logger.getLogger(ProteinFile.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        return proteisnSet;

    }

}
