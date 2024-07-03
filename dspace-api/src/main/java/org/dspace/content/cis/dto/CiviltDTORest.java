/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dto;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Class representing bitstreams stored in the DSpace system.
 * <p>
 * When modifying the bitstream metadata, changes are not reflected in the
 * database until <code>update</code> is called. Note that you cannot alter
 * the contents of a bitstream; you need to create a new bitstream.
 *
 * @author Robert Tansley
 */

public class CiviltDTORest {
    protected String cino;
    private  String nature_t;
    protected Integer reg_no;
    protected Short reg_year;
    protected String pet_name;
    protected  String branch_id;
    protected String res_name;
    protected String pet_adv;
    protected String res_adv;
    protected String dt_regis;
    protected String date_of_filing;
    protected Date date_of_decision;
    protected String jocode;
    protected Integer c_subject;
    protected String case_no;
    protected Short pet_age = 0;
    protected Short res_age = 0;
    private CaseTypeDTO caseType = null;
    private CourttDTO courtt = null;
    private String nature_cd;
    private DspaceToCisMappingDTO dspaceToCisMapping;
    private List<ConnectedtDTO> civilt2connected;
    private List<UUID> civilt2ItemUUID;
    private SubjectMasterDTO subjectMaster;
    private SubnatureOnetDTO subnatureOnet;
    private SubnatureTwoTDTO subnatureTwoT;
    private SubnatureThreeTDTO subnatureThreeT;
    private OrgNametDTO petitionerorganization;
    private OrgNametDTO respondentorganization;
    private JudgeNameDTO judgeName;
    private String subject;
    protected Integer cs_subject;
    protected Integer css_subject;
    protected Integer csss_subject;
    protected String c_subject_1;
    private String categorisation;
    protected String cs_subject_2;
    protected String css_subject_3;
    protected String csss_subject_4;

    protected String orgid ;
    protected OrgNametDTO orgNametDTO;
    protected String case_remark;
    protected String main_case_no;
    protected String type = "CaseFile";
    protected String fir_no;
    protected String fir_year;
    protected String policestation_name;
    protected String dist_name;
    private List<String> actnames;
    protected List<String> sections;
    protected String actname;
    protected String section;

    protected String casefilestatus;
    protected String regcase_type;
    protected List<ExtraadvtDTO> pet_advocates;
    protected List<ExtraadvtDTO> res_advocates;
    protected List<CiviltDTO> connected_case_data;
    protected UUID collectionuuid;

    protected  List<String> extrarespondate;
    protected  List<String> extrapetitioner;

    public CiviltDTORest() {
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
        if (this.pet_age != null && this.pet_age >= 65)
            return pet_name + "(Senior Citizen)";
        else
            return pet_name;
    }

    public void setPet_name(String pet_name) {
        this.pet_name = pet_name;
    }

