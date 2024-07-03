/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Enum.WorkFlowAction;
import org.dspace.app.rest.Enum.WorkFlowStatus;
import org.dspace.app.rest.Enum.WorkFlowType;
import org.dspace.app.rest.Enum.WorkFlowUserType;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.WorkFlowProcessConverter;
import org.dspace.app.rest.converter.WorkFlowProcessMasterValueConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;

import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.EPersonRest;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.model.WorkflowProcessDTO;
import org.dspace.app.rest.model.WorkflowProcessEpersonRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * This is the repository responsible to manage Item Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */


@Component(WorkFlowProcessRest.CATEGORY + "." + WorkFlowProcessRest.NAME)
public class WorkflowProcessRestRepository extends DSpaceObjectRestRepository<WorkflowProcess, WorkFlowProcessRest> {

    private static final Logger log = LogManager.getLogger(WorkflowProcessRestRepository.class);
    @Autowired
    WorkFlowProcessConverter workFlowProcessConverter;
    @Autowired
    GroupService groupService;
    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;
    @Autowired
    private WorkflowProcessService workflowProcessService;
    @Autowired
    private WorkflowProcessReferenceDocService workflowProcessReferenceDocService;
    @Autowired
    private WorkflowProcessSenderDiaryService workflowProcessSenderDiaryService;
    @Autowired
    private WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    JbpmServerImpl jbpmServer;

    @Autowired
    private ValidatorFactory validatorFactory;

