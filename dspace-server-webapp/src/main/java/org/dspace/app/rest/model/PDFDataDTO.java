package org.dspace.app.rest.model;

import java.util.ArrayList;
import java.util.List;

public class PDFDataDTO {


    private String fromDate;
    private String todate;
    private String uplodatedBY;
    private String downlodedby;
    private List<String>uplodateDocname=new ArrayList<>();

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getTodate() {
        return todate;
    }

    public void setTodate(String todate) {
        this.todate = todate;
    }

    public String getUplodatedBY() {
        return uplodatedBY;
    }

    public void setUplodatedBY(String uplodatedBY) {
        this.uplodatedBY = uplodatedBY;
    }

    public List<String> getUplodateDocname() {
        return uplodateDocname;
    }

    public void setUplodateDocname(List<String> uplodateDocname) {
        this.uplodateDocname = uplodateDocname;
    }

    public String getDownlodedby() {
        return downlodedby;
    }

    public void setDownlodedby(String downlodedby) {
        this.downlodedby = downlodedby;
    }
}