    public String getRes_name() {
        if (this.res_age != null && this.res_age >= 65)
            return res_name + "(Senior Citizen)";
        else
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

    public String getDt_regis() {
        return dt_regis;
    }

    public void setDt_regis(String dt_regis) {
        this.dt_regis = dt_regis;
    }

    public String getDate_of_filing() {
        return date_of_filing;
    }

    public void setDate_of_filing(String date_of_filing) {
        this.date_of_filing = date_of_filing;
    }

    public Date getDate_of_decision() {
        return date_of_decision;
    }

    public void setDate_of_decision(Date date_of_decision) {
        this.date_of_decision = date_of_decision;
    }

    public String getJocode() {
        return null;
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

    public String getOrgid() {
        return orgid;
    }

    public void setOrgid(String orgid) {
        this.orgid = orgid;
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

    public CaseTypeDTO getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseTypeDTO caseType) {
        this.caseType = caseType;
    }

    public CourttDTO getCourtt() {
        return courtt;
    }

    public void setCourtt(CourttDTO courtt) {
        this.courtt = courtt;
    }

    public List<ConnectedtDTO> getCivilt2connected() {
        return civilt2connected;
    }

    public void setCivilt2connected(List<ConnectedtDTO> civilt2connected) {
        this.civilt2connected = civilt2connected;
    }

    public SubjectMasterDTO getSubjectMaster() {
        return subjectMaster;
    }

    public void setSubjectMaster(SubjectMasterDTO subjectMaster) {
        this.subjectMaster = subjectMaster;
    }

    public SubnatureOnetDTO getSubnatureOnet() {
        return subnatureOnet;
    }

    public void setSubnatureOnet(SubnatureOnetDTO subnatureOnet) {
        this.subnatureOnet = subnatureOnet;
    }

    public SubnatureTwoTDTO getSubnatureTwoT() {
        return subnatureTwoT;
    }

    public SubnatureThreeTDTO getSubnatureThreeT() {
        return subnatureThreeT;
    }

    public void setSubnatureThreeT(SubnatureThreeTDTO subnatureThreeT) {
        this.subnatureThreeT = subnatureThreeT;
    }

    public void setSubnatureTwoT(SubnatureTwoTDTO subnatureTwoT) {
        this.subnatureTwoT = subnatureTwoT;
    }

    public DspaceToCisMappingDTO getDspaceToCisMapping() {
        return dspaceToCisMapping;
    }

    public void setDspaceToCisMapping(DspaceToCisMappingDTO dspaceToCisMapping) {
        this.dspaceToCisMapping = dspaceToCisMapping;
    }

    public OrgNametDTO getPetitionerorganization() {
        return petitionerorganization;
    }

    public void setPetitionerorganization(OrgNametDTO petitionerorganization) {
        this.petitionerorganization = petitionerorganization;
    }

    public OrgNametDTO getRespondentorganization() {
        return respondentorganization;
    }

    public void setRespondentorganization(OrgNametDTO respondentorganization) {
        this.respondentorganization = respondentorganization;
    }

    public Integer getC_subject() {
        return c_subject;
    }

    public void setC_subject(Integer c_subject) {
        this.c_subject = c_subject;
    }

    public Integer getCsss_subject() {
        return csss_subject;
    }

    public void setCsss_subject(Integer csss_subject) {
        this.csss_subject = csss_subject;
    }

    public String getNature_cd() {
        return nature_cd;
    }

    public void setNature_cd(String nature_cd) {
        this.nature_cd = nature_cd;
    }

    public OrgNametDTO getOrgNametDTO() {
        return orgNametDTO;
    }

    public void setOrgNametDTO(OrgNametDTO orgNametDTO) {
        this.orgNametDTO = orgNametDTO;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCasefilestatus() {
        if (this.date_of_decision == null) {
            return "Pending";
        } else {
            return "Disposed";
        }

    }

    public void setCasefilestatus(String casefilestatus) {
        this.casefilestatus = casefilestatus;
    }

    public String getRegcase_type() {
        if (this.regcase_type != null) {
            return regcase_type;
        }
        return  null;
    }

    public void setRegcase_type(String regcase_type) {
        this.regcase_type = regcase_type;
    }

    public List<ExtraadvtDTO> getPet_advocates() {
        return pet_advocates;
    }

    public void setPet_advocates(List<ExtraadvtDTO> pet_advocates) {
        this.pet_advocates = pet_advocates;
    }

    public List<ExtraadvtDTO> getRes_advocates() {
        return res_advocates;
    }

    public void setRes_advocates(List<ExtraadvtDTO> res_advocates) {
        this.res_advocates = res_advocates;
    }

    public JudgeNameDTO getJudgeName() {
        return judgeName;
    }

    public void setJudgeName(JudgeNameDTO judgeName) {
        this.judgeName = judgeName;
    }

    public List<UUID> getCivilt2ItemUUID() {
        return civilt2ItemUUID;
    }

    public void setCivilt2ItemUUID(List<UUID> civilt2ItemUUID) {
        this.civilt2ItemUUID = civilt2ItemUUID;
    }

    public UUID getCollectionuuid() {
        return collectionuuid;
    }

    public void setCollectionuuid(UUID collectionuuid) {
        this.collectionuuid = collectionuuid;
    }

    public String getNature_t() {
        return nature_t;
    }

    public void setNature_t(String nature_t) {
        this.nature_t = nature_t;
    }

    public String getBranch_id() {
        return branch_id;
    }

    public void setBranch_id(String branch_id) {
        this.branch_id = branch_id;
    }

    public String getC_subject_1() {
        return c_subject_1;
    }

    public void setC_subject_1(String c_subject_1) {
        this.c_subject_1 = c_subject_1;
    }

    public String getCs_subject_2() {
        return cs_subject_2;
    }

    public void setCs_subject_2(String cs_subject_2) {
        this.cs_subject_2 = cs_subject_2;
    }

    public String getCss_subject_3() {
        return css_subject_3;
    }

    public void setCss_subject_3(String css_subject_3) {
        this.css_subject_3 = css_subject_3;
    }

    public String getCsss_subject_4() {
        return csss_subject_4;
    }

    public void setCsss_subject_4(String csss_subject_4) {
        this.csss_subject_4 = csss_subject_4;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<CiviltDTO> getConnected_case_data() {
        return connected_case_data;
    }

    public void setConnected_case_data(List<CiviltDTO> connected_case_data) {
        this.connected_case_data = connected_case_data;
    }

    public List<String> getExtrarespondate() {
        return extrarespondate;
    }

    public void setExtrarespondate(List<String> extrarespondate) {
        this.extrarespondate = extrarespondate;
    }

    public List<String> getExtrapetitioner() {
        return extrapetitioner;
    }

    public void setExtrapetitioner(List<String> extrapetitioner) {
        this.extrapetitioner = extrapetitioner;
    }

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

    public String getPolicestation_name() {
        return policestation_name;
    }

    public void setPolicestation_name(String policestation_name) {
        this.policestation_name = policestation_name;
    }

    public String getDist_name() {
        return dist_name;
    }

    public void setDist_name(String dist_name) {
        this.dist_name = dist_name;
    }

    public List<String> getActnames() {
        return actnames;
    }

    public void setActnames(List<String> actnames) {
        this.actnames = actnames;
    }

    public List<String> getSections() {
        return sections;
    }

    public void setSections(List<String> sections) {
        this.sections = sections;
    }

    public String getActname() {
        return actname;
    }

    public void setActname(String actname) {
        this.actname = actname;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getCategorisation() {
        return categorisation;
    }

    public void setCategorisation(String categorisation) {
        this.categorisation = categorisation;
    }
}

