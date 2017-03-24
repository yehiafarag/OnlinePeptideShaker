package com.uib.onlinepeptideshaker.model;

import com.uib.onlinepeptideshaker.model.core.ReadableFile;
import java.util.LinkedHashSet;
import java.util.Set;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This class represents peptide file utility in the system the class will
 * select best way to handle the interaction with galaxy server based on server
 * capabilities
 *
 * @author Yehia Farag
 */
public class PeptideFile extends ReadableFile {

    @Override
    public Set<Object[]> getDataFromRange(long start, long end) {
        return null;
    }

//    public Set<Object[]> getDataFromRanges(Set<Long> points) {
//        Set<Object[]> peptidesSet = new LinkedHashSet<>();
//        BufferedRandomAccessFile bufferedRandomAccessFile;
//        try {//           
//            bufferedRandomAccessFile = new BufferedRandomAccessFile(super.getLocalFilePath(), "r", 1024 * 100);
//            String line;
//            for (long point : points) {
//                bufferedRandomAccessFile.seek(point);
//                line = bufferedRandomAccessFile.getNextLine();
//                String[] arr = line.split("\\t");
//                 Object[] obj = new Object[]{arr[0], arr[1], arr[4], arr[5], arr[6], arr[13], arr[15]};
//                peptidesSet.add(obj);
//
//            }
//           
//            bufferedRandomAccessFile.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return peptidesSet;
//    }

}
