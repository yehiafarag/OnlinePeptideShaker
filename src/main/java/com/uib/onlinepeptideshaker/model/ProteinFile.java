package com.uib.onlinepeptideshaker.model;

import com.uib.onlinepeptideshaker.model.core.ReadableFile;
import java.util.LinkedHashSet;
import java.util.Set;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 *
 * @author yfa041
 */
public class ProteinFile extends ReadableFile {

    @Override
    public Set<Object[]> getDataFromRange(long start, long end) {
        return null;
    }

    /**
     * Read the full file at once
     *
     * @return Set<Object[]> full protein table items
     */
    public Set<Object[]> readFullData() {

        Set<Object[]> proteisnSet = new LinkedHashSet<>();
        BufferedRandomAccessFile bufferedRandomAccessFile;
        try {//           
            bufferedRandomAccessFile = new BufferedRandomAccessFile(super.getLocalFilePath(), "r", 1024 * 100);
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
        }
        return proteisnSet;

    }

}
