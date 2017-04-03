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

    public PeptideFile(String galaxyURL, String galaxyDatasetHistoryID, String cookiesRequestProperty) {
        super(galaxyURL, galaxyDatasetHistoryID, cookiesRequestProperty);
    }
    

    @Override
    public Set<Object[]> getDataFromRange(long start, long end) {
        return null;
    }


}