    public WorkflowProcessRestRepository(WorkflowProcessService dsoService) {
        super(dsoService);
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public WorkFlowProcessRest findOne(Context context, UUID id) throws SQLException {
       context.turnOffAuthorisationSystem();
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcess workflowProcess = workflowProcessService.find(context, id);
        workFlowProcessRest = workFlowProcessConverter.convert(workflowProcess, utils.obtainProjection());
        try {
            if (!workflowProcess.getIsread()) {
                workflowProcess.setIsread(true);
                workflowProcessService.update(context, workflowProcess);
                context.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workFlowProcessRest;
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public Page<WorkFlowProcessRest> findAll(Context context, Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        context.turnOffAuthorisationSystem();
        try {
            UUID statusid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Workflow Type");
            UUID statusdraftid = null;
            if (workFlowProcessMaster != null) {
                statusdraftid = workFlowProcessMasterValueService.findByName(context, "Draft", workFlowProcessMaster).getID();
            }
            int count = workflowProcessService.countfindNotCompletedByUser(context, context.getCurrentUser().getID(), statusid, statusdraftid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.findNotCompletedByUser(context, context.getCurrentUser().getID(), statusid, statusdraftid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());

            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<WorkFlowProcessRest> getDomainClass() {
        return null;
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected WorkFlowProcessRest createAndReturn(Context context) throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        context.turnOffAuthorisationSystem();
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcess workflowProcess = null;
        try {
            workFlowProcessRest = mapper.readValue(req.getInputStream(), WorkFlowProcessRest.class);
            Set<ConstraintViolation<WorkFlowProcessRest>> violations = validatorFactory.getValidator().validate(workFlowProcessRest);
            if (!violations.isEmpty()) {
                //throw new WorkFlowValiDationException(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList()));
            }
            boolean isDraft = workFlowProcessRest.getDraft();
            if (isDraft) {
                workFlowProcessRest.getWorkflowProcessEpersonRests().clear();
                //clear user if workflowis Draft
            }
            //set submitorUser
            if (context.getCurrentUser() != null && !isDraft) {
                WorkflowProcessEpersonRest workflowProcessEpersonSubmitor = new WorkflowProcessEpersonRest();
                EPersonRest ePersonRest = new EPersonRest();
                ePersonRest.setUuid(context.getCurrentUser().getID().toString());
                workflowProcessEpersonSubmitor.setIndex(0);
                Optional<WorkFlowProcessMasterValue> workFlowUserTypOptional = WorkFlowUserType.INITIATOR.getUserTypeFromMasterValue(context);
                if (workFlowUserTypOptional.isPresent()) {
                    workflowProcessEpersonSubmitor.setUserType(workFlowProcessMasterValueConverter.convert(workFlowUserTypOptional.get(), utils.obtainProjection()));
                }
                workflowProcessEpersonSubmitor.setePersonRest(ePersonRest);
                workFlowProcessRest.getWorkflowProcessEpersonRests().add(workflowProcessEpersonSubmitor);
            }
            workflowProcess = createworkflowProcessFromRestObject(context, workFlowProcessRest);
            workFlowProcessRest = workFlowProcessConverter.convert(workflowProcess, utils.obtainProjection());
            try {
                if (!isDraft) {
                    WorkFlowAction create = WorkFlowAction.CREATE;
                    create.perfomeAction(context, workflowProcess, workFlowProcessRest);
                }
                context.commit();
            } catch (RuntimeException | SQLException e) {
                e.printStackTrace();
                throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
            }
        } catch (RuntimeException | IOException e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return workFlowProcessRest;
    }

    private WorkflowProcess createworkflowProcessFromRestObject(Context context, WorkFlowProcessRest workFlowProcessRest) throws AuthorizeException {
        WorkflowProcess workflowProcess = null;
        try {

            workflowProcess = workFlowProcessConverter.convert(workFlowProcessRest, context);
            Optional<WorkflowProcessSenderDiary> workflowProcessSenderDiaryOptional = Optional.ofNullable(workflowProcessSenderDiaryService.findByEmailID(context, workflowProcess.getWorkflowProcessSenderDiary().getEmail()));
            if (workflowProcessSenderDiaryOptional.isPresent()) {
                workflowProcess.setWorkflowProcessSenderDiary(workflowProcessSenderDiaryOptional.get());
            }
            WorkFlowProcessMasterValue workflowstatusopOptionalWorkFlowProcessMasterValue = null;
            if (!workFlowProcessRest.getDraft()) {
                workflowstatusopOptionalWorkFlowProcessMasterValue = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get();
            } else {
                workflowstatusopOptionalWorkFlowProcessMasterValue = WorkFlowStatus.SUSPEND.getUserTypeFromMasterValue(context).get();
            }
            if (workflowstatusopOptionalWorkFlowProcessMasterValue != null) {

                workflowProcess.setWorkflowStatus(workflowstatusopOptionalWorkFlowProcessMasterValue);
            }
            workflowProcess = workflowProcessService.create(context, workflowProcess);
            WorkflowProcess finalWorkflowProcess = workflowProcess;
            workflowProcess.setWorkflowProcessReferenceDocs(workFlowProcessRest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
                    workflowProcessReferenceDoc.setWorkflowProcess(finalWorkflowProcess);
                    return workflowProcessReferenceDoc;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(toList()));
            workflowProcessService.update(context, workflowProcess);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return workflowProcess;
    }

     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getByWorkFlowType")
    public Page<WorkFlowProcessRest> getByWorkFlowType(@Parameter(value = "uuid", required = true) UUID typeid, Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext(); context.turnOffAuthorisationSystem();
            UUID statusid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countfindNotCompletedByUser(context, context.getCurrentUser().getID(), statusid, typeid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.findNotCompletedByUser(context, context.getCurrentUser().getID(), statusid, typeid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "findCompletedFlowByWorkflowTypeid")
    public Page<WorkFlowProcessRest> findCompletedFlowByWorkflowTypeid(@Parameter(value = "uuid", required = true) UUID typeid, Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID statusid = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countfindCompletedFlow(context, context.getCurrentUser().getID(), statusid, typeid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.findCompletedFlow(context, context.getCurrentUser().getID(), statusid, typeid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "gethistory")
    public Page<WorkflowProcessDTO> gethistory(Pageable pageable) {
        log.info("in gethistory start ");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            context.turnOffAuthorisationSystem();
            UUID statusid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countgetHistoryByNotOwnerAndNotDraft(context, context.getCurrentUser().getID(), statusid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.getHistoryByNotOwnerAndNotDraft(context, context.getCurrentUser().getID(), statusid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            log.info("in gethistory stop! ");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("in dashboard Error " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "sentTapal")
    public Page<WorkflowProcessDTO> sentTapal(Pageable pageable) {
        System.out.println("in sentTapal");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
               Context context = obtainContext();
               context.turnOffAuthorisationSystem();
            UUID statusidclose = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            UUID statusid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            UUID workflowtypeid = WorkFlowType.INWARD.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countTapal(context, context.getCurrentUser().getID(), statusid,workflowtypeid,statusidclose);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.sentTapal(context, context.getCurrentUser().getID(), statusid,workflowtypeid,statusidclose, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            System.out.println("out sentTapal");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "sentEFile")
    public Page<WorkflowProcessDTO> sentEFile(Pageable pageable) {
        System.out.println("in sentEFile");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID statusidclose = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            UUID statusid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            UUID workflowtypeid = WorkFlowType.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countTapal(context, context.getCurrentUser().getID(), statusid,workflowtypeid,statusidclose);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.sentTapal(context, context.getCurrentUser().getID(), statusid,workflowtypeid,statusidclose, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            System.out.println("out sentEFile");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "closeTapal")
    public Page<WorkflowProcessDTO> closeTapal(Pageable pageable) {
        System.out.println("in closeTapal");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID statusdraft = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            UUID statusclose = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            UUID workflowtypeid = WorkFlowType.INWARD.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countCloseTapal(context, context.getCurrentUser().getID(), statusdraft,statusclose,workflowtypeid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.closeTapal(context, context.getCurrentUser().getID(), statusdraft,statusclose,workflowtypeid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            System.out.println("out closeTapal");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "closeEFile")
    public Page<WorkflowProcessDTO> closeEFile(Pageable pageable) {
        System.out.println("in closeEFile");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID statusdraft = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            UUID statusclose = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            UUID workflowtypeid = WorkFlowType.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countCloseTapal(context, context.getCurrentUser().getID(), statusdraft,statusclose,workflowtypeid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.closeTapal(context, context.getCurrentUser().getID(), statusdraft,statusclose,workflowtypeid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            System.out.println("out closeTapal");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "acknowledgementTapal")
    public Page<WorkflowProcessDTO> acknowledgementTapal(Pageable pageable) {
        System.out.println("in acknowledgementTapal");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID statusdraft = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            UUID statusclose = WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            UUID workflowtypeid = WorkFlowType.INWARD.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countacknowledgementTapal(context, context.getCurrentUser().getID(), statusdraft,statusclose,workflowtypeid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.acknowledgementTapal(context, context.getCurrentUser().getID(), statusdraft,statusclose,workflowtypeid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            System.out.println("out acknowledgementTapal");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "dispatchTapal")
    public Page<WorkflowProcessDTO> dispatchTapal(Pageable pageable) {
        System.out.println("in dispatchTapal");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID statusdraft = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            UUID statusclose = WorkFlowStatus.DISPATCHCLOSE.getUserTypeFromMasterValue(context).get().getID();
            UUID workflowtypeid = WorkFlowType.INWARD.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countdispatchTapal(context, context.getCurrentUser().getID(), statusdraft,statusclose,workflowtypeid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.dispatchTapal(context, context.getCurrentUser().getID(), statusdraft,statusclose,workflowtypeid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            System.out.println("out dispatchTapal");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "parkedWorkflow")
    public Page<WorkflowProcessDTO> parkedWorkflow(Pageable pageable) {
        System.out.println("in parkedWorkflow");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID statusdraft = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            UUID statusparked = WorkFlowStatus.PARKED.getUserTypeFromMasterValue(context).get().getID();
            //you get file work flow the change workflow type just
            UUID workflowtypeid = WorkFlowType.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countparkedFlow(context, context.getCurrentUser().getID(), statusdraft,statusparked,workflowtypeid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.parkedFlow(context, context.getCurrentUser().getID(), statusdraft,statusparked,workflowtypeid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            System.out.println("out parkedWorkflow");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "dashboard")
    public Page<WorkFlowProcessRest> dashboard(Context context, Pageable pageable) {
        log.info("in dashboard start");
        context.turnOffAuthorisationSystem();
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            UUID statusid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countgetHistoryByNotOwnerAndNotDraft(context, context.getCurrentUser().getID(), statusid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.getHistoryByNotOwnerAndNotDraft(context, context.getCurrentUser().getID(), statusid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            log.info("in dashboard start");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("in dashboard Error " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getDraft")
    public Page<WorkFlowProcessRest> getDraft(Pageable pageable) {
        log.info("in getDraft start");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID workflowtypeid = WorkFlowType.INWARD.getUserTypeFromMasterValue(context).get().getID();
            UUID statusid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countgetHistoryByOwnerAndIsDraft(context, context.getCurrentUser().getID(), statusid,workflowtypeid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.getHistoryByOwnerAndIsDraft(context, context.getCurrentUser().getID(), statusid,workflowtypeid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            log.info("in getDraft stop!");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("in getDraft Error" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getDraftByWorkFlowType")
    public Page<WorkFlowProcessRest> getDraftByWorkFlowType(@Parameter(value = "uuid", required = true) UUID typeid, Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID statusid = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            int count = workflowProcessService.countgetHistoryByOwnerAndIsDraft(context, context.getCurrentUser().getID(), statusid,typeid);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.getHistoryByOwnerAndIsDraft(context, context.getCurrentUser().getID(), statusid,typeid, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            log.info("in getDraft stop!");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "filterbyInwardAndOutWard")
    public Page<WorkFlowProcessRest> filterbyInwardAndOutWard(
            @Parameter(value = "subject",required = false)String subject,
            @Parameter(value = "priority",required = false)String priority,
            @Parameter(value = "status",required = false)String status,
            @Parameter(value = "type",required = false)String type,
            @Parameter(value = "department",required = false)String department,
            @Parameter(value = "categoryRest",required = false)String categoryRest,
            @Parameter(value = "subcategoryRest",required = false)String subcategoryRest,
            @Parameter(value = "officeRest",required = false)String officeRest,
            @Parameter(value = "inwardmodeRest",required = false)String inwardmodeRest,
            @Parameter(value = "outwardmodeRest",required = false)String outwardmodeRest,
            @Parameter(value = "outwardmedium",required = false)String outwardmedium,
            @Parameter(value = "designation",required = false)String designation,
            @Parameter(value = "inwarddate",required = false)String inwarddate,
            @Parameter(value = "outwarddate",required = false)String outwarddate,
            @Parameter(value = "receiveddate",required = false)String receiveddate,
            @Parameter(value = "username",required = false)String username,
            @Parameter(value = "user",required = false)String user,
            @Parameter(value = "sendername",required = false)String sendername,
            @Parameter(value = "senderphonenumber",required = false)String senderphonenumber,
            @Parameter(value = "senderaddress",required = false)String senderaddress,
            @Parameter(value = "sendercity",required = false)String sendercity,
            @Parameter(value = "sendercountry",required = false)String sendercountry,
            @Parameter(value = "senderpincode",required = false)String senderpincode,
            @Parameter(value = "inward",required = false)String inward,
            @Parameter(value = "outward",required = false)String outward,
            Pageable pageable) {
        try {
            Context context = obtainContext(); context.turnOffAuthorisationSystem();
            System.out.println("::::::::::::::::::::start filterbyInwardAndOutWard :::::::::::::::::::");
            HashMap<String, String> map = new HashMap<>();
            if (priority != null) {
                map.put("priority", priority);
            }
            if (status != null) {
                map.put("status", status);
            }
            if (type != null) {
                map.put("type", type);
            }
            if (department != null) {
                map.put("department",department);
            }
            if (categoryRest != null) {
                map.put("categoryRest", categoryRest);
            }
            if (subcategoryRest != null) {
                map.put("subcategoryRest", subcategoryRest);
            }
            if (officeRest != null) {
                map.put("officeRest",officeRest);
            }
            if (inwardmodeRest!= null) {
                map.put("inwardmodeRest", inwardmodeRest);
            }
            if (outwardmodeRest!= null) {
                map.put("outwardmodeRest", outwardmodeRest);
            }
            if (outwardmedium!= null) {
                map.put("outwardmedium", outwardmedium);
            }
            if (designation != null) {
                map.put("designation", designation);
            }
            if (user != null) {
                map.put("user", user);
            }
            //text
            if (subject!= null) {
                map.put("subject", subject);
            }
            if (inwarddate!= null) {
                map.put("inwarddate", inwarddate);
            }
            if (outwarddate != null) {
                map.put("outwarddate", outwarddate);
            }
            if (receiveddate!= null) {
                map.put("receiveddate", receiveddate);
            }
            if (username != null) {
                map.put("username", username);
            }
            if (sendername!= null) {
                map.put("sendername", sendername);
            }
            if (senderphonenumber!= null) {
                map.put("senderphonenumber", senderphonenumber);
            }
            if (senderaddress != null) {
                map.put("senderaddress", senderaddress);
            }
            if (sendercity != null) {
                map.put("sendercity",sendercity);
            }
            if (sendercountry!= null) {
                map.put("sendercountry",sendercountry);
            }
            if (senderpincode!= null) {
                map.put("senderpincode", senderpincode);
            }
            if (inward!= null) {
                map.put("inward", inward);
            }
            if (outward!= null) {
                map.put("outward", outward);
            }
            UUID workflowtype_draftid = WorkFlowType.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            String uui=workflowtype_draftid.toString();
            map.put("draftid",uui);
            System.out.println(map);
            int count = workflowProcessService.countfilterInwarAndOutWard(context, map, Math.toIntExact(pageable.getOffset()),Math.toIntExact(pageable.getPageSize()));
            List<WorkflowProcess> list = workflowProcessService.filterInwarAndOutWard(context, map, Math.toIntExact(pageable.getOffset()),Math.toIntExact(pageable.getPageSize()));
            List<WorkFlowProcessRest> rests = list.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context,d, utils.obtainProjection());
            }).collect(Collectors.toList());
            System.out.println("::::::::::::::::::::stop filterbyInwardAndOutWard :::::::::::::::::::");
            return new PageImpl(rests, pageable,count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getDraftNotePendingWorkflow")
    public Page<WorkFlowProcessRest> getDraftNotePendingWorkflow(Pageable pageable) {
        log.info("in getDraftNotePendingWorkflow start");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID statuscloseid = WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getID();
            UUID statusdraft = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Workflow Type");
            UUID statusdraftid = null;
            if (workFlowProcessMaster != null) {
                statusdraftid = workFlowProcessMasterValueService.findByName(context, "Draft", workFlowProcessMaster).getID();
            }
            int count = workflowProcessService.countfindDraftPending(context, context.getCurrentUser().getID(), statuscloseid, statusdraftid, statusdraft);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.findDraftPending(context, context.getCurrentUser().getID(), statuscloseid, statusdraftid, statusdraft, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            log.info("in getDraftNotePendingWorkflow stop!");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("in getDraftNotePendingWorkflow Error" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getReferWorkflow")
    public Page<WorkflowProcessDTO> getReferWorkflow(Pageable pageable) {
        log.info("in getReferWorkflow start!");
        List<WorkFlowProcessRest> workflowsRes = new ArrayList<WorkFlowProcessRest>();
        List<WorkflowProcessDTO> workflowsRes1 = new ArrayList<WorkflowProcessDTO>();
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID referstatusid = WorkFlowStatus.REFER.getUserTypeFromMasterValue(context).get().getID();
            UUID statusdraft = WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            WorkFlowProcessMaster workFlowProcessMaster = workFlowProcessMasterService.findByName(context, "Workflow Type");
            UUID statusdraftid = null;
            if (workFlowProcessMaster != null) {
                statusdraftid = workFlowProcessMasterValueService.findByName(context, "Draft", workFlowProcessMaster).getID();
            }
            int count = workflowProcessService.countRefer(context, context.getCurrentUser().getID(), referstatusid, statusdraftid, statusdraft);
            List<WorkflowProcess> workflowProcesses = workflowProcessService.findReferList(context, context.getCurrentUser().getID(), referstatusid, statusdraftid, statusdraft, Math.toIntExact(pageable.getOffset()), Math.toIntExact(pageable.getPageSize()));
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context, d, utils.obtainProjection());
            }).collect(toList());
            log.info("in getReferWorkflow stop!");
            return new PageImpl(workflowsRes, pageable, count);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("in getReferWorkflow Error " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SearchRestMethod(name = "getDocumentByItemID")
    public WorkFlowProcessRest getDocumentByItemID(@Parameter(value = "itemid", required = true) UUID itemid) {
        log.info("in getDocumentByItemID start!");
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            WorkflowProcess witems = workflowProcessService.getNoteByItemsid(context, itemid);
            log.info("in getDocumentByItemID stop!");
            return workFlowProcessConverter.convert(witems, utils.obtainProjection());
        } catch (SQLException e) {
            e.printStackTrace();
            log.info("in getDocumentByItemID Error" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

     @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "searchByTypeandSubject")
    public Page<WorkFlowProcessRest> searchByTypeandSubject(@Parameter(value = "workflowtypeid", required = true) UUID workflowtypeid, @Parameter(value = "subject", required = true) String subject, Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes = null;
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            Optional<List<WorkflowProcess>> workflowProcesses = Optional.ofNullable(workflowProcessService.searchSubjectByWorkflowTypeandSubject(context, workflowtypeid, subject));
            if (workflowProcesses.isPresent()) {
                workflowsRes = workflowProcesses.get().stream().map(d -> {
                    return workFlowProcessConverter.convertsearchBySubject(d);
                }).collect(toList());
                return new PageImpl(workflowsRes, pageable, 9999);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}