/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Enum.WorkFlowAction;
import org.dspace.app.rest.Enum.WorkFlowStatus;
import org.dspace.app.rest.Enum.WorkFlowUserType;
import org.dspace.app.rest.converter.*;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.repository.AbstractDSpaceRestRepository;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.repository.LinkRestRepository;
import org.dspace.app.rest.utils.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.disseminate.service.CitationDocumentService;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.services.ConfigurationService;
import org.dspace.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.dspace.app.rest.utils.RegexUtils.REGEX_REQUESTMAPPING_IDENTIFIER_AS_UUID;

/**
 * This is a specialized controller to provide access to the bitstream binary
 * content
 * <p>
 * The mapping for requested endpoint try to resolve a valid UUID, for example
 * <pre>
 * {@code
 * https://<dspace.server.url>/api/core/bitstreams/26453b4d-e513-44e8-8d5b-395f62972eff/content
 * }
 * </pre>
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 * @author Tom Desair (tom dot desair at atmire dot com)
 * @author Frederic Van Reet (frederic dot vanreet at atmire dot com)
 */
@RestController
@RequestMapping("/api/" + WorkFlowProcessRest.CATEGORY + REGEX_REQUESTMAPPING_IDENTIFIER_AS_UUID)
public class WorkflowProcessActionController extends AbstractDSpaceRestRepository implements LinkRestRepository {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessActionController.class);
    private static final int BUFFER_SIZE = 4096 * 10;
    @Autowired
    WorkflowProcessService workflowProcessService;

    @Autowired
    ItemConverter itemConverter;
    @Autowired
    WorkFlowProcessConverter workFlowProcessConverter;

    @Autowired
    WorkFlowProcessDraftDetailsConverter workFlowProcessDraftDetailsConverter;

    @Autowired
    WorkflowProcessReferenceDocVersionService workflowProcessReferenceDocVersionService;

    @Autowired
    WorkflowProcessNoteService workflowProcessNoteService;
    @Autowired
    BundleRestRepository bundleRestRepository;

    @Autowired
    CitationDocumentService citationDocumentService;

    @Autowired
    private FeedbackService feedbackService;


    @Autowired
    ConfigurationService configurationService;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    WorkflowProcessSenderDiaryConverter workflowProcessSenderDiaryConverter;
    @Autowired
    WorkflowProcessSenderDiaryService processSenderDiaryService;

    @Autowired
    WorkFlowProcessCommentService workFlowProcessCommentService;

    @Autowired
    WorkFlowProcessCommentConverter workFlowProcessCommentConverter;
    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;
    @Autowired
    WorkflowProcessReferenceDocService workflowProcessReferenceDocService;

    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Autowired
    WorkFlowProcessEpersonConverter workFlowProcessEpersonConverter;
    @Autowired
    GroupConverter groupConverter;
    @Autowired
    WorkFlowProcessHistoryService workFlowProcessHistoryService;

    @Autowired
    WorkflowProcessEpersonService workflowProcessEpersonService;

    @Autowired
    WorkFlowProcessDraftDetailsService workFlowProcessDraftDetailsService;
    @Autowired
    private BundleService bundleService;
    @Autowired
    JbpmServerImpl jbpmServer;
    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    EPersonConverter ePersonConverter;

    @Autowired
    EventService eventService;
    @Autowired
    MetadataFieldService metadataFieldService;
    @Autowired
    MetadataValueService metadataValueService;

    @Autowired
    WorkFlowProcessOutwardDetailsConverter outwardDetailsConverter;

    private static Font COURIER = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
    private static Font COURIER_SMALL = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static Font COURIER_SMALL_FOOTER = new Font(Font.FontFamily.UNDEFINED, 10, Font.NORMAL);
    private static Font COURIER_SMALL_FOOTER1 = new Font(Font.FontFamily.COURIER, 10, Font.BOLD);

    private Boolean isslip = false;

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "forwordDraft")
    public WorkFlowProcessRest forwordDraft(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        WorkFlowProcessRest workFlowProcessRest = null;
        log.info("in Forward Action start");
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            ObjectMapper mapper = new ObjectMapper();
            workFlowProcessRest = mapper.readValue(request.getInputStream(), WorkFlowProcessRest.class);
            WorkFlowProcessRest workFlowProcessRest1 = workFlowProcessRest;
            String comment = workFlowProcessRest.getComment();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            Optional<WorkflowProcessEperson> e=workFlowProcess.getWorkflowProcessEpeople().stream().filter(d->d.getePerson().getID().equals(context.getCurrentUser().getID())).findFirst();
            if(e.isPresent()){
                System.out.println("remark update!");
                WorkflowProcessEperson ee=e.get();
                ee.setRemark(workFlowProcessRest.getRemark());
                workflowProcessEpersonService.update(context,ee);
            }
            WorkflowProcess workFlowProcessfinal=workFlowProcess;
            if(workFlowProcess!=null&&workFlowProcessRest1.getWorkflowProcessSenderDiaryRests()!=null &&workFlowProcessRest1.getWorkflowProcessSenderDiaryRests().size()!=0){
                System.out.println("in sender ::::::::::::dirys");
                for (WorkflowProcessSenderDiaryRest rest:workFlowProcessRest1.getWorkflowProcessSenderDiaryRests()) {
                    WorkflowProcessSenderDiary workflowProcessSenderDiary =workflowProcessSenderDiaryConverter.convert(context,rest);
                    workflowProcessSenderDiary.setWorkflowProcess(workFlowProcessfinal);
                    processSenderDiaryService.create(context,workflowProcessSenderDiary);
                }
            }
//            if (workFlowProcessRest != null && workFlowProcessRest.getItemRest() != null) {
//                workFlowProcess.setItem(itemConverter.convert(workFlowProcessRest.getItemRest(), context));
//            }
            //new draft add in note
//            if(workFlowProcess!=null&&workFlowProcessRest1.getWorkFlowProcessDraftDetailsRest()!=null){
//                System.out.println("in drft save");
//               workFlowProcess.setWorkFlowProcessDraftDetails(workFlowProcessDraftDetailsConverter.convert(context,workFlowProcessRest1.getWorkFlowProcessDraftDetailsRest()));
//                if(workFlowProcessRest1.getIsreplydraft()!=null){
//                    System.out.println("in is reply draft");
//                    workFlowProcess.setIsreplydraft(workFlowProcessRest1.getIsreplydraft());
//                }
//               workflowProcessService.update(context,workFlowProcess);
//            }
            List<String> olduser = null;
            List<WorkflowProcessEperson> olduserlistuuid = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> !d.getIssequence()).collect(Collectors.toList());
            List<WorkflowProcessEperson> olduserlistuuidissequenstrue = workFlowProcess.getWorkflowProcessEpeople().stream().collect(Collectors.toList());
            if (olduserlistuuid != null && olduserlistuuid.size() != 0) {
                olduser = olduserlistuuid.stream().filter(d -> d.getePerson() != null).filter(d -> d.getePerson().getID() != null).filter(d -> !d.getIssequence()).map(d -> d.getePerson().getID().toString()).collect(Collectors.toList());
            }
            if (workFlowProcessRest.getWorkflowProcessEpersonRests() != null) {
                List<WorkflowProcessEpersonRest> WorkflowProcessEpersonRestList = workFlowProcessRest.getWorkflowProcessEpersonRests().stream().filter(d -> !d.getIssequence()).collect(Collectors.toList());
                for (WorkflowProcessEpersonRest newEpesonrest : WorkflowProcessEpersonRestList) {
                    WorkflowProcessEperson workflowProcessEperson = workFlowProcessEpersonConverter.convert(context, newEpesonrest);
                    workflowProcessEperson.setWorkflowProcess(workFlowProcess);
                    Optional<WorkFlowProcessMasterValue> userTypeOption = WorkFlowUserType.NORMAL.getUserTypeFromMasterValue(context);
                    if (userTypeOption.isPresent()) {
                        workflowProcessEperson.setUsertype(userTypeOption.get());
                    }
                    if (newEpesonrest.getePersonRest() != null && newEpesonrest.getePersonRest().getId() != null && olduser != null && olduser.contains(newEpesonrest.getePersonRest().getId())) {
                        System.out.println(":::::::::ALLREADY USE EPERSON IN SYSTEM");
                    } else {
                        System.out.println("ADD NEW USER IN WORKFLOWEPERSON LIST");
                        System.out.println("New user index  : " + workflowProcessEperson.getIndex());
                        workFlowProcess.setnewUser(workflowProcessEperson);
                        workflowProcessService.create(context, workFlowProcess);
                    }
                }
            }
            WorkFlowAction action = WorkFlowAction.FORWARD;
            //user not select any next user then flow go initiator
            if (olduser == null && workFlowProcessRest.getWorkflowProcessEpersonRests().size() == 0) {
                System.out.println("::::::::::::::::::::::::::::setInitiator :::::::true::::::::::::::::::::");
                Optional<WorkflowProcessEperson> workflowPro = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getUsertype().getPrimaryvalue().equalsIgnoreCase(WorkFlowUserType.INITIATOR.getAction())).findFirst();
                if (workflowPro.isPresent()) {
                    action.setInitiator(true);
                } else {
                    action.setInitiator(false);
                }
            }
            //one flow completed after next time forward initiator to next user
            if (workFlowProcess.getWorkflowProcessEpeople() != null) {
                Optional<WorkflowProcessEperson> workflowPro = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getUsertype().getPrimaryvalue().equalsIgnoreCase(WorkFlowUserType.INITIATOR.getAction())).findFirst();
                if (workflowPro.isPresent() && workflowPro.get().getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())) {
                    System.out.println("::::::::::::::::::::::::::::setInitiatorForward::::::::true::::::::::::::::::::");
                    action.setInitiatorForward(true);
                } else {
                    action.setInitiatorForward(false);
                }
            }
            if (comment != null) {
                action.setComment(comment);
                if (workFlowProcessRest.getWorkflowProcessReferenceDocRests() != null && workFlowProcessRest.getWorkflowProcessReferenceDocRests().size() != 0) {
                    List<WorkflowProcessReferenceDoc> doc = getCommentDocuments(context, workFlowProcessRest);
                    if (doc != null) {
                        action.setWorkflowProcessReferenceDocs(doc);
                    }
                }
            }
            WorkflowProcess workflowProcess1 = workFlowProcess;
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            if(workFlowProcessRest1.getRemark()!=null) {
                workFlowProcessRest.setRemark(workFlowProcessRest1.getRemark());
            }else {
                System.out.println("getRemark not found");
            }
            action.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            context.commit();
            action.setComment(null);
            action.setWorkflowProcessReferenceDocs(null);
            action.setInitiator(false);
            if (workFlowProcessRest1 != null && workFlowProcessRest1.getWorkFlowProcessCommentRest() != null) {
                Context context12 = ContextUtil.obtainContext(request);
                //saveComment(context12, workflowProcess1, workFlowProcessRest1, request);
            }
            log.info("in Forward Action stop");
            return workFlowProcessRest;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in forwardTask Server..");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "backward")
    public WorkFlowProcessRest backward(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        log.info("in Backward Action start");
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcessEpersonRest workflowProcessEpersonRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            ObjectMapper mapper = new ObjectMapper();
            workflowProcessEpersonRest = mapper.readValue(request.getInputStream(), WorkflowProcessEpersonRest.class);
            String comment = workflowProcessEpersonRest.getComment();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess.getItem() == null && workflowProcessEpersonRest.getItemRest() != null) {
              //  workFlowProcess.setItem(itemConverter.convert(workflowProcessEpersonRest.getItemRest(), context));
            }
            int index = workFlowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson() != null).filter(d -> d.getePerson().getID() != null).filter(d -> d.getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())).findFirst().get().getIndex();
            WorkflowProcessEperson workflowProcessEperson = workFlowProcess.getWorkflowProcessEpeople().get(index - 1);
            if (workflowProcessEperson != null && workflowProcessEperson.getIsrefer()) {
                System.out.println("::::::::::::::::::::::::::::REFER USER ::::::::::::::::::::::::::::::::");
                Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.REFER.getUserTypeFromMasterValue(context);
                if (workFlowTypeStatus.isPresent()) {
                    workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
                }
            } else {
                System.out.println("::::::::::::::::::::::::::::NORMAL USER ::::::::::::::::::::::::::::::::");
                Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
                if (workFlowTypeStatus.isPresent()) {
                    workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
                }
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction backward = WorkFlowAction.BACKWARD;
            if (comment != null) {
                backward.setComment(comment);
                if (workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests() != null && workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests().size() != 0) {
                    List<WorkflowProcessReferenceDoc> doc = getCommentDocumentsByEpersion(context, workflowProcessEpersonRest);
                    if (doc != null) {
                        backward.setWorkflowProcessReferenceDocs(doc);
                    }
                }
            }
            backward.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            context.commit();
            backward.setComment(null);
            backward.setWorkflowProcessReferenceDocs(null);
            log.info("in Backward Action stop!");
            return workFlowProcessRest;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in backwar Server..");
        }
    }


     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
   
    @RequestMapping(method = {RequestMethod.DELETE, RequestMethod.HEAD}, value = "deleteitem")
    public void deleteItem(@PathVariable UUID uuid, HttpServletRequest request) throws Exception {
        log.info("in deleteItem Action start");
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess.getItem() != null) {
                workFlowProcess.setItem(null);
                workflowProcessService.update(context, workFlowProcess);
                context.commit();
                System.out.println("Item Deleted!");
            } else {
                System.out.println("Item All ready Deleted");
            }
            log.info("in deleteItem Action stop!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
   
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "digitalsign")
    public WorkflowProcessReferenceDocRest digitalsign(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        log.info("in digitalsign Action start!");
        WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, uuid);
            if (workflowProcessReferenceDoc != null) {
                workflowProcessReferenceDoc.setIssignature(true);
            }
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc1 = workflowProcessReferenceDocService.create(context, workflowProcessReferenceDoc);
            workflowProcessReferenceDocRest = workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc1, utils.obtainProjection());
            if (workflowProcessReferenceDoc1.getIssignature()) {
                WorkFlowProcessHistory workFlowAction = new WorkFlowProcessHistory();
                WorkflowProcessEperson eperson = new WorkflowProcessEperson();
                WorkflowProcess workflowProcess = workflowProcessReferenceDoc1.getWorkflowProcess();
                eperson.setOwner(true);
                eperson.setePerson(context.getCurrentUser());
                eperson.setWorkflowProcess(workflowProcessReferenceDoc1.getWorkflowProcess());
                eperson.setIndex(workflowProcess.getWorkflowProcessEpeople().stream().map(d -> d.getSequence()).max(Integer::compareTo).get());
                workflowProcess.setnewUser(eperson);
                workflowProcessService.create(context, workflowProcess);
                WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.UPDATE.getAction(), workFlowProcessMaster);
                workFlowAction.setAction(workFlowProcessMasterValue);
                workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
            }
            return workflowProcessReferenceDocRest;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in callback Server..");
        }
    }

     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
   
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "callback")
    public WorkFlowProcessRest callback(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        log.info("in callback Action start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction CALLBACK = WorkFlowAction.CALLBACK;
            CALLBACK.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            context.commit();
            CALLBACK.setComment(null);
            CALLBACK.setWorkflowProcessReferenceDocs(null);
            log.info("in callback Action stop!");
            return workFlowProcessRest;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in callback Server..");
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "parked")
    public WorkFlowProcessRest parked(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            ObjectMapper mapper = new ObjectMapper();
            WorkFlowProcessRest workFlowProcessRest1 = mapper.readValue(request.getInputStream(), WorkFlowProcessRest.class);
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);

            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.PARKED.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent() && workFlowProcess != null) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            if (workFlowProcessRest1.getComment() != null) {
                workFlowProcess.setRemark(workFlowProcessRest1.getComment());
            }
            //need to set timing parked time if predifine
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction parked = WorkFlowAction.PARKED;
            parked.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            parked.setComment(null);
            parked.setWorkflowProcessReferenceDocs(null);
            return workFlowProcessRest;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in callback Server..");
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "parkedreopen")
    public WorkFlowProcessRest parkedreopen(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent() && workFlowProcess != null) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            //need to set timing parked time if predifine
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction reopenAction = WorkFlowAction.REOPEN;
            reopenAction.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            reopenAction.setComment(null);
            reopenAction.setWorkflowProcessReferenceDocs(null);
            return workFlowProcessRest;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnprocessableEntityException("error in callback Server..");
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "reject")
    public WorkFlowProcessRest reject(@PathVariable UUID uuid, HttpServletRequest request) throws Exception {
        log.info("in reject Action start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcessEpersonRest workflowProcessEpersonRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            ObjectMapper mapper = new ObjectMapper();
            workflowProcessEpersonRest = mapper.readValue(request.getInputStream(), WorkflowProcessEpersonRest.class);
            String comment = workflowProcessEpersonRest.getComment();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess.getItem() == null && workflowProcessEpersonRest.getItemRest() != null) {
               //workFlowProcess.setItem(itemConverter.convert(workflowProcessEpersonRest.getItemRest(), context));
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.REJECTED.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction reject = WorkFlowAction.REJECTED;
            if (comment != null) {
                reject.setComment(comment);
                if (workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests() != null && workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests().size() != 0) {
                    List<WorkflowProcessReferenceDoc> doc = getCommentDocumentsByEpersion(context, workflowProcessEpersonRest);
                    if (doc != null) {
                        reject.setWorkflowProcessReferenceDocs(doc);
                    }
                }
            }
            reject.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            reject.setComment(null);
            reject.setWorkflowProcessReferenceDocs(null);
            log.info("in reject Action stop!");
            return workFlowProcessRest;
        } catch (RuntimeException e) {
            e.printStackTrace();
         //   log.error("Error in in reject Action" + e.getMessage());
        }
        return null;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "refer")
    public WorkFlowProcessRest refer(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        log.info("in refer Action start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcessEpersonRest workflowProcessEpersonRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            ObjectMapper mapper = new ObjectMapper();
            workflowProcessEpersonRest = mapper.readValue(request.getInputStream(), WorkflowProcessEpersonRest.class);
            String comment = workflowProcessEpersonRest.getComment();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            WorkflowProcessEperson workflowProcessEperson = workFlowProcessEpersonConverter.convert(context, workflowProcessEpersonRest);
            workflowProcessEperson.setWorkflowProcess(workFlowProcess);
            workflowProcessEperson.setOwner(true);
            Optional<WorkFlowProcessMasterValue> userTypeOption = WorkFlowUserType.REFER.getUserTypeFromMasterValue(context);
            if (userTypeOption.isPresent()) {
                workflowProcessEperson.setUsertype(userTypeOption.get());
            }
            if (workFlowProcess.getItem() == null && workflowProcessEpersonRest.getItemRest() != null) {
               // workFlowProcess.setItem(itemConverter.convert(workflowProcessEpersonRest.getItemRest(), context));
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.REFER.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcess.setnewUser(workflowProcessEperson);
            workflowProcessService.create(context, workFlowProcess);
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction refer = WorkFlowAction.REFER;
            refer.setIsrefer(true);
            if (comment != null) {
                refer.setComment(comment);
                if (workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests() != null && workflowProcessEpersonRest.getWorkflowProcessReferenceDocRests().size() != 0) {
                    List<WorkflowProcessReferenceDoc> doc = getCommentDocumentsByEpersion(context, workflowProcessEpersonRest);
                    if (doc != null) {
                        refer.setWorkflowProcessReferenceDocs(doc);
                    }
                }
            }
            refer.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            context.commit();
            refer.setComment(null);
            refer.setWorkflowProcessReferenceDocs(null);
            refer.setIsrefer(false);
            log.info("in refer Action stop!");
            return workFlowProcessRest;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Error refer Action is" + e.getMessage());
            throw new UnprocessableEntityException("error in forwardTask Server..");
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "received")
    public WorkFlowProcessRest received(@PathVariable UUID uuid, HttpServletRequest request) throws IOException, SQLException, AuthorizeException {
        log.info("in received Action start!");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            workFlowProcess.setIsmode(true);
            workflowProcessService.update(context, workFlowProcess);
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction received = WorkFlowAction.RECEIVED;
            received.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            context.commit();
            received.setComment(null);
            received.setWorkflowProcessReferenceDocs(null);
            received.setIsrefer(false);
            log.info("in received Action stop!");
            return workFlowProcessRest;
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            log.error("in received Action Error" + e.getMessage());
            throw new UnprocessableEntityException("error in forwardTask Server..");
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "dispatchreplydraftCRU")
    public WorkFlowProcessRest dispatchreplydraftCRU(@PathVariable UUID uuid, HttpServletRequest request, @RequestBody WorkFlowProcessRest workFlowProcessRests) throws IOException, SQLException, AuthorizeException {
        System.out.println("in dispatchreplydraftCRU Action dispatchreplydraftCRU");
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = null;
        Set<WorkflowProcessReferenceDocVersion> workflowProcessReferenceDocVersions = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);

            Optional<WorkflowProcessReferenceDoc> doc = workFlowProcess.getWorkflowProcessReferenceDocs().stream().filter(d -> d.getDrafttype() != null).filter(d -> d.getDrafttype().getPrimaryvalue() != null).filter(d -> d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reply Tapal")).findFirst();
            if (doc.isPresent()) {
                workflowProcessReferenceDoc = doc.get();
                workflowProcessReferenceDocVersions = workflowProcessReferenceDoc.getWorkflowProcessReferenceDocVersion();
            }
//            if (workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest() != null && workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getOutwardDepartmentRest() != null) {
//                Group g = groupConverter.convert(context, workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getOutwardDepartmentRest());
//                for (EPerson e : g.getMembers()) {
//                    try {
//                        if (workflowProcessReferenceDoc != null) {
//                            WorkflowProcessReferenceDocVersion version = new WorkflowProcessReferenceDocVersion();
//                            version.setCreator(e);
//                            Double versionnumber = (double) doc.get().getWorkflowProcessReferenceDocVersion().size() + 1;
//                            version.setVersionnumber(versionnumber);
//                            version.setIssign(true);
//                            version.setWorkflowProcessReferenceDoc(workflowProcessReferenceDoc);
//                            workflowProcessReferenceDocVersions.add(version);
//                        }
//                    } catch (Exception ee) {
//                        throw new RuntimeException(ee.getMessage());
//                    }
//                }
//                workflowProcessReferenceDoc.setWorkflowProcessReferenceDocVersion(workflowProcessReferenceDocVersions);
//                workflowProcessReferenceDocService.update(context, workflowProcessReferenceDoc);
//            }
          //  WorkFlowProcessDraftDetails draftDetails = workFlowProcess.getWorkFlowProcessDraftDetails();
//            if (draftDetails != null) {
//                if (draftDetails != null) {
//                    draftDetails.setIsdispatchbycru(true);
//                    workFlowProcessDraftDetailsService.update(context, draftDetails);
//                }
//            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return workFlowProcessRest;
    }




     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "dispatchbySelf")
    public WorkFlowProcessRest dispatchbySelf(@PathVariable String uuid, HttpServletRequest request, @RequestBody WorkFlowProcessRest workFlowProcessRests) throws IOException, SQLException, AuthorizeException {
        WorkFlowProcessRest workFlowProcessRest = null;
        String mrgeddoc = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            String comment = "";
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, UUID.fromString(uuid));
            if (workFlowProcessRests.getWorkflowProcessSenderDiaryRests() != null) {
                List<WorkflowProcessSenderDiary> workflowProcessSenderDiaries = workFlowProcess.getWorkflowProcessSenderDiaries();
                if (workflowProcessSenderDiaries != null) {
                    for (WorkflowProcessSenderDiaryRest workflowProcessSenderDiaryrest : workFlowProcessRests.getWorkflowProcessSenderDiaryRests()) {
                        WorkflowProcessSenderDiary workflowProcessSenderDiary = workflowProcessSenderDiaryConverter.convert(context, workflowProcessSenderDiaryrest);
                        workflowProcessSenderDiary.setWorkflowProcess(workFlowProcess);
                        workflowProcessSenderDiaries.add(workflowProcessSenderDiary);
                    }
                    workFlowProcess.setWorkflowProcessSenderDiaries(workflowProcessSenderDiaries);
                }
            }
            if (workFlowProcessRests.getDispatchModeRest() != null) {
                WorkFlowProcessMasterValue dispatchMode = workFlowProcessMasterValueConverter.convert(context, workFlowProcessRests.getDispatchModeRest());
                if (dispatchMode != null && dispatchMode.getPrimaryvalue() != null && dispatchMode.getPrimaryvalue().equalsIgnoreCase("Electronic")) {
                    try {
                        System.out.println("sent email ");
                        //sent email
                       // comment = sentMailElectronic(context, request, workFlowProcess, workFlowProcessRests);
                        //change
                 //       WorkFlowProcessDraftDetails draftDetails = workFlowProcess.getWorkFlowProcessDraftDetails();
//                        if (draftDetails != null) {
//                            draftDetails.setIsdispatchbyself(true);
//                            workFlowProcessDraftDetailsService.update(context, draftDetails);
//                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                } else {
                    System.out.println("in phisical............!");
//                    WorkFlowProcessDraftDetails draftDetails = workFlowProcess.getWorkFlowProcessDraftDetails();
//                    if (draftDetails != null) {
//                        draftDetails.setIsdispatchbyself(true);
//                        workFlowProcessDraftDetailsService.update(context, draftDetails);
//                    }
                    if (workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest() != null) {
                        WorkFlowProcessOutwardDetails workFlowProcessOutwardDetails = new WorkFlowProcessOutwardDetails();
                        if (workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getAwbno() != null) {
                            workFlowProcessOutwardDetails.setAwbno(workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getAwbno());
                        }
                        if (workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getServiceprovider() != null) {
                            workFlowProcessOutwardDetails.setServiceprovider(workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getServiceprovider());
                        }
                        if (workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getDispatchdate() != null) {
                            workFlowProcessOutwardDetails.setDispatchdate(workFlowProcessRests.getWorkFlowProcessOutwardDetailsRest().getDispatchdate());
                        }
                        workFlowProcess.setWorkFlowProcessOutwardDetails(workFlowProcessOutwardDetails);
                    }
                }
            }
            if (workFlowProcess.getWorkflowType().getPrimaryvalue().equals("Draft")) {
                System.out.println("in draft by self");
                Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
                if (workFlowTypeStatus.isPresent()) {
                    workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
                }
            }else {
                Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.DISPATCHCLOSE.getUserTypeFromMasterValue(context);
                if (workFlowTypeStatus.isPresent()) {
                    workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
                }
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            workflowProcessService.create(context, workFlowProcess);
            storeWorkFlowHistory(context, workFlowProcess);
            context.commit();
            log.info("in dispatch Stop !");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("in dispatch Action Error" + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        workFlowProcessRest.setMargeddocuuid(mrgeddoc);
        return workFlowProcessRest;
    }









     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "dispatchColose")
    public WorkFlowProcessRest dispatchColose(@PathVariable String uuid, HttpServletRequest request) throws
            IOException, SQLException, AuthorizeException {
        log.info("in dispatchColose Start !");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            String comment = null;
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, UUID.fromString(uuid));
            //comment = sentMailElectronic(context, request, workFlowProcess, workFlowProcessRest);
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction COMPLETE = WorkFlowAction.COMPLETE;
            if (comment != null) {
                COMPLETE.setComment(comment);
            }
            COMPLETE.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            COMPLETE.setComment(null);
            log.info("in dispatchColose Stop !");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return workFlowProcessRest;
    }

     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "suspend")
    public WorkFlowProcessRest suspend(@PathVariable UUID uuid, HttpServletRequest request) throws
            IOException, SQLException {
        log.info("in suspend Start !");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
//            if (workFlowProcess.getWorkflowType().getPrimaryvalue().equals("Draft")) {
//                createFinalNote(context, workFlowProcess);
//            }
            if (workFlowProcess == null) {
                throw new RuntimeException("Workflow not found");
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.HOLD.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction holdAction = WorkFlowAction.HOLD;

            holdAction.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            holdAction.setComment(null);
            log.info("in suspend Stop !");
            return workFlowProcessRest;
        } catch (RuntimeException e) {
            throw new UnprocessableEntityException("error in suspendTask Server..");
        } catch (AuthorizeException e) {
            throw new RuntimeException(e);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "resumetask")
    public WorkFlowProcessRest resumetask(@PathVariable UUID uuid, HttpServletRequest request) throws
            IOException, SQLException, AuthorizeException {
        log.info("in resumetask Start !");
        WorkFlowProcessRest workFlowProcessRest = null;
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();
            WorkflowProcess workFlowProcess = workflowProcessService.find(context, uuid);
            if (workFlowProcess == null) {
                throw new RuntimeException("Workflow not found");
            }
            Optional<WorkFlowProcessMasterValue> workFlowTypeStatus = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context);
            if (workFlowTypeStatus.isPresent()) {
                workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            }
            workFlowProcessRest = workFlowProcessConverter.convert(workFlowProcess, utils.obtainProjection());
            WorkFlowAction unholdAction = WorkFlowAction.UNHOLD;
            unholdAction.perfomeAction(context, workFlowProcess, workFlowProcessRest);
            workFlowProcess.setWorkflowStatus(workFlowTypeStatus.get());
            workflowProcessService.create(context, workFlowProcess);
            context.commit();
            unholdAction.setComment(null);
            log.info("in resumetask Stop !");
            return workFlowProcessRest;
        } catch (RuntimeException e) {
            log.error("in resumetask Error !" + e.getMessage());
            throw new UnprocessableEntityException("error in forwardTask Server..");
        }
    }

    ///createFinalDraftDoc


    public WorkflowProcessReferenceDoc margedPDF(Context context, WorkflowProcess workflowProcess) {
        try {
            WorkflowProcessReferenceDoc margedoc = new WorkflowProcessReferenceDoc();
            margedoc.setSubject(workflowProcess.getSubject());
            // margedoc.setDescription(workflowProcess.getSubject() + " for " + FileUtils.getNameWithoutExtension(tempFile1html.getName()));
            margedoc.setInitdate(new Date());
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Draft Type");
            if (workFlowProcessMaster != null) {
                WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, "Note", workFlowProcessMaster);
                if (workFlowProcessMasterValue != null) {
                    margedoc.setDrafttype(workFlowProcessMasterValue);
                }
            }
            margedoc.setWorkflowProcess(workflowProcess);
            //mrged doc
            // FileInputStream outputfile = new FileInputStream(new File(tempFile1html.getAbsolutePath()));
            // Bitstream bitstream = bundleRestRepository.processBitstreamCreationWithoutBundle(context, outputfile, "", tempFile1html.getName());
            // margedoc.setBitstream(bitstream);
            WorkflowProcessReferenceDoc margedoc1 = workflowProcessReferenceDocService.create(context, margedoc);
            WorkflowProcessReferenceDoc workflowProcessReferenceDoc = margedoc1;
            WorkflowProcessNote workflowProcessNote = new WorkflowProcessNote();
            Optional<EPerson> creator = workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson() != null).filter(d -> d.getIndex() == 0).map(d -> d.getePerson()).findFirst();
            if (creator.isPresent()) {
                workflowProcessNote.setSubmitter(creator.get());
            }
            if (workflowProcess.getSubject() != null) {
                workflowProcessNote.setSubject(workflowProcess.getSubject());
            }
            List<WorkflowProcessReferenceDoc> doc = new ArrayList<>();
            doc.add(workflowProcessReferenceDoc);
            WorkflowProcessNote finalw = workflowProcessNoteService.create(context, workflowProcessNote);
            margedoc.setWorkflowprocessnote(finalw);
            workflowProcessReferenceDocService.update(context, workflowProcessReferenceDoc);
            return workflowProcessReferenceDoc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<WorkflowProcessReferenceDoc> getCommentDocumentsByEpersion(Context
                                                                                   context, WorkflowProcessEpersonRest erest) {
        List<WorkflowProcessReferenceDoc> docs = null;
        if (erest.getWorkflowProcessReferenceDocRests() != null) {
            docs = erest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                try {
                    return workflowProcessReferenceDocConverter.convertByService(context, d);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        }
        return docs;
    }

    public List<WorkflowProcessReferenceDoc> getCommentDocuments(Context context, WorkFlowProcessRest wrest) {
        List<WorkflowProcessReferenceDoc> docs = null;
        if (wrest.getWorkflowProcessReferenceDocRests() != null) {
            if (wrest.getWorkflowProcessReferenceDocRests() != null) {
                docs = wrest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                    try {
                        return workflowProcessReferenceDocConverter.convertByService(context, d);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
            }
        }

        return docs;
    }

    private static String DateFormate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        return formatter.format(date);
    }

    public WorkFlowProcessMasterValue getMastervalueData(Context context, String mastername, String mastervaluename) throws
            SQLException {
        WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, mastername);
        if (workFlowProcessMaster != null) {
            WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, mastervaluename, workFlowProcessMaster);
            if (workFlowProcessMasterValue != null) {
                System.out.println(" MAster value" + workFlowProcessMasterValue.getPrimaryvalue());
                return workFlowProcessMasterValue;
            }
        }
        return null;
    }

    public void stroremetadateinmap(Bitstream bitstream, Map<String, String> map) throws ParseException {
        if (bitstream.getMetadata() != null) {
            int i = 0;
            String refnumber = null;
            String doctype = null;
            String date = null;
            String lettercategory = null;
            String lettercategoryhindi = null;
            String description = null;
            StringBuffer doctyperefnumber = new StringBuffer();
            StringBuffer datelettercategory = new StringBuffer();

            for (MetadataValue metadataValue : bitstream.getMetadata()) {
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_doc_type")) {
                    doctype = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_ref_number")) {
                    refnumber = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_date")) {
                    date = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_category")) {
                    lettercategory = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_categoryhi")) {
                    lettercategoryhindi = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_description")) {
                    description = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_title")) {
                }
                i++;
            }

            if (doctype != null) {

                doctyperefnumber.append(doctype);
            } else {
                if (bitstream.getName() != null) {

                } else {

                }
            }
            if (refnumber != null) {

                doctyperefnumber.append("(" + refnumber + ")");
            } else {

            }
            if (date != null) {
                try {
                    datelettercategory.append(DateUtils.strDateToString(date));
                } catch (Exception e) {
                   // e.getMessage();
                    e.printStackTrace();
                }
            } else {

            }
            if (lettercategory != null && lettercategoryhindi != null) {

                datelettercategory.append(" (" + lettercategory + "|" + lettercategoryhindi + ")");
            } else {

            }
            if (description != null) {

                map.put("description", description);
            } else {

            }
            map.put("datelettercategory", datelettercategory.toString() != null ? datelettercategory.toString() : "-");
            map.put("doctyperefnumber", doctyperefnumber.toString() != null ? doctyperefnumber.toString() : "-");

        }

    }



    public void storeWorkFlowHistoryForSignaturePanding(Context context, WorkflowProcessReferenceDoc doc) throws
            Exception {
        System.out.println("::::::IN :storeWorkFlowHistoryForSignaturePanding:::::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        WorkflowProcess workflowProcess = doc.getWorkflowProcess();
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.PENDING.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        // workflowProcess.getWorkflowProcessNote().getSubject();
        workFlowAction.setComment("Document Signature Pending By " + doc.getDocumentsignator().getFullName() + " | ");
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistoryForSignaturePanding:::: ");
    }

    public void storeWorkFlowHistory(Context context, WorkflowProcessReferenceDoc doc) throws Exception {
        System.out.println("::::::IN :storeWorkFlowHistoryForSignaturePanding:::::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        WorkflowProcess workflowProcess = doc.getWorkflowProcess();
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.PENDING.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        // workflowProcess.getWorkflowProcessNote().getSubject();
        workFlowAction.setComment("Document Signature Pending By " + doc.getDocumentsignator().getFullName() + " | ");
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistoryForSignaturePanding:::: ");
    }

    public void storeWorkFlowHistory(Context context, WorkflowProcess workflowProcess) throws Exception {
        System.out.println("::::::IN :storeWorkFlowHistoryForSignaturePanding:::::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        WorkflowProcessEperson current = workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get();
       if (current != null) {
           workFlowAction.setWorkflowProcessEpeople(current);
           workFlowAction.setSentto(current);
           workFlowAction.setSenttoname(current.getePerson().getFullName());
           workFlowAction.setSentbyname(current.getePerson().getFullName());
       }
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.DISPATCHCLOSE.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        // workflowProcess.getWorkflowProcessNote().getSubject();
        workFlowAction.setComment("Dispach By " + context.getCurrentUser().getFullName() + ".");
        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistoryForSignaturePanding:::: ");
    }

    public void stroremetadate(Bitstream bitstream, StringBuffer sb) throws ParseException {
        if (bitstream.getMetadata() != null) {
            int i = 0;
            String refnumber = null;
            String doctype = null;
            String date = null;
            String lettercategory = null;
            String lettercategoryhindi = null;
            String description = null;
            for (MetadataValue metadataValue : bitstream.getMetadata()) {
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_doc_type")) {
                    doctype = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_ref_number")) {
                    refnumber = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_date")) {
                    date = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_category")) {
                    lettercategory = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_letter_categoryhi")) {
                    lettercategoryhindi = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_description")) {
                    description = metadataValue.getValue();
                }
                if (metadataValue.getMetadataField() != null && metadataValue.getMetadataField().toString().equalsIgnoreCase("dc_title")) {
                }
                i++;
            }

            if (doctype != null) {
                sb.append(doctype + "</a>");
            } else {
                if (bitstream.getName() != null) {
                    sb.append(FileUtils.getNameWithoutExtension(bitstream.getName()) + "</a>");

                } else {
                    sb.append("-</a>");
                }
            }
            if (refnumber != null) {
                sb.append(" (" + refnumber + ")");
            } else {
                sb.append("-");
            }
            if (date != null) {
                try {
                    sb.append("<br>" + DateUtils.strDateToString(date));

                } catch (Exception e) {

                }
            } else {
                sb.append("<br>-");
            }
            if (lettercategory != null && lettercategoryhindi != null) {
                sb.append(" (" + lettercategory + "|" + lettercategoryhindi + ")");
            } else {
                sb.append("(-)");
            }
            if (description != null) {
                sb.append("<br>" + description);
            } else {
                sb.append("<br> -");
            }
            sb.append("</span><br><br>");
        }

    }
    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

}
