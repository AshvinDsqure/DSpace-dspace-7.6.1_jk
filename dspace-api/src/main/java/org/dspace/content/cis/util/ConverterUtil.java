package org.dspace.content.cis.util;

import com.google.gson.Gson;
import org.apache.tools.ant.taskdefs.Get;
import org.dspace.app.itemimport.model.MataDataEnum;
import org.dspace.app.itemimport.model.MataDataEnumRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.Collection;
import org.dspace.content.cis.*;
import org.dspace.content.cis.dto.*;
import org.dspace.content.cis.service.OrgNametService;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.core.ContextCIS;
import org.dspace.discovery.IndexingService;
import org.dspace.discovery.indexobject.IndexableItem;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class ConverterUtil {
    Gson gson = new Gson();
    private OrgNametService orgNametService;
    private CommunityService communityService;
    protected MetadataValueService metadataValueService;
    private String cino=null;
    private  EPerson ePerson;
    @Autowired
    private WorkspaceItemService workspaceItemService;
    @Autowired
    private ItemService itemService;
    @Autowired(required = true)
    protected InstallItemService installItemService;
    @Autowired(required = true)
    protected IndexingService indexingService;

    protected CollectionService collectionService;
    @Autowired
    protected RelationshipService relationshipService;
    @Autowired
    protected RelationshipTypeService relationshipTypeService;

    private ContextCIS contextCIS;
    private Context context;
    private  String causelistDate;
    private String cisUri;
    private BlockingQueue<Civilt> queue;
    private BlockingQueue<CiviltDTO> queueDTO;

    private boolean migrateConnectedCase;
    Predicate<CiviltDTO> c_subjectPredicate = csubject -> csubject.getC_subject() != null;
    Predicate<CiviltDTO> cs_subjectPredicate = csubject -> csubject.getCs_subject() != null;
    Predicate<CiviltDTO> css_subjectPredicate = csubject -> csubject.getCss_subject() != null;
    Predicate<CiviltDTO> csss_subjectPredicate = csubject -> csubject.getCsss_subject() != null;
    Predicate<CiviltDTO> rgidPredicate = rgid -> rgid.getOrgid() != null;
    private Function<CiviltDTO, CiviltDTO> OrgNameFunction = civiltDTO -> {
        try {
            if (rgidPredicate.test(civiltDTO)) {
                OrgNametDTO orgNametDTO = this.getOrgNametDTO(civiltDTO);
                if (orgNametDTO != null) {
                    civiltDTO.setOrgNametDTO(orgNametDTO);
                }
            }
            return civiltDTO;
        } catch (SQLException e) {
            return civiltDTO;
        } catch (Exception ex) {
            return civiltDTO;
        }

    };
    private Function<CiviltDTO, CiviltDTO> JudgeNameFunction = civiltDTO -> {
        try {
            //System.out.println("jocode::::" + civiltDTO.getJocode());
            JudgeNameDTO judgeNameDTO = this.getJudGeNames(civiltDTO.getJocode().split(","));
            if (judgeNameDTO != null) {
                civiltDTO.setJudgeName(judgeNameDTO);
            }

            return civiltDTO;
        } catch (SQLException e) {
            return civiltDTO;
        } catch (Exception ex) {
            return civiltDTO;
        }

    };
    private Function<Short, CaseTypeDTO> civilt2naturetCaseType = nature_cd -> {
        try {
            Naturet naturet = this.findCaseTypeFromNatureT(nature_cd);
            if (naturet != null) {
                CaseTypeDTO caseTypeDTO = new CaseTypeDTO(naturet.getNature_cd(), naturet.getNature_desc());
                return caseTypeDTO;
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        } catch (Exception ex) {
            return null;
        }

    };
    private Function<Civilt, List<String>> extrarespondateFuncation = civilt -> {
        try {
            List<Civaddresst> civaddresst = this.getExtrarespondate(civilt);
            if (civaddresst != null) {
               return civaddresst.stream().map(d-> d.getName()).peek(p->System.out.println("extrarespondateFuncation::::"+p +" cino::::"+civilt.getCino())).collect(Collectors.toList());
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        } catch (Exception ex) {
            return null;
        }

    };
    private Function<Civilt, List<String>> extextrapetitionerFuncation = civilt -> {
        try {
            List<Civaddresst> civaddresst = this.getExtrapetitioner(civilt);
            if (civaddresst != null) {
                return civaddresst.stream().map(d-> d.getName()).peek(p->System.out.println("extextrapetitionerFuncation::::"+p +" cino::::"+civilt.getCino())).collect(Collectors.toList());
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        } catch (Exception ex) {
            return null;
        }

    };
    private Function<CiviltDTO, CiviltDTO> SubjectMaterFunction = d -> {
        try {
            if (c_subjectPredicate.test(d)) {
                SubjectMasterDTO subjectMasterDTO = this.getSubjectMasterDTO(d);
                if (subjectMasterDTO != null) {
                    d.setSubjectMaster(subjectMasterDTO);
                }
            }
            return d;
        } catch (SQLException e) {
            return d;
        } catch (Exception ex) {
            return d;
        }

    };
    private Function<CiviltDTO, CiviltDTO> SubnatureOnetFunction = d -> {
        try {
            if (c_subjectPredicate.and(cs_subjectPredicate).test(d)) {
                SubnatureOnetDTO subnatureOnetDTO = this.getSubnatureOnetDTO(d);
                if (subnatureOnetDTO != null) {
                    d.setSubnatureOnet(subnatureOnetDTO);
                }
            }
            return d;
        } catch (SQLException e) {
            return d;
        } catch (Exception ex) {
            return d;
        }
    };
    private Function<CiviltDTO, CiviltDTO> SubnatureTwotFunction = d -> {
        try {
            if (c_subjectPredicate.and(cs_subjectPredicate).and(css_subjectPredicate).test(d)) {
                SubnatureTwoTDTO subnatureTwoTDTO = this.getSubnatureTwoT(d);
                if (subnatureTwoTDTO != null) {
                    d.setSubnatureTwoT(subnatureTwoTDTO);
                }
            }
            return d;
        } catch (SQLException e) {
            return d;
        } catch (Exception ex) {
            return d;
        }
    };
    private Function<CiviltDTO, CiviltDTO> SubnatureThreetFunction = d -> {
        try {
            if (c_subjectPredicate.and(cs_subjectPredicate).and(css_subjectPredicate).and(csss_subjectPredicate).test(d)) {
                //SubnatureThreeTDTO subnatureThreeTDTO = this.getSubnatureThreeTDTO(d);
               /* if (subnatureThreeTDTO != null) {
                    d.setSubnatureThreeT(subnatureThreeTDTO);
                }*/
            }
            return d;
        } catch (Exception e) {
            return d;
        }
    };

    public ConverterUtil(Context context, CommunityService communityService) {
        this.context = context;
        this.communityService = communityService;
    }

    public ConverterUtil(ContextCIS contextCIS, OrgNametService orgNametService, LinkedBlockingQueue queue) {
        this.orgNametService = orgNametService;
        this.contextCIS = contextCIS;
        this.queue = queue;
        this.queueDTO = new LinkedBlockingQueue<>();
    }

    public CiviltDTO getConnectedCiviltDTO() throws InterruptedException {
        Civilt civilt = this.queue.take();
        CiviltDTO civiltDTO = new CiviltDTO();
        DspaceToCisMapping dspaceToCisMapping = civilt.getDspaceToCisMapping();
        DspaceToCisMappingDTO dspaceToCisMappingDTO = new DspaceToCisMappingDTO();
        dspaceToCisMappingDTO.setCino(dspaceToCisMapping.getCino());
        civiltDTO.setDspaceToCisMapping(dspaceToCisMappingDTO);
        dspaceToCisMappingDTO.setDspaceobjectid(dspaceToCisMapping.getDspaceobjectid());
        civiltDTO.setCino(civilt.getCino());
        civiltDTO.setCivilt2ItemUUID(civilt.getCivilt2connected().stream().map(d -> {
            System.out.println("d.getCivilt().getDspaceToCisMapping().getDspaceobjectid():::::"+d.getCivilt().getDspaceToCisMapping().getDspaceobjectid());
            return d.getCivilt().getDspaceToCisMapping().getDspaceobjectid();
        }).collect(Collectors.toList()));
        return civiltDTO;
    }
    public CiviltDTO getTOCiviltDTOQueueforupdate(LinkedBlockingQueue<Civilt> queue) throws InterruptedException, Exception {
        System.out.println("this.queue:::" + queue.size());
        CiviltDTO civiltDTO = new CiviltDTO();
        synchronized (queue) {
            Civilt civilt = queue.take();
            civiltDTO.setCino(civilt.getCino());
            civiltDTO.setRes_name(civilt.getRes_name());
            civiltDTO.setPet_name(civilt.getPet_name());
            List<Civaddresst> extrarespondatelist =  orgNametService.findExtraPatandres(this.contextCIS,civilt.getCino());
            if (extrarespondatelist != null) {
                civiltDTO.setExtrarespondate(extrarespondatelist.stream().filter(d-> d.getType() == 2).map(d -> d.getName()).peek(p -> System.out.println("extrarespondateFuncation::::" + p + " cino::::" + civilt.getCino())).collect(Collectors.toList()));
            }
            if (extrarespondatelist != null) {
                civiltDTO.setExtrapetitioner(extrarespondatelist.stream().filter(d-> d.getType() == 1).map(d -> d.getName()).peek(p -> System.out.println("extextrapetitionerFuncation::::" + p + " cino::::" + civilt.getCino())).collect(Collectors.toList()));
            }
        }
        return civiltDTO;
    }
    public CiviltDTO pushTOCiviltDTOQueue(LinkedBlockingQueue<Civilt> queue) throws InterruptedException, Exception {
        System.out.println("this.queue:::" + queue.size());
        Civilt civilt = queue.take();
        CiviltDTO civiltDTO = new CiviltDTO();
        civiltDTO.setDate_of_filing(civilt.getDate_of_filing());
        civiltDTO.setCino(civilt.getCino());
        civiltDTO.setDate_of_decision(civilt.getDate_of_decision());
        civiltDTO.setC_subject(civilt.getC_subject());
        civiltDTO.setCase_no(civilt.getCase_no());
        civiltDTO.setPet_age(civilt.getPet_age());
        civiltDTO.setRes_age(civilt.getRes_age());
        civiltDTO.setReg_year(civilt.getReg_year());
        civiltDTO.setCs_subject(civilt.getCs_subject());
        civiltDTO.setCss_subject(civilt.getCs_subject());
        civiltDTO.setCsss_subject(civilt.getCsss_subject());
        civiltDTO.setOrgid(civilt.getOrgid());
        civiltDTO.setNature_cd(civilt.getNature_cd());
        civiltDTO.setCase_remark(civilt.getCase_remark());
        civiltDTO.setMain_case_no(civilt.getMain_case_no());
        civiltDTO.setReg_no(civilt.getReg_no());
        civiltDTO.setPet_name(civilt.getPet_name());
        civiltDTO.setRes_name(civilt.getRes_name());
        civiltDTO.setPet_adv(civilt.getPet_adv());
        civiltDTO.setRes_adv(civilt.getRes_adv());
        civiltDTO.setDt_regis(civilt.getDt_regis());
        //:::::::::::::::::::::::::::::jugename Name:::::::::::::::::::::::::::::::::::::::
        System.out.println(":::::::::::::::::::::::::::>>>>>>>>>>>civiltDTO.getCasefilestatus()   \t"+civiltDTO.getCasefilestatus());
        if(!civiltDTO.getCasefilestatus().equalsIgnoreCase("Pending")) {
            if (civilt.getJudge_code() != null && isDigitsOnly(civilt.getJudge_code())) {
                String jugename = getJudGeNamesByJudGeCode(civilt.getJudge_code());
                if (jugename != null) {
                    System.out.println("jugename name:::::::::::\t" + jugename);
                    civiltDTO.setJudge_name(jugename);
                }
            }
        }
        civiltDTO.setCi_cri(civilt.getCi_cri());
        civiltDTO.setJocode(civilt.getJocode());
        civiltDTO.setC_subject(civilt.getC_subject());
        CaseType caseType = civilt.getCaseType();
        //::::::::::::::::::::::Act & section:::::::::::::::::::::::::::::::::::::::
        List<ExtraActT> extraActT=civilt.getExtraactt();
        if(extraActT!=null){
            civiltDTO.setActnames(extraActT.stream().filter(extraadvt -> extraadvt.getActs()!=null).filter(dd->dd.getActs().getActname()!=null).map(d -> {
                return d.getActs().getActname();
            }).collect(Collectors.toList()));
            civiltDTO.setSections(extraActT.stream().filter(extraadvt -> extraadvt.getSection()!=null).map(d -> {
                return d.getSection();
            }).collect(Collectors.toList()));
        }else {
            System.out.println("::::::::::::::::getActname:::::::null");
        }
        //:::::::::::::::::::::::Extra Party name for petitioner:::::::::::::::::::::::::::::
        List<Civaddresst> extrarespondatelist =  orgNametService.findExtraPatandres(this.contextCIS,civilt.getCino());
        if (extrarespondatelist != null) {
            civiltDTO.setExtrarespondate(extrarespondatelist.stream().filter(d-> d.getType() == 2).map(d -> d.getName()).peek(p -> System.out.println("extrarespondateFuncation::::" + p + " cino::::" + civilt.getCino())).collect(Collectors.toList()));
        }
        if (extrarespondatelist != null) {
            civiltDTO.setExtrapetitioner(extrarespondatelist.stream().filter(d-> d.getType() == 1).map(d -> d.getName()).peek(p -> System.out.println("extextrapetitionerFuncation::::" + p + " cino::::" + civilt.getCino())).collect(Collectors.toList()));
        }
        //:::::::::::::::::::::FirNumber,Firyear,DistName,PoliceName::::::::::::::::::::
        if(civilt.getCino()!=null){
            System.out.println("::::::::::::::cino:::::::::::::::::!!!"+civilt.getCino());
            FirNoAndFirYearAndDistrictAndPoliceNameDTO dto=getFirNoAndFirYearAndDistrictAndPoliceNameDTO(civilt.getCino());
            if(dto!=null){
                if(dto.getFir_no()!=null){
                    civiltDTO.setFir_no(dto.getFir_no());
                }if(dto.getFir_year()!=null){
                    civiltDTO.setFir_year(dto.getFir_year());
                }if(dto.getDist_name()!=null){
                    civiltDTO.setDist_name(dto.getDist_name());
                }if(dto.getPolice_st_name()!=null){
                    civiltDTO.setPolicestation_name(dto.getPolice_st_name());
                }
            }

            //GetPetName(civilt.getCino());
            //GetResName(civilt.getCino());

        }
        if (caseType != null && caseType.getType_name() != null) {
            String caseTyeStr = caseType.getType_name();
            CaseTypeDTO caseTypeDTO = null;
            if (caseTyeStr.equals("WPC")) {
                Short case_nature = null;
                try {
                    case_nature = Short.valueOf(civiltDTO.getNature_cd().trim());
                    caseTypeDTO = civilt2naturetCaseType.apply(case_nature);
                } catch (Exception e) {

                }
            } else {
                caseTypeDTO = new CaseTypeDTO(caseType.getCase_type(), caseType.getType_name());
            }
            civiltDTO.setCaseType(caseTypeDTO);
        }
        List<Extraadvt> extraadvts = civilt.getExtraadvts();
        if (extraadvts != null) {
            //pet_advocates
            civiltDTO.setPet_advocates(extraadvts.stream().filter(extraadvt -> extraadvt.getType() == 1).map(d -> {
                return new ExtraadvtDTO(d.getSrno(), d.getAdv_name(), d.getType());
            }).collect(Collectors.toList()));
            //res_advocatesresult
            civiltDTO.setRes_advocates(extraadvts.stream().filter(extraadvt -> extraadvt.getType() == 2).map(d -> {
                return new ExtraadvtDTO(d.getSrno(), d.getAdv_name(), d.getType());
            }).collect(Collectors.toList()));
        }
       if (civilt.getCourtt() != null) {
            Courtt courtt = civilt.getCourtt();
            civiltDTO.setCourtt(new CourttDTO(courtt.getCourt_no(), courtt.getBench_desc()));
        }

       //:::::::::::::::::Subject & categorization::::::::::::::::::::::
        SubjectMasterDTO subjectMasterDTO = this.getSubjectMasterDTO(civiltDTO);
         if(subjectMasterDTO!=null) {
           if(subjectMasterDTO.getSubject_name()!=null){
               civiltDTO.setCategorisation(subjectMasterDTO.getSubject_name());
               System.out.println("setCategorisation:::::::::::::"+subjectMasterDTO.getSubject_name());
           }
        }
        SubnatureOnetDTO subnatureOnetDTO =  getSubnatureOnetDTO(civiltDTO);
       if(subnatureOnetDTO!=null) {
           if(subnatureOnetDTO.getSubnature1_desc()!=null){
               civiltDTO.setCs_subject_2(subnatureOnetDTO.getSubnature1_desc());
           }
       }
        SubnatureTwoTDTO subnatureTwoT=getSubnatureTwoT(civiltDTO);
        if(subnatureTwoT!=null){
            if(subnatureTwoT.getSubnature2_desc()!=null){
                civiltDTO.setCss_subject_3(subnatureTwoT.getSubnature2_desc());
            }
        }
       SubnatureThreeTDTO subnatureThreeTDTO=getSubnatureThreeTDTO(civiltDTO);
        if(subnatureThreeTDTO!=null){
            if(subnatureThreeTDTO.getSubnature3_desc()!=null){
                civiltDTO.setCsss_subject_4(subnatureThreeTDTO.getSubnature3_desc());
            }
        }
        civiltDTO = OrgNameFunction.andThen(SubjectMaterFunction)
                .andThen(SubnatureOnetFunction)
                .andThen(SubnatureTwotFunction)
                .andThen(SubnatureThreetFunction)
                .apply(civiltDTO);

        civiltDTO = OrgNameFunction.andThen(SubjectMaterFunction)
                .andThen(SubnatureOnetFunction)
                .apply(civiltDTO);
        civiltDTO = JudgeNameFunction.apply(civiltDTO);
        return civiltDTO;
    }

    public void generateRelationshipInDspace(CiviltDTO civiltDTO) throws Exception {
        if (civiltDTO.getCivilt2ItemUUID() != null && civiltDTO.getCivilt2ItemUUID().size() != 0) {
            Item leftItem = itemService.find(this.context, civiltDTO.getDspaceToCisMapping().getDspaceobjectid());
            RelationshipType relationshipType = relationshipTypeService.find(this.context, 53);
            civiltDTO.getCivilt2ItemUUID().stream().forEach(d -> {
                try {
                    Item rightItem = itemService.find(this.context, civiltDTO.getDspaceToCisMapping().getDspaceobjectid());
                    if (leftItem != null && rightItem != null) {
                        relationshipService.create(this.context, leftItem, rightItem, relationshipType, 0, 0);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (AuthorizeException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
   public void updateMataDataBycino(CiviltDTO civiltDTO) throws Exception {


            Item item= itemService.findByCIno(this.context,civiltDTO.getCino());
        if(item != null) {
            System.out.println("item:::::::" + item.getID());
            itemService.clearMetadata(this.context, item, "casefile", "petitioner", "name", null);
            itemService.clearMetadata(this.context, item, "casefile", "respondent", "name", null);
            MataDataEnum exares_name = MataDataEnum.valueOf("res_name");
            exares_name.setCiviltDTO(civiltDTO);

            if (exares_name.getvalue() != null) {
                itemService.addMetadata(this.context, item, exares_name.getSchema(), exares_name.getElement(), exares_name.getQualifier(), null, exares_name.getvalue());
            }

            MataDataEnum extrarespondateenum = MataDataEnum.valueOf("extrarespondate");
            extrarespondateenum.setCiviltDTO(civiltDTO);
            if (extrarespondateenum.getvalue() != null) {
                Set<String> advSet = Arrays.stream(extrarespondateenum.getvalue().split("\\|\\|"))
                        .map(str -> str.trim()) // remove white-spaces
                        .collect(Collectors.toSet());
                advSet.forEach(d -> {
                    try {
                        if (d.trim().length() != 0) {
                            System.out.println("Extra Respondateenum::::::"+d +" cino::::"+civiltDTO.getCino());
                            itemService.addMetadata(this.context, item, extrarespondateenum.getSchema(), extrarespondateenum.getElement(), extrarespondateenum.getQualifier(), null, d);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
            }
            MataDataEnum pet_nameMataDataEnum = MataDataEnum.valueOf("pet_name");
            pet_nameMataDataEnum.setCiviltDTO(civiltDTO);
            if (pet_nameMataDataEnum.getvalue() != null)
                itemService.addMetadata(this.context, item, pet_nameMataDataEnum.getSchema(), pet_nameMataDataEnum.getElement(), pet_nameMataDataEnum.getQualifier(), null, pet_nameMataDataEnum.getvalue());

            MataDataEnum extrapetitionerMataDataEnum = MataDataEnum.valueOf("extrapetitioner");
            extrapetitionerMataDataEnum.setCiviltDTO(civiltDTO);
            if (extrapetitionerMataDataEnum.getvalue() != null) {
                Set<String> advSetpet_name = Arrays.stream(extrapetitionerMataDataEnum.getvalue().split("\\|\\|"))
                        .map(str -> str.trim()) // remove white-spaces
                        .collect(Collectors.toSet());
                advSetpet_name.forEach(d -> {
                    try {
                        if (d.trim().length() != 0) {
                            System.out.println("Extra Petitioner::::::"+d +" cino::::"+civiltDTO.getCino());
                            itemService.addMetadata(this.context, item, extrapetitionerMataDataEnum.getSchema(), extrapetitionerMataDataEnum.getElement(), extrapetitionerMataDataEnum.getQualifier(), null, d);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
            }
            DspaceToCisMapping dspaceToCisMapping = new DspaceToCisMapping();
            dspaceToCisMapping.setDspaceobjectid(item.getID());
            dspaceToCisMapping.setCino(civiltDTO.getCino());
            dspaceToCisMapping.setDate(new Date());
            dspaceToCisMapping.setCisobjecttype(1);
            orgNametService.save(this.contextCIS, dspaceToCisMapping);
            this.context.setFromtool(false);
            this.context.commit();
            this.contextCIS.commit();

        }

    }
    public void pushCiviltDTOTODspace(CiviltDTO civiltDTO) throws Exception {
        System.out.println("civiltDTO.getCi_cri()::: "+civiltDTO.getCi_cri());
        if (civiltDTO.getCourtt() != null) {
            Community community = null;
            try {
                if(civiltDTO.getCi_cri()==2){
                    community= communityService.searchByTitle(this.context, "Civil Cases");
                    civiltDTO.setNature("Civil");
                }
                else
                {
                    civiltDTO.setNature("Criminal");
                    community= communityService.searchByTitle(this.context, "Criminal Cases");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("community::::"+community.getName());
            if (community != null && civiltDTO.getReg_year() != null) {
                String collectionName = civiltDTO.getReg_year().toString();
                Collection collection = null;
                synchronized (collectionService) {
                    collection = collectionService.findCollectionByNameAndCommunityID(this.context, community, collectionName);
                    if (collection == null) {
                        try {
                            collection = collectionService.create(context, community);
                            collectionService.setMetadataSingleValue(context, collection, "dc", "title", null, null, collectionName.trim());
                            collectionService.update(context, collection);
                            //this.context.commit();
                        } catch (SQLException e) {
                            throw new RuntimeException("Unable to create new Collection under parent Community " + community.getID(), e);
                        } catch (AuthorizeException e) {
                            e.printStackTrace();
                        }
                    }
                    collection = collectionService.find(context, collection.getID());
                }
                WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
                Item item = workspaceItem.getItem();
                item.setArchived(true);
                item.setCino(civiltDTO.getCino());
                item.setOwningCollection(collection);
                item.setDiscoverable(true);
                item.setLastModified(new Date());
                itemService.clearMetadata(context, item, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
                CiviltDTO finalCiviltDTO = civiltDTO;
                Arrays.asList(CiviltDTO.class.getDeclaredFields()).stream().forEach(dx -> {
                    MataDataEnum mataDataEnum = null;
                    try {
                        mataDataEnum = MataDataEnum.valueOf(dx.getName());
                    } catch (IllegalArgumentException e) {

                    } catch (Exception ex) {

                    }

                    if (mataDataEnum != null) {
                        mataDataEnum.setCiviltDTO(finalCiviltDTO);
                        if (mataDataEnum.getvalue() != null) {
                            try {
                                if (mataDataEnum.isMultiple()) {
                                    MataDataEnum finalMataDataEnum = mataDataEnum;
                                    Set<String> advSet = Arrays.stream(mataDataEnum.getvalue().split("\\|\\|"))
                                            .map(str -> str.trim()) // remove white-spaces
                                            .collect(Collectors.toSet());
                                    advSet.forEach(d -> {
                                        try {
                                            if (d.trim().length() != 0) {
                                                itemService.addMetadata(this.context, item, finalMataDataEnum.getSchema(), finalMataDataEnum.getElement(), finalMataDataEnum.getQualifier(), null, d);
                                            }
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });

                                } else {
                                    itemService.addMetadata(this.context, item, mataDataEnum.getSchema(), mataDataEnum.getElement(), mataDataEnum.getQualifier(), null, mataDataEnum.getvalue());
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
                Item savedItem = installItemService.installItem(this.context, workspaceItem);
                System.out.println("Item ID::::::::::::::::::>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<:::::" + item.getID());
                DspaceToCisMapping dspaceToCisMapping = new DspaceToCisMapping();
                dspaceToCisMapping.setDspaceobjectid(savedItem.getID());
                dspaceToCisMapping.setCino(civiltDTO.getCino());
                dspaceToCisMapping.setDate(new Date());
                dspaceToCisMapping.setCisobjecttype(1);
                orgNametService.save(this.contextCIS, dspaceToCisMapping);
                this.context.setFromtool(false);
                this.context.commit();
                this.contextCIS.commit();
            }
            //converterUtil.getContextCIS().commit();
        }

    }
    public void pushCiviltDTORestTODspace(CiviltDTORest civiltDTORest) throws Exception {
        try {
            if (civiltDTORest.getBranch_id() != null) {
                Community community = communityService.searchByTitle(this.context, civiltDTORest.getBranch_id().trim());
                if (community != null && civiltDTORest.getReg_year() != null) {
                    String collectionName = civiltDTORest.getReg_year().toString();
                    Collection collection = null;
                    System.out.println("collectionName:::" + collectionName);
                    synchronized (collectionService) {
                        collection = collectionService.findCollectionByNameAndCommunityID(this.context, community, collectionName);
                        if (collection == null) {
                            try {
                                collection = collectionService.create(context, community);
                                collectionService.setMetadataSingleValue(context, collection, "dc", "title", null, null, collectionName.trim());
                                collectionService.update(context, collection);
                                this.context.commit();
                            } catch (Exception e) {
                                throw new Exception("Unable to create new Collection under parent Community " + community.getID(), e);
                            }
                        }
                        collection = collectionService.find(context, collection.getID());
                    }
                    WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
                    Item item = workspaceItem.getItem();
                    item.setArchived(true);
                    item.setOwningCollection(collection);
                    item.setDiscoverable(true);
                    item.setLastModified(new Date());
                    item.setCino(civiltDTORest.getCino());
                    itemService.clearMetadata(context, item, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
                    CiviltDTORest finalCiviltDTO = civiltDTORest;
                    Arrays.asList(CiviltDTORest.class.getDeclaredFields()).stream().forEach(dx -> {
                        MataDataEnumRest mataDataEnum = null;
                        try {
                            mataDataEnum = MataDataEnumRest.valueOf(dx.getName());
                        } catch (Exception ex) {
                        }
                        if (mataDataEnum != null) {
                            mataDataEnum.setCiviltDTO(finalCiviltDTO);
                            if (mataDataEnum.getvalue() != null) {
                                try {
                                    if (mataDataEnum.isMultiple()) {
                                        MataDataEnumRest finalMataDataEnum = mataDataEnum;
                                        Set<String> advSet = Arrays.stream(mataDataEnum.getvalue().split("\\|\\|"))
                                                .map(str -> str.trim()) // remove white-spaces
                                                .collect(Collectors.toSet());
                                        advSet.forEach(d -> {
                                            try {
                                                if (d.trim().length() != 0) {
                                                    itemService.addMetadata(this.context, item, finalMataDataEnum.getSchema(), finalMataDataEnum.getElement(), finalMataDataEnum.getQualifier(), null, d);
                                                }
                                            } catch (SQLException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });

                                    } else {
                                        itemService.addMetadata(this.context, item, mataDataEnum.getSchema(), mataDataEnum.getElement(), mataDataEnum.getQualifier(), null, mataDataEnum.getvalue());
                                    }
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
                    Item savedItem = installItemService.installItem(this.context, workspaceItem);
                    indexingService.indexContent(context, new IndexableItem(savedItem), true, false);
                    System.out.println("Item ID:::::::::::::::" + savedItem.getID() );
                    context.uncacheEntity(item);
                    if(civiltDTORest.getCivilt2connected() != null) {
                        RelationshipType relationshipType = relationshipTypeService.find(this.context, 53);
                        civiltDTORest.getCivilt2connected().stream().forEach(d -> {
                            try {
                                Item connectedItem = itemService.findByCIno(this.context, d.getCino());
                                relationshipService.create(this.context, savedItem, connectedItem, relationshipType, 0, 0);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            } catch (AuthorizeException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    indexingService.commit();
                }
                this.context.commit();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void pushConnectedCase(CiviltDTO civiltDTORest) throws Exception{
        System.out.println("civiltDTORest.getCino():::::"+civiltDTORest.getCino());
        Item item= itemService.findByCIno(this.context,civiltDTORest.getCino());
        if(civiltDTORest.getCivilt2connected() != null) {
            RelationshipType relationshipType = relationshipTypeService.find(this.context, 53);
            civiltDTORest.getCivilt2connected().stream().forEach(d -> {
                try {
                    Item connectedItem = itemService.findByCIno(this.context, d.getCino());
                    if(connectedItem != null) {
                        relationshipService.create(this.context, item, connectedItem, relationshipType, 0, 0);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (AuthorizeException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        DspaceToCisMapping dspaceToCisMapping = new DspaceToCisMapping();
        dspaceToCisMapping=orgNametService.findByCino(this.contextCIS,civiltDTORest.getCino());
        dspaceToCisMapping.setIsrelationshipdone(true);
        orgNametService.update(this.contextCIS, dspaceToCisMapping);
        this.context.setFromtool(false);
        this.context.commit();
        this.contextCIS.commit();
    }


    public OrgNametDTO getOrgNametDTO(CiviltDTO civiltdto) throws SQLException {
        try {
            OrgNamet orgNamet = orgNametService.findBYcivilt(this.contextCIS, civiltdto.getOrgid());
            if (orgNamet != null)
                return new OrgNametDTO(orgNamet.getOrgid(), orgNamet.getOrgname());
        } catch (Exception e) {

            //e.printStackTrace();
        }
        return null;
    }

    public JudgeNameDTO getJudGeNames(String[] jocode) throws SQLException {
        try {

            List<JudgeNamet> judGeName = orgNametService.findJudGeName(this.contextCIS, jocode);
            if (judGeName != null && judGeName.size() != 0) {
                String judgeJoin = judGeName.stream().map(d -> d.getJudge_name()).collect(Collectors.joining("||"));
                return new JudgeNameDTO(judgeJoin);
            } else {
                System.out.println("JudgeNamet found nulll");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
    public String getJudGeNamesByJudGeCode(String jodgecode) throws SQLException {
        StringBuffer JudGeNamesByffer=new StringBuffer();
        if(jodgecode.contains(",")){
            String jodgecodes[]=jodgecode.split(",");
            if(jodgecodes.length!=0) {
                int j = 0;
                for (String code : jodgecodes) {
                    if(code!=null&&!code.isEmpty()&&isDigitsOnly(code)) {
                        try {
                            JudgeNamet judGeName = orgNametService.findJudGeNameByJudgCode(this.contextCIS, code);
                            if (judGeName != null) {
                                if (j == 0) {
                                    JudGeNamesByffer.append(judGeName.getJudge_name());
                                } else {
                                    JudGeNamesByffer.append(" || " + judGeName.getJudge_name());
                                }
                            } else {
                                System.out.println("JudgeNamet found ::::: null");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    j++;
                }
                return JudGeNamesByffer.toString();
            }
        }else{
        try {
            if(jodgecode!=null &&!jodgecode.isEmpty()&&isDigitsOnly(jodgecode)) {
                JudgeNamet judGeName = orgNametService.findJudGeNameByJudgCode(this.contextCIS, jodgecode);
                if (judGeName != null) {
                    return judGeName.getJudge_name();
                } else {
                    System.out.println("JudgeNamet found nulll");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        return null;
    }
    public PoliceStnT getByPolistation(Integer pscode) throws SQLException {
        try {
            PoliceStnT policeStnT = orgNametService.findbypscode(this.contextCIS, pscode);
            return policeStnT;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getDisticBysistCode(String distcode) throws SQLException {
        try {

            DistrictT judGeName = orgNametService.findDistByDistc0de(this.contextCIS, distcode);
            if (judGeName != null) {
                return judGeName.getDist_name();
            } else {
               return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Naturet findCaseTypeFromNatureT(Short nature_cd) throws SQLException {
        try {
            return this.orgNametService.findCaseTypeByNatureT(this.contextCIS, nature_cd);

        } catch (Exception e) {

            //e.printStackTrace();
        }
        return null;

    }
    public List<Civaddresst> getExtrarespondate(Civilt civilt) throws SQLException {
        try {
            return civilt.getExtrarespondate();
        } catch (Exception e) {
        }
        return null;

    }
    public List<Civaddresst> getExtrapetitioner(Civilt civilt) throws SQLException {
        try {
            return civilt.getExtrapetitioner();
        } catch (Exception e) {
        }
        return null;

    }

    public SubnatureOnetDTO getSubnatureOnetDTO(CiviltDTO civiltdto) throws SQLException {
        try {
            SubnatureOnet subnatureOnet = orgNametService.getSubnatureOnet(this.contextCIS, civiltdto.getC_subject(), civiltdto.getCs_subject());
            if (subnatureOnet != null) {
                return new SubnatureOnetDTO(subnatureOnet.getCase_type_cd(), subnatureOnet.getNature_cd(), subnatureOnet.getSubnature1_cd(), subnatureOnet.getSubnature1_desc(), civiltdto);
            }
        } catch (Exception e) {

            //e.printStackTrace();
        }
        return null;
    }
    public FirNoAndFirYearAndDistrictAndPoliceNameDTO getFirNoAndFirYearAndDistrictAndPoliceNameDTO(String cino) throws SQLException {
        try {
            FirNoAndFirYearAndDistrictAndPoliceNameDTO dto = orgNametService.findFirNoandYearandDisticandPolicenameByCino(this.contextCIS, cino);
            if (dto != null) {
                return dto;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<String> GetPetName(String cino) throws SQLException {
        try {
            List<String> dto = orgNametService.GetExtraPet(this.contextCIS, cino);
            if (dto != null) {
                System.out.println("List Pet[]="+dto);
                return dto;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<String> GetResName(String cino) throws SQLException {
        try {
            List<String> dto = orgNametService.GetExtraRes(this.contextCIS, cino);
            if (dto != null) {
                System.out.println("List Res[]="+dto);
                return dto;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SubnatureTwoTDTO getSubnatureTwoT(CiviltDTO civiltdto) throws SQLException {
    try {


        SubnatureTwoT subnatureTwoT = orgNametService.getSubnatureTwoT(this.contextCIS, civiltdto.getC_subject(), civiltdto.getCs_subject(), civiltdto.getCss_subject());
        if (subnatureTwoT != null) {
            return new SubnatureTwoTDTO(subnatureTwoT.getCase_type_cd(), subnatureTwoT.getNature_cd(), subnatureTwoT.getSubnature1_cd(), subnatureTwoT.getSubnature2_cd(), subnatureTwoT.getSubnature2_desc(), civiltdto);
        } else {
            return null;
        }
    }catch (Exception e){
        e.printStackTrace();
    }
    return null;
    }

    public SubnatureThreeTDTO getSubnatureThreeTDTO(CiviltDTO civiltdto) throws SQLException {
       try {
           SubnatureThreeT subnatureThreeT = orgNametService.getSubnatureThreeT(this.contextCIS, civiltdto.getC_subject(), civiltdto.getCs_subject(), civiltdto.getCss_subject(), civiltdto.getCsss_subject());
           if (subnatureThreeT != null) {
            return new SubnatureThreeTDTO(subnatureThreeT.getCase_type_cd(), subnatureThreeT.getNature_cd(), subnatureThreeT.getSubnature1_cd(), subnatureThreeT.getSubnature2_cd(), subnatureThreeT.getSubnature3_cd(), subnatureThreeT.getSubnature3_desc(), civiltdto);
        }else{
             return null;
           }
       } catch (Exception e)
       {
           System.out.println("Error for getSubnatureThreeTDTO"+e.getMessage());
       }
       return null;
    }

    public SubjectMasterDTO getSubjectMasterDTO(CiviltDTO civiltdto) throws SQLException {

        try {
            SubjectMaster subjectMaster = orgNametService.getSubjectMaster(this.contextCIS, civiltdto.getC_subject());
            if (subjectMaster != null) {
                return new SubjectMasterDTO(subjectMaster.getSubject_name(), civiltdto);
            } else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public OrgNametService getOrgNametService() {
        return orgNametService;
    }

    public void setOrgNametService(OrgNametService orgNametService) {
        this.orgNametService = orgNametService;
    }

    public ContextCIS getContextCIS() {
        return contextCIS;
    }

    public void setContextCIS(ContextCIS contextCIS) {
        this.contextCIS = contextCIS;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public BlockingQueue<Civilt> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<Civilt> queue) {
        this.queue = queue;
    }

    public BlockingQueue<CiviltDTO> getQueueDTO() {
        return queueDTO;
    }

    public void setQueueDTO(BlockingQueue<CiviltDTO> queueDTO) {
        this.queueDTO = queueDTO;
    }

    public Predicate<CiviltDTO> getC_subjectPredicate() {
        return c_subjectPredicate;
    }

    public void setC_subjectPredicate(Predicate<CiviltDTO> c_subjectPredicate) {
        this.c_subjectPredicate = c_subjectPredicate;
    }

    public Predicate<CiviltDTO> getCs_subjectPredicate() {
        return cs_subjectPredicate;
    }

    public void setCs_subjectPredicate(Predicate<CiviltDTO> cs_subjectPredicate) {
        this.cs_subjectPredicate = cs_subjectPredicate;
    }

    public Predicate<CiviltDTO> getCss_subjectPredicate() {
        return css_subjectPredicate;
    }

    public void setCss_subjectPredicate(Predicate<CiviltDTO> css_subjectPredicate) {
        this.css_subjectPredicate = css_subjectPredicate;
    }

    public Predicate<CiviltDTO> getCsss_subjectPredicate() {
        return csss_subjectPredicate;
    }

    public void setCsss_subjectPredicate(Predicate<CiviltDTO> csss_subjectPredicate) {
        this.csss_subjectPredicate = csss_subjectPredicate;
    }

    public Predicate<CiviltDTO> getRgidPredicate() {
        return rgidPredicate;
    }

    public void setRgidPredicate(Predicate<CiviltDTO> rgidPredicate) {
        this.rgidPredicate = rgidPredicate;
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public CommunityService getCommunityService() {
        return communityService;
    }

    public void setCommunityService(CommunityService communityService) {
        this.communityService = communityService;
    }

    public Function<CiviltDTO, CiviltDTO> getOrgNameFunction() {
        return OrgNameFunction;
    }

    public void setOrgNameFunction(Function<CiviltDTO, CiviltDTO> orgNameFunction) {
        OrgNameFunction = orgNameFunction;
    }

    public Function<CiviltDTO, CiviltDTO> getSubjectMaterFunction() {
        return SubjectMaterFunction;
    }

    public void setSubjectMaterFunction(Function<CiviltDTO, CiviltDTO> subjectMaterFunction) {
        SubjectMaterFunction = subjectMaterFunction;
    }

    public Function<CiviltDTO, CiviltDTO> getSubnatureOnetFunction() {
        return SubnatureOnetFunction;
    }

    public void setSubnatureOnetFunction(Function<CiviltDTO, CiviltDTO> subnatureOnetFunction) {
        SubnatureOnetFunction = subnatureOnetFunction;
    }

    public Function<CiviltDTO, CiviltDTO> getSubnatureTwotFunction() {
        return SubnatureTwotFunction;
    }

    public void setSubnatureTwotFunction(Function<CiviltDTO, CiviltDTO> subnatureTwotFunction) {
        SubnatureTwotFunction = subnatureTwotFunction;
    }

    public Function<CiviltDTO, CiviltDTO> getSubnatureThreetFunction() {
        return SubnatureThreetFunction;
    }

    public void setSubnatureThreetFunction(Function<CiviltDTO, CiviltDTO> subnatureThreetFunction) {
        SubnatureThreetFunction = subnatureThreetFunction;
    }

    public WorkspaceItemService getWorkspaceItemService() {
        return workspaceItemService;
    }

    public void setWorkspaceItemService(WorkspaceItemService workspaceItemService) {
        this.workspaceItemService = workspaceItemService;
    }

    public MetadataValueService getMetadataValueService() {
        return metadataValueService;
    }

    public void setMetadataValueService(MetadataValueService metadataValueService) {
        this.metadataValueService = metadataValueService;
    }

    public ItemService getItemService() {
        return itemService;
    }

    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }

    public InstallItemService getInstallItemService() {
        return installItemService;
    }

    public void setInstallItemService(InstallItemService installItemService) {
        this.installItemService = installItemService;
    }

    public CollectionService getCollectionService() {
        return collectionService;
    }

    public void setCollectionService(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    public boolean isMigrateConnectedCase() {
        return migrateConnectedCase;
    }

    public void setMigrateConnectedCase(boolean migrateConnectedCase) {
        this.migrateConnectedCase = migrateConnectedCase;
    }

    public String getCisUri() {
        return cisUri;
    }

    public void setCisUri(String cisUri) {
        this.cisUri = cisUri;
    }

    public String getCauselistDate() {
        return causelistDate;
    }

    public void setCauselistDate(String causelistDate) {
        this.causelistDate = causelistDate;
    }

    public IndexingService getIndexingService() {
        return indexingService;
    }

    public void setIndexingService(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    public String getCino() {
        return cino;
    }

    public void setCino(String cino) {
        this.cino = cino;
    }

    public RelationshipService getRelationshipService() {
        return relationshipService;
    }

    public void setRelationshipService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    public RelationshipTypeService getRelationshipTypeService() {
        return relationshipTypeService;
    }

    public void setRelationshipTypeService(RelationshipTypeService relationshipTypeService) {
        this.relationshipTypeService = relationshipTypeService;
    }
    public static boolean isDigitsOnly(String str) {
        // Using regular expression to check if the string contains only digits
        return str.matches("[0-9]+");
    }

    public EPerson getePerson() {
        return ePerson;
    }

    public void setePerson(EPerson ePerson) {
        this.ePerson = ePerson;
    }
}
