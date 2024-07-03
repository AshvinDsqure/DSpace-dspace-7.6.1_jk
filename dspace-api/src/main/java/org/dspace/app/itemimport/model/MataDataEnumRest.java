/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport.model;

import org.dspace.content.cis.dto.CiviltDTORest;

import java.util.stream.Collectors;

public enum MataDataEnumRest {
    cino("cino", "casefile", "case", "cnrnumber", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getCino();
        }
    },
    regcase_type("casetype", "casefile", "case", "typename", false) {
        @Override
        public String getvalue() {
            String caseTypeDTO=this.getCiviltDTO().getRegcase_type();
            if(caseTypeDTO.trim().toLowerCase().equals("wpc")){
                caseTypeDTO= this.getCiviltDTO().getNature_t();
            }
            return caseTypeDTO;
        }
    },
    type("type", "dspace", "entity", "type", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getType();
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
    pet_name("pet_name", "casefile", "petitioner", "name", true) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getPet_name().replace("@","||");
        }
    },
    res_name("res_name", "casefile", "respondent", "name", true) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getRes_name().replace("@","||");
        }
    },
    pet_adv("pet_adv", "casefile", "advocate", "petitioner", true) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getPet_adv().replace("@","||");
        }
    },
    res_adv("res_adv", "casefile", "advocate", "respondent", true) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getRes_adv().replace("@","||");
        }
    },
    dt_regis("dt_regis", "casefile", "case", "registrationdate", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getDt_regis() != null ?this.getCiviltDTO().getDt_regis() :null;
        }
    },
    date_of_filing("date_of_decision", "casefile", "status", "dateofdisposal", false) {
        @Override
        public String getDatevalue() {
            return this.getCiviltDTO().getDate_of_filing();
        }
    },
    date_of_decision("date_of_decision", "casefile", "status", "dateofdisposal", false) {
        public String getvalue() {
            return this.getCiviltDTO().getDate_of_decision() != null ?this.getCiviltDTO().getDate_of_decision().toString() :null;
        }
    },
    jocode("judgeName", "casefile", "judge", "name", true) { //judgeName
        @Override
        public String getvalue() {
            if(this.getCiviltDTO().getJudgeName() != null){
                return this.getCiviltDTO().getJocode().replace("@","||");
            }else{
                return  null;
            }
        }
    },
    case_no("casuselistcino", "causelist", "casefile", "number", false) {
        @Override
        public String getvalue() {
            //return this.getCiviltDTO().getCase_no();
            return  null; //chanage by raja sir.....
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
    },
    dist_name("dist_name", "casefile", "fir", "district", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getDist_name();
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
    categorisation("categorisation", "casefile", "case", "categorisation", true) {
        @Override
        public String getvalue() {
            if (this.getCiviltDTO().getCategorisation() != null) {
                return this.getCiviltDTO().getCategorisation();
            }
            return null;
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
    case_remark("case_remark", "causelist", "case", "remark", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getCase_remark();
        }
    },
    casefilestatus("casefilestatus", "casefile", "case", "status", false) {
        @Override
        public String getvalue() {
            return this.getCiviltDTO().getCasefilestatus();
        }
    };
    private String fild;
    private String schema;
    private String element;
    private String qualifier;
    private CiviltDTORest civiltDTO;
    private  boolean isMultiple;

    MataDataEnumRest(String fild) {

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
    public String getDatevalue() {
        return this.getCiviltDTO().getDt_regis();
    }

    MataDataEnumRest(String fild, String schema, String element, String qualifier, boolean isMultiple) {
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

    public CiviltDTORest getCiviltDTO() {
        return civiltDTO;
    }

    public void setCiviltDTO(CiviltDTORest civiltDTO) {
        this.civiltDTO = civiltDTO;
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public void setMultiple(boolean multiple) {
        isMultiple = multiple;
    }
}
