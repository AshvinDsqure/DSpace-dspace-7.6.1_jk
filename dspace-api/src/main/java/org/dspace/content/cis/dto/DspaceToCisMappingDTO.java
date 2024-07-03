/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dto;

import java.util.Date;
import java.util.UUID;

/**
 * Class representing bitstreams stored in the DSpace system.
 * <P>
 * When modifying the bitstream metadata, changes are not reflected in the
 * database until <code>update</code> is called. Note that you cannot alter
 * the contents of a bitstream; you need to create a new bitstream.
 *
 * @author Robert Tansley
 */

public class DspaceToCisMappingDTO{
    protected String cino;

    protected  UUID dspaceobjectid;
    protected Integer cisobjecttype;
    protected String ia_no;
    protected Integer ia_case_type;
    protected Integer purpose_cd;
    protected Boolean isrelationship;
    protected String ref_case_no;
    protected String case_no;
    protected String main_case_no;
    protected Integer causelist_type;
    protected Date causelist_date;
    protected Date date;
    protected Integer for_bench_id;
    protected Integer causelist_sr_no;
    protected Integer unique_no;

    public UUID getDspaceobjectid() {
        return dspaceobjectid;
    }

    public void setDspaceobjectid(UUID dspaceobjectid) {
        this.dspaceobjectid = dspaceobjectid;
    }

    public Integer getCisobjecttype() {
        return cisobjecttype;
    }

    public void setCisobjecttype(Integer cisobjecttype) {
        this.cisobjecttype = cisobjecttype;
    }

    public String getIa_no() {
        return ia_no;
    }

    public void setIa_no(String ia_no) {
        this.ia_no = ia_no;
    }

    public Integer getIa_case_type() {
        return ia_case_type;
    }

    public void setIa_case_type(Integer ia_case_type) {
        this.ia_case_type = ia_case_type;
    }

    public Integer getPurpose_cd() {
        return purpose_cd;
    }

    public void setPurpose_cd(Integer purpose_cd) {
        this.purpose_cd = purpose_cd;
    }

    public Boolean getIsrelationship() {
        return isrelationship;
    }

    public void setIsrelationship(Boolean isrelationship) {
        this.isrelationship = isrelationship;
    }

    public String getRef_case_no() {
        return ref_case_no;
    }

    public void setRef_case_no(String ref_case_no) {
        this.ref_case_no = ref_case_no;
    }

    public String getCase_no() {
        return case_no;
    }

    public void setCase_no(String case_no) {
        this.case_no = case_no;
    }

    public String getMain_case_no() {
        return main_case_no;
    }

    public void setMain_case_no(String main_case_no) {
        this.main_case_no = main_case_no;
    }

    public Integer getCauselist_type() {
        return causelist_type;
    }

    public void setCauselist_type(Integer causelist_type) {
        this.causelist_type = causelist_type;
    }

    public Date getCauselist_date() {
        return causelist_date;
    }

    public void setCauselist_date(Date causelist_date) {
        this.causelist_date = causelist_date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getFor_bench_id() {
        return for_bench_id;
    }

    public void setFor_bench_id(Integer for_bench_id) {
        this.for_bench_id = for_bench_id;
    }

    public Integer getCauselist_sr_no() {
        return causelist_sr_no;
    }

    public void setCauselist_sr_no(Integer causelist_sr_no) {
        this.causelist_sr_no = causelist_sr_no;
    }

    public Integer getUnique_no() {
        return unique_no;
    }

    public void setUnique_no(Integer unique_no) {
        this.unique_no = unique_no;
    }

    public String getCino() {
        return cino;
    }

    public void setCino(String cino) {
        this.cino = cino;
    }

}
