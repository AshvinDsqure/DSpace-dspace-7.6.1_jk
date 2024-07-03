/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport.model;

import org.dspace.content.cis.dto.CaseTypeDTO;
import org.dspace.content.cis.dto.CiviltDTO;

import java.util.Date;
import java.util.stream.Collectors;

public enum MataDataEnum {
    cino("cino", "casefile", "case", "cnrnumber", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getCino();
        }
    },
    caseType("casetype", "casefile", "case", "typename", false) {
        @Override
        public String getvalue() {
            CaseTypeDTO caseTypeDTO=this.getCiviltDTO().getCaseType();
            if(caseTypeDTO != null){
                return this.getCiviltDTO().getCaseType().getType_name();
            }
            return null;
        }

    },
    reg_no("reg_no", "dc", "title", null, false) {
        @Override
        public String getvalue() {
            if(this.getCiviltDTO().getReg_no().toString().contains("9900"))
            return this.getCiviltDTO().getReg_no()!= null? ""+this.getCiviltDTO().getReg_no().toString().replace("9900" ,"")+"(P)":null;
            else
            return this.getCiviltDTO().getReg_no()!= null? ""+this.getCiviltDTO().getReg_no():null;
        }
    },
    reg_year("reg_year", "casefile", "case", "registrationyear", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getReg_year() != null ?""+this.getCiviltDTO().getReg_year():null;
        }
    },
    ia_regno("ia_regno", "dc", "title", null, false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    ia_regyear("ia_regyear", "casefile", "case", "registrationyear", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    judgeName("judgeName", "casefile", "judge", "name", true) {
        @Override
        public String getvalue() {
            if(this.getCiviltDTO().getJudgeName() != null){
                return this.getCiviltDTO().getJudgeName().getJudge_name();
            }else{
                return  null;
            }
        }
    },
    categorisation("categorisation", "casefile", "case", "categorisation", true) {
        @Override
        public String getvalue() {
            if (this.getCiviltDTO().getCategorisation() != null) {
                String subject="";
                if(this.getCiviltDTO().getSubjectMaster() != null){
                    subject=this.getCiviltDTO().getSubjectMaster().getSubject_name();
                }
                return subject;
            }
            return null;
        }
    },
    judge_name("judge_name", "casefile", "judge", "name", true) {
        @Override
        public String getvalue() {
           return this.getCiviltDTO().getJudge_name();
        }
    },
    jocodecaseFile("jocodecaseFile", "casefile", "judge", "name", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    pet_name("pet_name", "casefile", "petitioner", "name", true) {
        @Override
        public String getvalue() {

                if(this.getCiviltDTO().getPet_name()!=null) {
                    String pet_names = this.getCiviltDTO().getExtrapetitioner().stream().filter(extraadvt -> extraadvt != null).map(d -> {
                        return d;
                    }).collect(Collectors.joining("||"));
                    return pet_names;
                }
            return null;
        }
    },
    res_name("res_name", "casefile", "respondent", "name", true) {
        @Override
        public String getvalue() {
                if(this.getCiviltDTO().getRes_name()!=null) {
                    String resnames = this.getCiviltDTO().getExtrarespondent().stream().filter(extraadvt -> extraadvt != null).map(d -> {
                        return d;
                    }).collect(Collectors.joining("||"));
                    return resnames;
                }
            return null;
        }
    },
    pet_advocates("pet_advocates", "casefile", "advocate", "petitioner", true) {
        @Override
        public String getvalue() {
            if(this.getCiviltDTO().getPet_advocates() != null) {
                String pet_advocates = this.getCiviltDTO().getPet_advocates().stream().filter(extraadvt -> extraadvt.getType() == 1).map(d -> {
                    return d.getAdv_name();
                }).collect(Collectors.joining("||"));
                return pet_advocates;
            }
            else {
                return  null;
            }
        }
    },
    extrarespondate("extrarespondate", "casefile", "respondent", "name", true) {
        @Override
        public String getvalue() {
            if(this.getCiviltDTO().getExtrarespondate() != null) {
                String pet_advocates = this.getCiviltDTO().getExtrarespondate().stream().map(d -> {
                    return d;
                }).collect(Collectors.joining("||"));
                return pet_advocates;
            }
            else {
                return  null;
            }
        }
    },
    extrapetitioner("extrapetitioner", "casefile", "petitioner", "name", true) {
        @Override
        public String getvalue() {
            if(this.getCiviltDTO().getExtrapetitioner() != null) {
                String pet_advocates = this.getCiviltDTO().getExtrapetitioner().stream().map(d -> {
                    return d;
                }).collect(Collectors.joining("||"));
                return pet_advocates;
            }
            else {
                return  null;
            }
        }
    },
    actname("actname", "casefile", "case", "actname", true) {
        @Override
        public String getvalue() {
            if(this.getCiviltDTO().getActnames() != null) {
                String actnames = this.getCiviltDTO().getActnames().stream().filter(act ->act!=null).collect(Collectors.joining("||"));
                return actnames;
            }
            else {
                return  null;
            }
        }
    },
    section("section", "casefile", "case", "section", true) {
        @Override
        public String getvalue() {
            if(this.getCiviltDTO().getActnames() != null) {
                String sections = this.getCiviltDTO().getSections().stream().filter(act ->act!=null)
                        .map(d->{
                            return d;
                        })
                        .collect(Collectors.joining("||"));
                return sections;
            }
            else {
                return  null;
            }
        }
    },
    nature("nature", "casefile", "case", "nature", false) {
        @Override
        public String getvalue() {
          return  this.getCiviltDTO().getNature();
        }
    },
    dist_name("dist_name", "casefile", "fir", "district", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getDist_name();
        }
    },

    cs_subject_2("cs_subject_2", "casefile", "casesub", "category", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getCs_subject_2();
        }
    }, css_subject_3("css_subject_3", "casefile", "casesubsub", "category", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getCss_subject_3();
        }
    }, csss_subject_4("csss_subject_4", "casefile", "casesubsubsub", "category", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getCsss_subject_4();
        }
    },

    res_advocates("res_name", "casefile", "advocate", "respondent", true) {
        @Override
        public String getvalue() {
            if(this.getCiviltDTO().getPet_advocates() != null) {
                String pet_advocates = this.getCiviltDTO().getPet_advocates().stream().filter(extraadvt -> extraadvt.getType() == 2).map(d -> {
                    return d.getAdv_name();
                }).collect(Collectors.joining("||"));
                return pet_advocates;
            }
            else {
                return  null;
            }
        }
    },
    pet_adv("pet_adv", "casefile", "advocate", "petitioner", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getPet_adv();
        }
    },
    res_adv("res_adv", "casefile", "advocate", "respondent", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getRes_adv();
        }
    },
    dt_regis("dt_regis", "casefile", "case", "registrationdate", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getDt_regis() != null ?this.getCiviltDTO().getDt_regis().toString() :null;
        }
    },
    date_of_decision("date_of_decision", "casefile", "status", "dateofdisposal", false) {
        public String getvalue() {
            return this.getCiviltDTO().getDate_of_decision() != null ?this.getCiviltDTO().getDate_of_decision().toString() :null;
        }
    },
    date_of_filing("date_of_decision", "casefile", "status", "dateofdisposal", false) {
        @Override
        public Date getDatevalue() {
            return null;
        }
    },
    iacasetype("ia_type_name", "casefile", "case", "typename", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    appotherparty("appotherparty", "casefile", "petitioner", "name", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    againotherparty("againotherparty", "casefile", "respondent", "name", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    appotheradv("appotheradv", "casefile", "advocate", "petitioner", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },

    againotheradv("againotheradv", "casefile", "advocate", "respondent", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    date_of_ia_registration("date_of_ia_registration", "casefile", "case", "registrationdate", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    date_of_order("date_of_order", "casefile", "status", "dateofdisposal", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    petname("petname", "casefile", "petitioner", "name", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    resname("resname", "casefile", "respondent", "name", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    dt_reg("dt_reg", "casefile", "case", "registrationdate", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    for_bench_id("for_bench_id", "causelist", "bench", "code", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    casuselistcino("casuselistcino", "causelist", "casefile", "number", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    causelist_date("causelist_date", "causelist", "date", null, false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    causelistperiod("causelistperiod", "cause_list_type", "causelist", "type", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    ia_case_type_t("ia_case_type_t", "casefile", "case", "typename", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    epersonjocode("epersonjocode", "eperson", "jocode", null, false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    casefilestatus("casefilestatus", "casefile", "case", "status", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getCasefilestatus();
        }
    },
    case_remark("case_remark", "causelist", "case", "remark", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    sr_no("sr_no", "causelist", "case", "srno", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    unique_no("unique_no", "causelist", "case", "uniqueno", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    purpose_cd("purpose_cd", "causelist", "purpose", "cd", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    pet_age("pet_age", "casefile", "petitioner", "age", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getPet_age() != null ?""+this.getCiviltDTO().getPet_age() : null;
        }
    },
    res_age("res_age", "casefile", "respondent", "age", false) {
        @Override
        public Short getShortvalue() {
            return this.getCiviltDTO().getRes_age();

        }
    },
    newregcase_type("newregcase_type", "casefile", "newregcase_type", null, false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    newreg_no("newreg_no", "casefile", "newreg_no", null, false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    newreg_year("newreg_year", "casefile", "newreg_year", null, false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    petitionerorganization("petitionerorganization", "casefile", "petitioner", "organization", false) {
        @Override
        public String getvalue() {
            var petitionerorganization="";
            if(this.getCiviltDTO().getPetitionerorganization() != null){
                petitionerorganization=this.getCiviltDTO().getPetitionerorganization().getOrgname();
            }
            return  petitionerorganization;
        }
    },
    respondentorganization("respondentorganization", "casefile", "respondent", "organization", false) {
        @Override
        public String getvalue() {
            var respondentorganization="";
            if(this.getCiviltDTO().getRespondentorganization() != null){
                respondentorganization=this.getCiviltDTO().getRespondentorganization().getOrgname();
            }
            return  respondentorganization;
        }
    },

    regcase_type("regcase_type", "casefile", "case", "typename", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getRegcase_type();
        }
    },
    orders("orders", "causelist", "order_name", null, false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    ia_no("ia_no", "causelist", "ia", "no", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    ia_case_type("ia_case_type", "causelist", "ia", "type", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    ia_flag("ia_flag", "causelist", "ia", "flag", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    old_ia_no("old_ia_no", "casefile", "old", "iano", false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    relief_offense("relief_offense", "casefile", "relief_offense", null, false) {
        @Override
        public String getvalue() {
            return null;
        }
    },
    type("type", "dspace", "entity", "type", false) {
        @Override
        public String getvalue() {
           return this.getCiviltDTO().getType();
        }
    },
    fir_no("fir_no", "casefile", "fir", "number", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getFir_no();

        }
    },
    fir_year("fir_year", "casefile", "fir", "year", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getFir_year();

        }
    },
    policestation_name("policestation_name", "casefile", "fir", "policestationname", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getPolicestation_name();
        }
    };
    private String fild;
    private String schema;
    private String element;
    private String qualifier;
    private CiviltDTO civiltDTO;
    private  boolean isMultiple;

    MataDataEnum(String fild) {

    }
    public  String getMataDataString(){
        return  schema +"."+element+"."+qualifier;
    }
    public String getvalue() {
        return null;
    }
    public int getIntvalue() {
        return 0;
    }
    public Short getShortvalue() {
        return null;
    }
    public Date getDatevalue() {
        return this.getCiviltDTO().getDt_regis();
    }

    MataDataEnum(String fild, String schema, String element, String qualifier, boolean isMultiple) {
        this.fild = fild;
        this.schema = schema;
        this.element = element;
        this.qualifier = qualifier;
        this.isMultiple=isMultiple;
    }

    public String getFild() {
        return fild;
    }

    public void setFild(String fild) {
        this.fild = fild;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public CiviltDTO getCiviltDTO() {
        return civiltDTO;
    }

    public void setCiviltDTO(CiviltDTO civiltDTO) {
        this.civiltDTO = civiltDTO;
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public void setMultiple(boolean multiple) {
        isMultiple = multiple;
    }
}
