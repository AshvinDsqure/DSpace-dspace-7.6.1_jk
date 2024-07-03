/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dto;

/**
 * Class representing bitstreams stored in the DSpace system.
 * <P>
 * When modifying the bitstream metadata, changes are not reflected in the
 * database until <code>update</code> is called. Note that you cannot alter
 * the contents of a bitstream; you need to create a new bitstream.
 *
 * @author Robert Tansley
 */

public class SubnatureThreeTDTO{

    protected Integer case_type_cd;
    protected Integer nature_cd;
    protected Integer subnature1_cd;
    protected Integer subnature2_cd;
    protected Integer subnature3_cd;
    protected  String subnature3_desc;
    private CiviltDTO civilt;

    public SubnatureThreeTDTO(Integer case_type_cd, Integer nature_cd, Integer subnature1_cd, Integer subnature2_cd, Integer subnature3_cd, String subnature3_desc, CiviltDTO civilt) {
        this.case_type_cd = case_type_cd;
        this.nature_cd = nature_cd;
        this.subnature1_cd = subnature1_cd;
        this.subnature2_cd = subnature2_cd;
        this.subnature3_cd = subnature3_cd;
        this.subnature3_desc = subnature3_desc;
        this.civilt = civilt;
    }

    public Integer getCase_type_cd() {
        return case_type_cd;
    }
    public void setCase_type_cd(Integer case_type_cd) {
        this.case_type_cd = case_type_cd;
    }

    public Integer getNature_cd() {
        return nature_cd;
    }

    public void setNature_cd(Integer nature_cd) {
        this.nature_cd = nature_cd;
    }

    public Integer getSubnature1_cd() {
        return subnature1_cd;
    }

    public void setSubnature1_cd(Integer subnature1_cd) {
        this.subnature1_cd = subnature1_cd;
    }

    public Integer getSubnature2_cd() {
        return subnature2_cd;
    }

    public void setSubnature2_cd(Integer subnature2_cd) {
        this.subnature2_cd = subnature2_cd;
    }

    public Integer getSubnature3_cd() {
        return subnature3_cd;
    }

    public void setSubnature3_cd(Integer subnature3_cd) {
        this.subnature3_cd = subnature3_cd;
    }

    public String getSubnature3_desc() {
        return subnature3_desc;
    }

    public void setSubnature3_desc(String subnature3_desc) {
        this.subnature3_desc = subnature3_desc;
    }

    public CiviltDTO getCivilt() {
        return civilt;
    }

    public void setCivilt(CiviltDTO civilt) {
        this.civilt = civilt;
    }

}
