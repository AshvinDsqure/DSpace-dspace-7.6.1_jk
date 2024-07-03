package org.dspace.content.cis.dto;

public class ExtraadvtDTO {
    protected Integer srno;

    protected String adv_name;
    protected String case_no;
    protected Short type;

    public ExtraadvtDTO(Integer srno, String adv_name,Short type) {
        this.srno = srno;
        this.adv_name = adv_name;
        this.type = type;
    }

    public Integer getSrno() {
        return srno;
    }

    public void setSrno(Integer srno) {
        this.srno = srno;
    }



    public String getAdv_name() {
        return adv_name;
    }

    public void setAdv_name(String adv_name) {
        this.adv_name = adv_name;
    }



    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }
}
