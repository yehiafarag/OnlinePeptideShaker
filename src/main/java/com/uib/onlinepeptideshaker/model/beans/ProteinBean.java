package com.uib.onlinepeptideshaker.model.beans;

/**
 * This class represents protein bean across the system
 *
 * @author Yehia Farag
 */
public class ProteinBean {

    /**
     * Leading protein accession
     */
    private String mainAccession;
    /**
     * Leading protein description (name)
     */
    private String description;
    /**
     * Gene name
     */
    private String geneName;
    /**
     * Possible coverage value
     */
    private double possibleCoverage;
    /**
     * Molecular weight in kDa.
     */
    private double MWkDa;

    /**
     * Get leading protein accession
     *
     * @return String accession
     */
    public String getMainAccession() {
        return mainAccession;
    }

    /**
     **Set leading protein accession
     *
     * @param mainAccession leading protein accession
     */
    public void setMainAccession(String mainAccession) {
        this.mainAccession = mainAccession;
    }

    /**
     * Get leading protein description (name)
     *
     * @return String protein description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set leading protein description (name)
     *
     * @param description String protein description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get Gene name
     *
     * @return String gene name
     */
    public String getGeneName() {
        return geneName;
    }

    /**
     * set Gene name
     *
     * @param geneName String gene name
     */
    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    /**
     * Get Possible coverage value
     *
     * @return double possible coverage value
     */
    public double getPossibleCoverage() {
        return possibleCoverage;
    }

    /**
     * Set Possible coverage value
     *
     * @param possibleCoverage double possible coverage value
     */
    public void setPossibleCoverage(double possibleCoverage) {
        this.possibleCoverage = possibleCoverage;
    }

    /**
     *Get  Molecular weight in kDa.
     * @return double  molecular weight in kDa.
     */
    public double getMWkDa() {
        return MWkDa;
    }

    /**
     * Set  Molecular weight in kDa.
     * @param MWkDa double molecular weight in kDa.
     */
    public void setMWkDa(double MWkDa) {
        this.MWkDa = MWkDa;
    }

}
