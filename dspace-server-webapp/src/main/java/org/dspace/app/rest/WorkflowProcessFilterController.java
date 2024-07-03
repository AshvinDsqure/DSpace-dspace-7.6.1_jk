///**
// * The contents of this file are subject to the license and copyright
// * detailed in the LICENSE and NOTICE files at the root of the source
// * tree and available online at
// * <p>
// * http://www.dspace.org/license/
// */
//package org.dspace.app.rest;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.dspace.app.rest.Enum.WorkFlowType;
//import org.dspace.app.rest.converter.EPersonConverter;
//import org.dspace.app.rest.converter.WorkFlowProcessConverter;
//import org.dspace.app.rest.model.WorkFlowProcessFilterRest;
//import org.dspace.app.rest.model.WorkFlowProcessRest;
//import org.dspace.app.rest.utils.ContextUtil;
//import org.dspace.app.rest.utils.Utils;
//import org.dspace.content.WorkFlowProcessMaster;
//import org.dspace.content.WorkflowProcess;
//import org.dspace.content.service.*;
//import org.dspace.core.Context;
//import org.dspace.eperson.service.EPersonService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.hateoas.Link;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
///**
// * Controller to upload bitstreams to a certain bundle, indicated by a uuid in the request
// * Usage: POST /api/core/bundles/{uuid}/bitstreams (with file and properties of file in request)
// * Example:
// * <pre>
// * {@code
// * curl https://<dspace.server.url>/api/core/bundles/d3599177-0408-403b-9f8d-d300edd79edb/bitstreams
// *  -XPOST -H 'Content-Type: multipart/form-data' \
// *  -H 'Authorization: Bearer eyJhbGciOiJI...' \
// *  -F "file=@Downloads/test.html" \
// *  -F 'properties={ "name": "test.html", "metadata": { "dc.description": [ { "value": "example file", "language": null,
// *          "authority": null, "confidence": -1, "place": 0 } ]}, "bundleName": "ORIGINAL" };type=application/json'
// * }
// * </pre>
// */
//@RestController
//@RequestMapping("/api/" + WorkFlowProcessRest.CATEGORY + "/" + WorkFlowProcessRest.PLURAL_NAME + "/filter")
//public class WorkflowProcessFilterController {
//
//    @Autowired
//    WorkflowProcessService workflowProcessService;
//
//
//    @Autowired
//    WorkFlowProcessMasterService workFlowProcessMasterService;
//    @Autowired
//    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
//    @Autowired
//    WorkFlowProcessOutwardDetailsService workFlowProcessOutwardDetailsService;
//
//    @Autowired
//    WorkFlowProcessInwardDetailsService workFlowProcessInwardDetailsService;
//
//    @Autowired
//    EPersonConverter ePersonConverter;
//    @Autowired
//    EPersonService ePersonService;
//    @Autowired
//    private DiscoverableEndpointsService discoverableEndpointsService;
//
//
//    @Autowired
//    WorkFlowProcessConverter workFlowProcessConverter;
//
//    private static final Logger log = LogManager.getLogger();
//
//    @Autowired
//    protected Utils utils;
//    @Autowired
//    protected ItemService itemService;
//
//    public void afterPropertiesSet() throws Exception {
//        this.discoverableEndpointsService.register(this,
//                Arrays.asList(new Link[]{Link.of("/api/workflowprocesse", "workflowprocesses")}));
//    }
//
//    /**
//     * Method to upload a Bitstream to a Bundle with the given UUID in the URL. This will create a Bitstream with the
//     * file provided in the request and attach this to the Item that matches the UUID in the URL.
//     * This will only work for uploading one file, any extra files will silently be ignored
//     *
//     * @return The created BitstreamResource
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/filterbyData")
//    public List<WorkFlowProcessRest> filter(HttpServletRequest request, @RequestBody WorkFlowProcessFilterRest rest, Pageable pageable
//    ) {
//        try {
//            Context context = ContextUtil.obtainContext(request);
//            context.turnOffAuthorisationSystem();
//            HashMap<String, String> map = new HashMap<>();
//            if (rest.getPriorityRest() != null && rest.getPriorityRest().getUuid() != null) {
//                map.put("priority", rest.getPriorityRest().getUuid());
//            }
//            if (rest.getWorkflowStatusRest() != null && rest.getWorkflowStatusRest().getUuid() != null) {
//                map.put("status", rest.getWorkflowStatusRest().getUuid());
//            }
//            if (rest.getWorkflowTypeRest() != null && rest.getWorkflowTypeRest().getUuid() != null) {
//                map.put("type", rest.getWorkflowTypeRest().getUuid());
//            }
//            if (rest.getDepartmentRest() != null && rest.getDepartmentRest().getUuid() != null) {
//                map.put("department", rest.getWorkflowTypeRest().getUuid());
//            }
//            if (rest.getePersonRest() != null && rest.getePersonRest().getUuid() != null) {
//                map.put("user", rest.getePersonRest().getUuid());
//            }
//            if (rest.getSubject() != null) {
//                map.put("subject", rest.getSubject());
//            }
//            if (rest.getInward() != null) {
//             /* WorkFlowProcessInwardDetails inward=workFlowProcessInwardDetailsService.getByInwardNumber(context,rest.getInward());
//              if(inward!=null) {*/
//                map.put("inward", rest.getInward());
//                /*}*/
//            }
//            if (rest.getYear() != null) {
//             /* WorkFlowProcessInwardDetails inward=workFlowProcessInwardDetailsService.getByInwardNumber(context,rest.getInward());
//              if(inward!=null) {*/
//                map.put("inward", rest.getYear());
//                /*}*/
//            }
//            if (rest.getOutward() != null) {
//               /* WorkFlowProcessOutwardDetails outward=workFlowProcessOutwardDetailsService.getByOutwardNumber(context,rest.getOutward());
//                if(outward!=null) {*/
//                map.put("outward", rest.getOutward());
//                /*}*/
//            }
//            System.out.println(map);
//            List<WorkflowProcess> list = workflowProcessService.Filter(context, map, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
//            List<WorkFlowProcessRest> rests = list.stream().map(d -> {
//                return workFlowProcessConverter.convertFilter(d, utils.obtainProjection());
//            }).collect(Collectors.toList());
//            return rests;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.POST, value = "/filterbyInwardAndOutWard")
//    public Page<WorkFlowProcessRest> filterbyInwardAndOutWard(HttpServletRequest request, @RequestBody WorkFlowProcessFilterRest rest, Pageable pageable) {
//        try {
//            System.out.println("::::::::::::::::::::start filterbyInwardAndOutWard :::::::::::::::::::");
//            Context context = ContextUtil.obtainContext(request);
//            context.turnOffAuthorisationSystem();
//            HashMap<String, String> map = new HashMap<>();
//            if (rest.getPriorityRest() != null && rest.getPriorityRest().getUuid() != null) {
//                map.put("priority", rest.getPriorityRest().getUuid());
//            }
//            if (rest.getWorkflowStatusRest() != null && rest.getWorkflowStatusRest().getUuid() != null) {
//                map.put("status", rest.getWorkflowStatusRest().getUuid());
//            }
//            if (rest.getWorkflowTypeRest() != null && rest.getWorkflowTypeRest().getUuid() != null) {
//                map.put("type", rest.getWorkflowTypeRest().getUuid());
//            }
//            if (rest.getDepartmentRest() != null && rest.getDepartmentRest().getUuid() != null) {
//                map.put("department", rest.getWorkflowTypeRest().getUuid());
//            }
//            if (rest.getCategoryRest() != null && rest.getCategoryRest().getUuid() != null) {
//                map.put("categoryRest", rest.getCategoryRest().getUuid());
//            }
//            if (rest.getSubcategoryRest() != null && rest.getSubcategoryRest().getUuid() != null) {
//                map.put("subcategoryRest", rest.getSubcategoryRest().getUuid());
//            }
//            if (rest.getOfficeRest() != null && rest.getOfficeRest().getUuid() != null) {
//                map.put("officeRest", rest.getOfficeRest().getUuid());
//            }
//            if (rest.getInwardmodeRest() != null && rest.getInwardmodeRest().getUuid() != null) {
//                map.put("inwardmodeRest", rest.getInwardmodeRest().getUuid());
//            }
//            if (rest.getOutwardmodeRest() != null && rest.getOutwardmodeRest().getUuid() != null) {
//                map.put("outwardmodeRest", rest.getOutwardmodeRest().getUuid());
//            }
//            if (rest.getOutwardmediumRest() != null && rest.getOutwardmediumRest().getUuid() != null) {
//                map.put("outwardmedium", rest.getOutwardmediumRest().getUuid());
//            }
//            if (rest.getDesignationRest() != null && rest.getDesignationRest().getUuid() != null) {
//                map.put("designation", rest.getDesignationRest().getUuid());
//            }
//            if (rest.getePersonRest() != null && rest.getePersonRest().getUuid() != null) {
//                map.put("user", rest.getePersonRest().getUuid());
//            }
//            //text
//            if (rest.getSubject() != null) {
//                map.put("subject", rest.getSubject());
//            }
//            if (rest.getInwarddate() != null) {
//                map.put("inwarddate", rest.getInwarddate());
//            }
//            if (rest.getOutwarddate() != null) {
//                map.put("outwarddate", rest.getOutwarddate());
//            }
//            if (rest.getReceiveddate() != null) {
//                map.put("receiveddate", rest.getReceiveddate());
//            }
//            if (rest.getUsername() != null) {
//                map.put("username", rest.getUsername());
//            }
//            if (rest.getSendername() != null) {
//                map.put("sendername", rest.getSendername());
//            }
//            if (rest.getSenderphonenumber() != null) {
//                map.put("senderphonenumber", rest.getSenderphonenumber());
//            }
//            if (rest.getSenderaddress() != null) {
//                map.put("senderaddress", rest.getSenderaddress());
//            }
//            if (rest.getSendercity() != null) {
//                map.put("sendercity", rest.getSendercity());
//            }
//            if (rest.getSendercountry() != null) {
//                map.put("sendercountry", rest.getSendercountry());
//            }
//            if (rest.getSenderpincode() != null) {
//                map.put("senderpincode", rest.getSenderpincode());
//            }
//            if (rest.getInward() != null) {
//                map.put("inward", rest.getInward());
//            }
//            if (rest.getOutward() != null) {
//                map.put("outward", rest.getOutward());
//            }
//            System.out.println(map);
//            int count = workflowProcessService.countfilterInwarAndOutWard(context, map, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
//            List<WorkflowProcess> list = workflowProcessService.filterInwarAndOutWard(context, map, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
//            List<WorkFlowProcessRest> rests = list.stream().map(d -> {
//                return workFlowProcessConverter.convertFilter(d, utils.obtainProjection());
//            }).collect(Collectors.toList());
//            System.out.println("::::::::::::::::::::stop filterbyInwardAndOutWard :::::::::::::::::::");
//            return new PageImpl(rests, pageable, count);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value = "/getFilterPerameter")
//    public HashMap<String, String> getFilterdate(HttpServletRequest request) {
//        try {
//            System.out.println("in getFilterPerameter ");
//            Context context = ContextUtil.obtainContext(request);
//            context.turnOffAuthorisationSystem();
//            HashMap<String, String> map = new HashMap<>();
//            map.put("Priority", "dropdown");
//            map.put("Status", "dropdown");
//            map.put("Department", "dropdown");
//            map.put("Subject", "text");
//            map.put(WorkFlowType.INWARD.getAction() + " Number", "text");
//            map.put(WorkFlowType.OUTWARED.getAction() + " Number", "text");
//            map.put("Year", "text");
//            System.out.println("out getFilterPerameter ");
//            return map;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value = "/getFilterPerameterByInwardAndOutWard")
//    public HashMap<String, String> getFilterPerameterByInwardAndOutWard(HttpServletRequest request) {
//        try {
//            System.out.println("in getFilterPerameterByInwardAndOutWard ");
//            HashMap<String, String> map = new HashMap<>();
//            map.put("Priority", "dropdown");
//            map.put("Status", "dropdown");
//            map.put("Department", "dropdown");
//            map.put("Subject", "text");
//            map.put(WorkFlowType.INWARD.getAction() + " Number", "text");
//            map.put(WorkFlowType.OUTWARED.getAction() + " Number", "text");
//            map.put("Inward Date", "text");
//            map.put("Outward Date", "text");
//            map.put("Received Date", "text");
//            map.put("Category", "dropdown");
//            map.put("Sub Category", "dropdown");
//            map.put("Letter Category", "dropdown");
//            map.put("Office", "dropdown");
//            map.put("Inward Mode", "dropdown");
//            map.put("Outward Mode", "dropdown");
//            map.put("Outward Medium", "dropdown");
//            map.put("Designation", "dropdown");
//            map.put("User Name", "text");
//            map.put("Sender/Recipient Name", "text");
//            map.put("Sender/Recipient Email", "text");
//            map.put("Sender/Recipient Phone Number", "text");
//            map.put("Sender/Recipient Organization", "text");
//            map.put("Sender/Recipient Address", "text");
//            map.put("Sender/Recipient City", "text");
//            map.put("Sender/Recipient Country", "text");
//            map.put("Sender/Recipient Pin code", "text");
//
//            System.out.println("out getFilterPerameterByInwardAndOutWard ");
//            return map;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value = "/getFilterPerameterSearch")
//    public HashMap<String, String> getFilterPerameterSearch(HttpServletRequest request) {
//        try {
//            System.out.println("in getFilterPerameterSearch ");
//            Context context = ContextUtil.obtainContext(request);
//            context.turnOffAuthorisationSystem();
//            HashMap<String, String> map = new HashMap<>();
//            map.put("Priority", "dropdown");
//            map.put("Status", "dropdown");
//            map.put("Department", "dropdown");
//            map.put("Subject", "text");
//            map.put(WorkFlowType.INWARD.getAction() + " Number", "text");
//            map.put(WorkFlowType.OUTWARED.getAction() + " Number", "text");
//            map.put(WorkFlowType.DRAFT.getAction(), "text");
//            System.out.println("out getFilterPerameter ");
//            return map;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value = "/getCountsDashboard")
//    public HashMap<String, HashMap<String, Integer>> getCountsDashboard(HttpServletRequest request) {
//        try {
//            System.out.println("in getCountsDashboard ");
//            Context context = ContextUtil.obtainContext(request);
//            context.turnOffAuthorisationSystem();
//            UUID userid = context.getCurrentUser().getID();
//            UUID Lowid = null;
//            UUID Mediumid = null;
//            UUID Highid = null;
//            UUID inwardid = null;
//            UUID outwardid = null;
//            UUID draftid = null;
//            WorkFlowProcessMaster PriorityMaster = workFlowProcessMasterService.findByName(context, "Priority");
//            if (PriorityMaster != null) {
//                Lowid = workFlowProcessMasterValueService.findByName(context, "Low", PriorityMaster).getID();
//                Mediumid = workFlowProcessMasterValueService.findByName(context, "Medium", PriorityMaster).getID();
//                Highid = workFlowProcessMasterValueService.findByName(context, "High", PriorityMaster).getID();
//            }
//            inwardid = WorkFlowType.INWARD.getUserTypeFromMasterValue(context).get().getID();
//            draftid = WorkFlowType.DRAFT.getUserTypeFromMasterValue(context).get().getID();
//            outwardid = WorkFlowType.OUTWARED.getUserTypeFromMasterValue(context).get().getID();
//
//            UUID tSuspendid = WorkFlowStatus.SUSPEND.getUserTypeFromMasterValue(context).get().getID();
//            UUID tCloseid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
//            UUID tInProgressid = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getID();
//            UUID tReferid = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getID();
//            UUID parkedid = WorkFlowStatus.PARKED.getUserTypeFromMasterValue(context).get().getID();
//            UUID createdid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
//
//            //inward Map
//            HashMap<String, Integer> mapInward = new HashMap<>();
//            mapInward.put("Medium", workflowProcessService.countByTypeAndPriority(context, inwardid, Mediumid, userid));
//            mapInward.put("Low", workflowProcessService.countByTypeAndPriority(context, inwardid, Lowid, userid));
//            mapInward.put("High", workflowProcessService.countByTypeAndPriority(context, inwardid, Highid, userid));
//            //status
//            mapInward.put("Suspend", workflowProcessService.countByTypeAndStatus(context, inwardid, tSuspendid, userid));
//            mapInward.put("Close", workflowProcessService.countByTypeAndStatus(context, inwardid, tCloseid, userid));
//            mapInward.put("InProgress", workflowProcessService.countByTypeAndStatus(context, inwardid, tInProgressid, userid));
//            mapInward.put("Refer", workflowProcessService.countByTypeAndStatus(context, inwardid, tReferid, userid));
//
//            //Outward Map
//            HashMap<String, Integer> mapOutward = new HashMap<>();
//            mapOutward.put("Medium", workflowProcessService.countByTypeAndPriority(context, outwardid, Mediumid, userid));
//            mapOutward.put("Low", workflowProcessService.countByTypeAndPriority(context, outwardid, Lowid, userid));
//            mapOutward.put("High", workflowProcessService.countByTypeAndPriority(context, outwardid, Highid, userid));
//            //status
//            mapOutward.put("Suspend", workflowProcessService.countByTypeAndStatus(context, outwardid, tSuspendid, userid));
//            mapOutward.put("Close", workflowProcessService.countByTypeAndStatus(context, outwardid, tCloseid, userid));
//            mapOutward.put("InProgress", workflowProcessService.countByTypeAndStatus(context, outwardid, tInProgressid, userid));
//            mapOutward.put("Refer", workflowProcessService.countByTypeAndStatus(context, outwardid, tReferid, userid));
//
//            //Draft Map
//            HashMap<String, Integer> mapDraft = new HashMap<>();
//            mapDraft.put("Medium", workflowProcessService.countByTypeAndPriority(context, draftid, Mediumid, userid));
//            mapDraft.put("Low", workflowProcessService.countByTypeAndPriority(context, draftid, Lowid, userid));
//            mapDraft.put("High", workflowProcessService.countByTypeAndPriority(context, draftid, Highid, userid));
//            mapDraft.put("Suspend", workflowProcessService.countByTypeAndStatus(context, draftid, tSuspendid, userid));
//            mapDraft.put("Close", workflowProcessService.countByTypeAndStatus(context, draftid, tCloseid, userid));
//            mapDraft.put("InProgress", workflowProcessService.countByTypeAndStatus(context, draftid, tInProgressid, userid));
//            mapDraft.put("Refer", workflowProcessService.countByTypeAndStatus(context, draftid, tReferid, userid));
//            mapDraft.put("parked", workflowProcessService.countByTypeAndStatus(context, draftid, parkedid, userid));
//            mapDraft.put("created", workflowProcessService.countByTypeAndStatusNotwoner(context, draftid, createdid, userid));
//
//            HashMap<String, HashMap<String, Integer>> maps = new HashMap<>();
//            maps.put(WorkFlowType.INWARD.getAction(), mapInward);
//            maps.put(WorkFlowType.OUTWARED.getAction(), mapOutward);
//            maps.put(WorkFlowType.DRAFT.getAction(), mapDraft);
//
//            System.out.println("out getCountsDashbord ");
//            return maps;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    @RequestMapping(method = RequestMethod.POST, value = "/addContry")
//    public ContryDTO addContry(HttpServletRequest request, @RequestBody ContryDTO rest) {
//        try {
//            ContryDTO obj=new ContryDTO();
//            Context context = ContextUtil.obtainContext(request);
//            if (rest.getContryname() != null) {
//                Country c = new Country();
//                c.setCountryname(rest.getContryname());
//                Country cc = countryService.create(context, c);
//                obj.setContryuuid(cc.getID().toString());
//                obj.setContryname(cc.getCountryname());
//                context.commit();
//                return obj;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.POST, value = "/addVip")
//    public VipDTO addVip(HttpServletRequest request, @RequestBody VipDTO rest) {
//        try {
//            VipDTO obj=new VipDTO();
//            Context context = ContextUtil.obtainContext(request);
//            if (rest.getVip() != null) {
//                Vip c = new Vip();
//                c.setVip(rest.getVip());
//                Vip cc = vipService.create(context, c);
//                obj.setVipuuid(cc.getID().toString());
//                obj.setVip(cc.getVip());
//                context.commit();
//                return obj;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    @RequestMapping(method = RequestMethod.POST, value = "/addVipName")
//    public VipDTO addVipName(HttpServletRequest request, @RequestBody VipDTO rest) {
//        try {
//            VipDTO obj=new VipDTO();
//            Context context = ContextUtil.obtainContext(request);
//            if (rest.getVipname() != null&& rest.getVipuuid()!=null) {
//                Vip v=vipService.find(context,UUID.fromString(rest.getVipuuid()));
//                VipName c = new VipName();
//                c.setVipname(rest.getVipname());
//                c.setVip(v);
//                VipName cc = vipNameService.create(context, c);
//                obj.setVipnameuuid(cc.getID().toString());
//                obj.setVipname(cc.getVipname());
//                context.commit();
//                return obj;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.POST, value = "/addCategory")
//    public CategoryDTO addCategory(HttpServletRequest request, @RequestBody CategoryDTO rest) {
//        try {
//            CategoryDTO obj=new CategoryDTO();
//            Context context = ContextUtil.obtainContext(request);
//            if (rest.getCategoryname() != null) {
//                Category c = new Category();
//                c.setCategoryname(rest.getCategoryname());
//                Category cc = categoryService.create(context, c);
//                obj.setCategoryuuid(cc.getID().toString());
//                obj.setCategoryname(cc.getCategoryname());
//                context.commit();
//                return obj;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    @RequestMapping(method = RequestMethod.POST, value = "/addSubCategory")
//    public CategoryDTO addSubCategory(HttpServletRequest request, @RequestBody CategoryDTO rest) {
//        try {
//            CategoryDTO obj=new CategoryDTO();
//            Context context = ContextUtil.obtainContext(request);
//            if (rest.getCategoryuuid() != null&& rest.getSubcategoryname()!=null) {
//                Category v=categoryService.find(context,UUID.fromString(rest.getCategoryuuid()));
//                SubCategory c = new SubCategory();
//                c.setSubcategoryname(rest.getSubcategoryname());
//                c.setCategory(v);
//                SubCategory cc = subcategoryService.create(context, c);
//                obj.setSubcategoryuuid(cc.getID().toString());
//                obj.setSubcategoryname(cc.getSubcategoryname());
//                context.commit();
//                return obj;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    @RequestMapping(method = RequestMethod.GET, value = "/getAllCategory")
//    public List<CategoryDTO> getAllCategory(HttpServletRequest request) {
//        try {
//            Context context = ContextUtil.obtainContext(request);
//            List<Category> list = categoryService.getAll(context);
//            List<CategoryDTO>rest=list.stream().map(d->{
//                CategoryDTO contryDTO=new CategoryDTO();
//                contryDTO.setCategoryname(d.getCategoryname());
//                contryDTO.setCategoryuuid(d.getID().toString());
//                return contryDTO;
//            }).collect(Collectors.toList());
//            return rest;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    @RequestMapping(method = RequestMethod.GET, value = "/getSubCategoryByCategoryID")
//    public List<CategoryDTO> getSubCategoryByCategoryID(HttpServletRequest request,@Parameter(value = "categoryid", required = true) String categoryid) {
//        try {
//            Context context = ContextUtil.obtainContext(request);
//            List<SubCategory> list = subcategoryService.getByCountryId(context,UUID.fromString(categoryid));
//            List<CategoryDTO>rest=list.stream().map(d->{
//                CategoryDTO contryDTO=new CategoryDTO();
//                contryDTO.setCategoryname(d.getCategory().getCategoryname());
//                contryDTO.setSubcategoryname(d.getSubcategoryname());
//                contryDTO.setCategoryuuid(d.getCategory().getID().toString());
//                contryDTO.setSubcategoryuuid(d.getID().toString());
//                return contryDTO;
//            }).collect(Collectors.toList());
//            return rest;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//   @RequestMapping(method = RequestMethod.GET, value = "/getAllVip")
//    public List<VipDTO> getAllVip(HttpServletRequest request) {
//        try {
//            Context context = ContextUtil.obtainContext(request);
//            List<Vip> list = vipService.getAll(context);
//            List<VipDTO>rest=list.stream().map(d->{
//                VipDTO contryDTO=new VipDTO();
//                contryDTO.setVip(d.getVip());
//                contryDTO.setVipuuid(d.getID().toString());
//                return contryDTO;
//            }).collect(Collectors.toList());
//            return rest;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    @RequestMapping(method = RequestMethod.GET, value = "/getVipNameByVipid")
//    public List<VipDTO> getVipNameByVipid(HttpServletRequest request,@Parameter(value = "vipid", required = true) String vipid) {
//        try {
//            Context context = ContextUtil.obtainContext(request);
//            List<VipName> list = vipNameService.getByCountryId(context,UUID.fromString(vipid));
//            List<VipDTO>rest=list.stream().map(d->{
//                VipDTO contryDTO=new VipDTO();
//                contryDTO.setVipname(d.getVipname());
//                contryDTO.setVipnameuuid(d.getID().toString());
//                contryDTO.setVip(d.getVip().getVip());
//                contryDTO.setVipuuid(d.getVip().getID().toString());
//                return contryDTO;
//            }).collect(Collectors.toList());
//            return rest;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.POST, value = "/addState")
//    public ContryDTO addState(HttpServletRequest request, @RequestBody ContryDTO rest) {
//        try {
//            ContryDTO obj=new ContryDTO();
//            Context context = ContextUtil.obtainContext(request);
//            if (rest.getStatename() != null && rest.getContryuuid() != null) {
//                UUID uuid = UUID.fromString(rest.getContryuuid());
//                Country country = countryService.find(context, uuid);
//                if (country != null) {
//                    State state = new State();
//                    state.setStatename(rest.getStatename());
//                    state.setCountry(country);
//                    State state1 = stateService.create(context, state);
//                    obj.setStateuuid(state1.getID().toString());
//                    obj.setStatename(state1.getStatename());
//                    context.commit();
//                }
//                context.commit();
//                return obj;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.POST, value = "/addCity")
//    public ContryDTO addCity(HttpServletRequest request, @RequestBody ContryDTO rest) {
//        try {
//            ContryDTO obj=new ContryDTO();
//            Context context = ContextUtil.obtainContext(request);
//            if (rest.getCityname() != null && rest.getStateuuid() != null) {
//                UUID uuid = UUID.fromString(rest.getStateuuid());
//                State state = stateService.find(context, uuid);
//                if (state != null) {
//                    City city = new City();
//                    city.setCityname(rest.getCityname());
//                    city.setState(state);
//                    City city1 = cityService.create(context, city);
//                    obj.setCityuuid(city1.getID().toString());
//                    obj.setCityname(city1.getCityname());
//                    context.commit();
//                    return obj;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value = "/getAllCountries")
//    public List<ContryDTO> getContryAll(HttpServletRequest request) {
//        try {
//            Context context = ContextUtil.obtainContext(request);
//            List<Country> list = countryService.getAll(context);
//            List<ContryDTO>rest=list.stream().map(d->{
//                ContryDTO contryDTO=new ContryDTO();
//                contryDTO.setContryname(d.getCountryname());
//                contryDTO.setContryuuid(d.getID().toString());
//                return contryDTO;
//            }).collect(Collectors.toList());
//            return rest;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    @RequestMapping(method = RequestMethod.GET, value = "/getStateByContryid")
//    public List<ContryDTO> getStateByContryid(HttpServletRequest request,@Parameter(value = "countyid", required = true) String countyid) {
//        try {
//            Context context = ContextUtil.obtainContext(request);
//            List<State> list = stateService.getByCountryId(context,UUID.fromString(countyid));
//            List<ContryDTO>rest=list.stream().map(d->{
//                ContryDTO contryDTO=new ContryDTO();
//                contryDTO.setStatename(d.getStatename());
//                contryDTO.setStateuuid(d.getID().toString());
//                contryDTO.setContryname(d.getCountry().getCountryname());
//                contryDTO.setContryuuid(d.getCountry().getID().toString());
//                return contryDTO;
//            }).collect(Collectors.toList());
//            return rest;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    @RequestMapping(method = RequestMethod.GET, value = "/getCityByStateid")
//    public List<ContryDTO> getCityByStateid(HttpServletRequest request,@Parameter(value = "stateid", required = true) String stateid) {
//        try {
//            System.out.println("in Citys ");
//            Context context = ContextUtil.obtainContext(request);
//            List<City> list = cityService.getCityByStateid(context,UUID.fromString(stateid));
//            List<ContryDTO>rest=list.stream().map(d->{
//                ContryDTO contryDTO=new ContryDTO();
//                contryDTO.setCityname(d.getCityname());
//                contryDTO.setCityuuid(d.getID().toString());
//                contryDTO.setStatename(d.getState().getStatename());
//                contryDTO.setStateuuid(d.getState().getID().toString());
//                contryDTO.setContryname(d.getState().getCountry().getCountryname());
//                contryDTO.setContryuuid(d.getState().getCountry().getID().toString());
//                return contryDTO;
//            }).collect(Collectors.toList());
//            return rest;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value = "/citySearchByState")
//    public List<ContryDTO> citySearchByState(HttpServletRequest request,@Parameter(value = "stateid", required = true) String stateid,@Parameter(value = "searchcity", required = true) String searchcity) {
//        try {
//            System.out.println("in Citys ");
//            Context context = ContextUtil.obtainContext(request);
//            List<City> list = cityService.getCityByStateid(context,UUID.fromString(stateid),searchcity);
//            List<ContryDTO>rest=list.stream().map(d->{
//                ContryDTO contryDTO=new ContryDTO();
//                contryDTO.setCityname(d.getCityname());
//                contryDTO.setCityuuid(d.getID().toString());
//                contryDTO.setStatename(d.getState().getStatename());
//                contryDTO.setStateuuid(d.getState().getID().toString());
//                contryDTO.setContryname(d.getState().getCountry().getCountryname());
//                contryDTO.setContryuuid(d.getState().getCountry().getID().toString());
//                return contryDTO;
//            }).collect(Collectors.toList());
//            return rest;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
