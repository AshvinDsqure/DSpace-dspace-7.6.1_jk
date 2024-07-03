package org.dspace.content.cis.dto;

public class FirNoAndFirYearAndDistrictAndPoliceNameDTO {


    private  String fir_no;

    private  String fir_year;
    private  String dist_name;
    private  String police_st_name;

    public String getFir_no() {
        return fir_no;
    }

    public void setFir_no(String fir_no) {
        this.fir_no = fir_no;
    }

    public String getFir_year() {
        return fir_year;
    }

    public void setFir_year(String fir_year) {
        this.fir_year = fir_year;
    }

    public String getDist_name() {
        return dist_name;
    }

    public void setDist_name(String dist_name) {
        this.dist_name = dist_name;
    }

    public String getPolice_st_name() {
        return police_st_name;
    }

    public void setPolice_st_name(String police_st_name) {
        this.police_st_name = police_st_name;
    }
}
