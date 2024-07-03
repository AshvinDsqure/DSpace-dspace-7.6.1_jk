/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis;

import org.dspace.content.DSpaceObjectCIS;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
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
@Entity
@Table(name = "civil_t_a",schema = "public")
public class Civilta extends  DSpaceObjectCIS{
    @Id
    @Column(columnDefinition = "bpchar",name="cino",length = 4,unique = true, nullable = false, insertable = true, updatable = false)
    protected String cino;
    @Column(name="reg_no")
    protected Integer reg_no;
    @Column(name="reg_year",columnDefinition = "smallint")
    protected Short reg_year;
    @Column(name="pet_name",columnDefinition = "bpchar")
    protected String pet_name;
    @Column(name="res_name",columnDefinition = "bpchar")
    protected String res_name;
    @Column(name="pet_adv",columnDefinition = "bpchar")
    protected String pet_adv;
    @Column(name="res_adv" ,columnDefinition = "bpchar")
    protected String res_adv;
    @Column(name="dt_regis")
    @Temporal(TemporalType.DATE)
    protected Date dt_regis;
    @Column(name="date_of_filing")
    @Temporal(TemporalType.DATE)
    protected Date date_of_filing;
    @Column(name="date_of_decision")
    @Temporal(TemporalType.DATE)
    protected Date date_of_decision;
    @Column(name="jocode",columnDefinition = "bpchar")
    protected String jocode;
    @Column(name="case_no",columnDefinition = "bpchar")
    protected String case_no;
    @Column(name="pet_age",columnDefinition = "smallint")
    protected Short pet_age=0;
    @Column(name="res_age",columnDefinition = "smallint")
    protected Short res_age=0;
    @OneToOne(fetch = FetchType.LAZY,cascade = {CascadeType.ALL})
    @JoinColumn(name = "regcase_type", referencedColumnName = "case_type")
    private CaseType caseType = null;
    @OneToOne(fetch = FetchType.LAZY,cascade = {CascadeType.ALL})
    @JoinColumn(name = "branch_id", referencedColumnName = "court_no")
    private Courtt courtt = null;

    @OneToMany(mappedBy = "civilt", cascade = CascadeType.ALL)
    @Where(clause = "linkcino != cino and  disposed = 'N'" )
    private List<Connectedt> civilt2connected;
    @OneToMany(mappedBy = "civilt", cascade = CascadeType.ALL)
    private List<Extraadvt> extraadvts;

    @Column(name="orgid",columnDefinition = "smallint")
    protected Short orgid=0;
    @Column(name="resorgid",columnDefinition = "smallint")
    protected Short resorgid=0;
    @Column(name="nature_cd",columnDefinition = "bpchar")
    protected String nature_cd;
   /* @Column(name="newregcase_type",columnDefinition = "smallint")
    protected Short newregcase_type;
    @Column(name="newreg_no")
    protected Integer newreg_no;
    @Column(name="newreg_year",columnDefinition = "smallint")
    protected Short newreg_year;*/

    @Column(name="cs_subject")
    protected Integer cs_subject;
    @Column(name="c_subject")
    protected Integer c_subject;
    @Column(name="css_subject")
    protected Integer css_subject;
    @Column(name="csss_subject")
    protected Integer csss_subject;

    @Column(name="case_remark")
    protected String case_remark;
    @Column(name="main_case_no", columnDefinition = "bpchar")
    protected String main_case_no;



    public Civilta() {
    }

    public String getCino() {
        return cino;
    }
    public void setCino(String cino) {
        this.cino = cino;
    }


    public Integer getReg_no() {
        return reg_no;
    }

    public void setReg_no(Integer reg_no) {
        this.reg_no = reg_no;
    }

    public Short getReg_year() {
        return reg_year;
    }

    public void setReg_year(Short reg_year) {
        this.reg_year = reg_year;
    }

    public String getPet_name() {
        return pet_name;
    }

    public void setPet_name(String pet_name) {
        this.pet_name = pet_name;
    }

    public String getRes_name() {
        return res_name;
    }

    public void setRes_name(String res_name) {
        this.res_name = res_name;
    }

    public String getPet_adv() {
        return pet_adv;
    }

    public void setPet_adv(String pet_adv) {
        this.pet_adv = pet_adv;
    }

    public String getRes_adv() {
        return res_adv;
    }

    public void setRes_adv(String res_adv) {
        this.res_adv = res_adv;
    }

    public Date getDt_regis() {
        return dt_regis;
    }

    public void setDt_regis(Date dt_regis) {
        this.dt_regis = dt_regis;
    }

    public Date getDate_of_filing() {
        return date_of_filing;
    }

    public void setDate_of_filing(Date date_of_filing) {
        this.date_of_filing = date_of_filing;
    }

    public Date getDate_of_decision() {
        return date_of_decision;
    }

    public void setDate_of_decision(Date date_of_decision) {
        this.date_of_decision = date_of_decision;
    }

    public String getJocode() {
        return jocode;
    }

    public void setJocode(String jocode) {
        this.jocode = jocode;
    }

    public String getCase_no() {
        return case_no;
    }

    public void setCase_no(String case_no) {
        this.case_no = case_no;
    }

    public Short getPet_age() {
        return pet_age;
    }

    public void setPet_age(Short pet_age) {
        this.pet_age = pet_age;
    }
    public Short getRes_age() {
        return res_age;
    }

    public void setRes_age(Short res_age) {
        this.res_age = res_age;
    }

    public Integer getCs_subject() {
        return cs_subject;
    }

    public void setCs_subject(Integer cs_subject) {
        this.cs_subject = cs_subject;
    }

    public Integer getCss_subject() {
        return css_subject;
    }

    public void setCss_subject(Integer css_subject) {
        this.css_subject = css_subject;
    }

    public String getCase_remark() {
        return case_remark;
    }

    public void setCase_remark(String case_remark) {
        this.case_remark = case_remark;
    }

    public String getMain_case_no() {
        return main_case_no;
    }

    public void setMain_case_no(String main_case_no) {
        this.main_case_no = main_case_no;
    }

    public CaseType getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseType caseType) {
        this.caseType = caseType;
    }

    public Courtt getCourtt() {
        return courtt;
    }

    public void setCourtt(Courtt courtt) {
        this.courtt = courtt;
    }

    public List<Connectedt> getCivilt2connected() {
        return civilt2connected;
    }

    public void setCivilt2connected(List<Connectedt> civilt2connected) {
        this.civilt2connected = civilt2connected;
    }


    public Integer getCsss_subject() {
        return csss_subject;
    }

    public void setCsss_subject(Integer csss_subject) {
        this.csss_subject = csss_subject;
    }

    public Short getOrgid() {
        return orgid;
    }

    public void setOrgid(Short orgid) {
        this.orgid = orgid;
    }

    public Short getResorgid() {
        return resorgid;
    }

    public void setResorgid(Short resorgid) {
        this.resorgid = resorgid;
    }

    public String getNature_cd() {
        return nature_cd;
    }

    public void setNature_cd(String nature_cd) {
        this.nature_cd = nature_cd;
    }

    public Integer getC_subject() {
        return c_subject;
    }

    public void setC_subject(Integer c_subject) {
        this.c_subject = c_subject;
    }

    public List<Extraadvt> getExtraadvts() {
        return extraadvts;
    }

    public void setExtraadvts(List<Extraadvt> extraadvts) {
        this.extraadvts = extraadvts;
    }

    @Override
    public UUID getID() {
        return null;
    }
}

