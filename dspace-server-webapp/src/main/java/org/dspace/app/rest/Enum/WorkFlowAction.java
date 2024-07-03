/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.Enum;

import com.google.gson.Gson;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.jbpm.models.JBPMResponse_;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.model.WorkflowProcessEpersonRest;
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.app.rest.utils.PdfUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public enum WorkFlowAction {
    MASTER("Action"),
    CREATE("Create") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            if (workflowProcess.getWorkflowType().getPrimaryvalue().equals("Inward")) {
                System.out.println(":::::::::::::in inward flow  create:::::Action ");
                Optional<WorkflowProcessEpersonRest> optionalWorkflowProcessEpersonRest = workFlowProcessRest.getWorkflowProcessEpersonRests().stream().filter(d -> d.getIndex() == 1).findFirst();
                if (optionalWorkflowProcessEpersonRest.isPresent()) {
                    List<String> forwarduserides = new ArrayList<>();
                    forwarduserides.add(optionalWorkflowProcessEpersonRest.get().getId());
                    List<Object> usersUuid = new ArrayList<>(forwarduserides);
                    System.out.println("user id" + forwarduserides);
                    String jbpmResponce = this.getJbpmServer().startProcess(workFlowProcessRest, usersUuid);
                    JBPMResponse_ jbpmResponse = new Gson().fromJson(jbpmResponce, JBPMResponse_.class);
                    System.out.println("jbpm responce create" + new Gson().toJson(jbpmResponse));
                    this.setComment(null);
                    WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
                    this.setComment(null);
                    return this.storeWorkFlowHistoryforDocumentReference(context, workflowProcess, currentOwner, workFlowProcessRest);
                }
            }
            //other creted like outward draft
            List<Object> usersUuid = this.removeInitiatorgetUserList2(context, workFlowProcessRest);
            System.out.println("useruiid:::::::::::::::::" + usersUuid);
            if (usersUuid != null) {
                String comment = null;
                String jbpmResponce = this.getJbpmServer().startProcess(workFlowProcessRest, usersUuid);
                JBPMResponse_ jbpmResponse = new Gson().fromJson(jbpmResponce, JBPMResponse_.class);
                System.out.println("jbpm responce create" + new Gson().toJson(jbpmResponse));
                this.setComment(null);
                WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
                Optional<WorkflowProcessReferenceDoc> workflowProcessReferenceDoc = workflowProcess.getWorkflowProcessReferenceDocs().stream().filter(d -> d != null)
                        .filter(d -> d.getDrafttype() != null)
                        .filter(d -> d.getDrafttype().getPrimaryvalue() != null)
                        .filter(d -> d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Notsheet"))
                        .findFirst();
                if (workflowProcessReferenceDoc.isPresent()) {
                    if (workflowProcessReferenceDoc.get().getEditortext() != null && !workflowProcessReferenceDoc.get().getEditortext().isEmpty()) {
                        comment = (workflowProcessReferenceDoc.get().getEditortext());
                        this.setComment(comment);
                    }
                }
                WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
                return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
            } else {
                throw new RuntimeException("initiator not  found.....");
            }
        }
    },
    FORWARD("Forward") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            List<Object> usersUuid = null;
            if (this.getInitiatorForward()) {
                System.out.println("In InitiatorForward ");
                usersUuid = this.getIsInitiatorForward(workFlowProcessRest);
            } else if (this.getInitiator()) {
                System.out.println("In Initiator ");
                usersUuid = new ArrayList<Object>();
            } else {
                System.out.println(" normal flow");
                usersUuid = this.removeInitiatorgetUserList2Forward(context, workFlowProcessRest);
            }
            System.out.println("user list " + usersUuid);
            String forwardResponce = this.getJbpmServer().forwardTask(workFlowProcessRest, usersUuid);
            System.out.println("forward jbpm responce create" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    BACKWARD("Backward") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            String forwardResponce = this.getJbpmServer().backwardTask(workFlowProcessRest);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            System.out.println("jbpmResponse:: Backward" + new Gson().toJson(jbpmResponse));
            if (workflowProcess.getWorkflowType().getPrimaryvalue().equals("Draft")) {
                this.setIsbackward(true);
            }
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            this.setIsbackward(false);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    REFER("Refer") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            String referusersUuid = this.getreferUserID(workflowProcess);
            String forwardResponce = this.getJbpmServer().refer(workFlowProcessRest, referusersUuid);
            System.out.println("Refer jbpm responce :" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    RECEIVED("Received") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            String forwardResponce = this.getJbpmServer().received(workFlowProcessRest);
            System.out.println("Refer jbpm responce :" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    HOLD("Hold") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            //List<String> usersUuid = this.removeInitiatorgetUserList(workFlowProcessRest);
            String forwardResponce = this.getJbpmServer().holdTask(workFlowProcessRest);
            System.out.println("suspend jbpm responce create" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            //WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkflowProcessEperson currentOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getOwner()).findFirst().get();
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }

    },
    UNHOLD("UnHold") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            //List<String> usersUuid = this.removeInitiatorgetUserList(workFlowProcessRest);
            String forwardResponce = this.getJbpmServer().resumeTask(workFlowProcessRest);
            System.out.println("suspend jbpm responce create" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    REJECTED("Rejected") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            WorkflowProcessEperson currentOwner = this.changeOwnerByReject(context, workflowProcess);
            System.out.println("Reject action " + this.getComment());
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            this.setComment(null);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    DELETE("Delete"),
    UPDATE("Update"),
    APPROVED("Approved"),
    PENDING("Pending"),
    CALLBACK("CallBack") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            this.setIscallback(true);
            String forwardResponce = this.getJbpmServer().callback(workFlowProcessRest);
            System.out.println("CALLBACK::::::::responce String:::::::::" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            System.out.println("jbpmResponse:: CALLBACK " + new Gson().toJson(jbpmResponse));
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            this.setIscallback(false);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            this.setComment(null);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    },
    COMPLETE("Complete") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            String forwardResponce = this.getJbpmServer().completeTask(workFlowProcessRest, new ArrayList<>());
            System.out.println("completed::::::::responce String:::::::::" + forwardResponce);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            System.out.println("jbpmResponse:: Complete" + new Gson().toJson(jbpmResponse));
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            System.out.println("this is Complete Comment" + this.getComment());
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            this.setComment(null);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    }, DISPATCHCLOSE("Dispatch close") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            WorkflowProcessEperson currentOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner()).findFirst().get();
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            this.setComment(null);
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    }, PARKED("Parked") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            System.out.println("parked action ");
            String epersion = context.getCurrentUser().getID().toString();
            WorkFlowProcessHistory workFlowAction=null;
            WorkflowProcessEperson currentOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson().getID().toString().equalsIgnoreCase(epersion)).findFirst().get();
            if (currentOwner != null) {
                System.out.println("store History park");
                workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
                this.setComment(null);
                return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
            }else {
                return null;
            }
        }
    },
    REOPEN("Re Open") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            System.out.println("Re-Open Parked Workflow");
            String epersion = context.getCurrentUser().getID().toString();
            WorkFlowProcessHistory workFlowAction=null;
            WorkflowProcessEperson currentOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson().getID().toString().equalsIgnoreCase(epersion)).findFirst().get();
            if (currentOwner != null) {
                System.out.println("store History park");
                workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
                this.setComment(null);
                return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
            }else {
                return null;
            }
        }
    },
    DISPATCH("Dispatch Ready") {
        @Override
        public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
            List<String> usersUuid = this.removeInitiatorgetUserList(workFlowProcessRest);
            System.out.println("normal user::" + usersUuid.toString());
            List<String> dispatchusersUuid = this.getDispatchUsers(workFlowProcessRest);
            System.out.println("dispatchusersUuid user::" + dispatchusersUuid.toString());
            String forwardResponce = this.getJbpmServer().dispatchReady(workFlowProcessRest, usersUuid, dispatchusersUuid);
            JBPMResponse_ jbpmResponse = new Gson().fromJson(forwardResponce, JBPMResponse_.class);
            System.out.println("Dispatch Ready" + new Gson().toJson(jbpmResponse));
            WorkflowProcessEperson currentOwner = this.changeOwnership(context, jbpmResponse, workflowProcess);
            WorkFlowProcessHistory workFlowAction = this.storeWorkFlowHistory(context, workflowProcess, currentOwner, workFlowProcessRest);
            //workFlowAction.setComment(this.getComment());
            return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
        }
    };

    private String action;
    private String comment;

    private Boolean isInitiator = false;

    private Boolean isInitiatorForward = false;
    private Boolean isbackward = false;
    private Boolean isrefer = false;

    private Boolean iscallback = false;

    private List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs;
    private WorkFlowProcessHistoryService workFlowProcessHistoryService;
    private WorkFlowProcessCommentService workFlowProcessCommentService;
    private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    private WorkFlowProcessMasterService workFlowProcessMasterService;
    private WorkflowProcessEpersonService workflowProcessEpersonService;
    private WorkflowProcessReferenceDocService workflowProcessReferenceDocService;

    private JbpmServerImpl jbpmServer;

    @Component
    public static class ServiceInjector {
        @Autowired
        private WorkFlowProcessHistoryService workFlowProcessHistoryService;
        @Autowired
        private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
        @Autowired
        private WorkFlowProcessMasterService workFlowProcessMasterService;

        @Autowired
        private WorkflowProcessReferenceDocService workflowProcessReferenceDocService;
        @Autowired
        JbpmServerImpl jbpmServer;

        @Autowired
        private WorkflowProcessEpersonService workflowProcessEpersonService;

        @PostConstruct
        public void postConstruct() {
            for (WorkFlowAction rt : EnumSet.allOf(WorkFlowAction.class)) {
                rt.setWorkFlowProcessHistoryService(workFlowProcessHistoryService);
                rt.setWorkFlowProcessMasterValueService(workFlowProcessMasterValueService);
                rt.setWorkFlowProcessMasterService(workFlowProcessMasterService);
                rt.setJbpmServer(jbpmServer);
               // rt.setModelMapper(modelMapper);
                rt.setWorkflowProcessEpersonService(workflowProcessEpersonService);
                rt.setWorkflowProcessReferenceDocService(workflowProcessReferenceDocService);
            }
        }
    }

    public Boolean getIsrefer() {
        return isrefer;
    }

    public void setIsrefer(Boolean isrefer) {
        this.isrefer = isrefer;
    }

    public Boolean getIscallback() {
        return iscallback;
    }

    public void setIscallback(Boolean iscallback) {
        this.iscallback = iscallback;
    }

    WorkFlowAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAction() {
        return action;
    }

    public Boolean getInitiator() {
        return isInitiator;
    }

    public void setInitiator(Boolean initiator) {
        isInitiator = initiator;
    }

    public Boolean getInitiatorForward() {
        return isInitiatorForward;
    }

    public void setInitiatorForward(Boolean initiatorForward) {
        isInitiatorForward = initiatorForward;
    }

    public List<WorkflowProcessReferenceDoc> getWorkflowProcessReferenceDocs() {
        return workflowProcessReferenceDocs;
    }

    public void setWorkflowProcessReferenceDocs(List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs) {
        this.workflowProcessReferenceDocs = workflowProcessReferenceDocs;
    }

    public Boolean getIsbackward() {
        return isbackward;
    }

    public void setIsbackward(Boolean isbackward) {
        this.isbackward = isbackward;
    }

    public String getreferUserID(WorkflowProcess workFlowProcessRest) {
        System.out.println("in find referid");
        Integer index = workFlowProcessRest.getWorkflowProcessEpeople().size() - 1;
        System.out.println("Refer :::::user::::::index:::::::" + index);
        try {
            return workFlowProcessRest.getWorkflowProcessEpeople().stream()
                    .filter(s -> s.getIsrefer() != null)
                    .filter(s -> s.getIndex() == index)
                    .map(d -> d.getID().toString())
                    .findFirst().get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> removeInitiatorgetUserList(WorkFlowProcessRest workFlowProcessRest) {
        return workFlowProcessRest.getWorkflowProcessEpersonRests().stream()
                .filter(wei -> !wei.getUserType().getPrimaryvalue().equals(WorkFlowUserType.INITIATOR.getAction()))
                .filter(wei -> !wei.getUserType().getPrimaryvalue().equals(WorkFlowUserType.DISPATCH.getAction()))
                .sorted(Comparator.comparing(WorkflowProcessEpersonRest::getIndex)).map(d -> d.getUuid()).collect(Collectors.toList());
    }

    public List<Object> getIsInitiatorForward(WorkFlowProcessRest workFlowProcessRest) {
        return workFlowProcessRest.getWorkflowProcessEpersonRests().stream()
                .filter(wei -> !wei.getUserType().getPrimaryvalue().equals(WorkFlowUserType.INITIATOR.getAction()))
                .filter(wei -> !wei.getUserType().getPrimaryvalue().equals(WorkFlowUserType.DISPATCH.getAction()))
                .filter(ww -> !ww.getIssequence() == true)
                .filter(ss -> !ss.getIssequence())
                .sorted(Comparator.comparing(WorkflowProcessEpersonRest::getIndex)).map(d -> d.getUuid()).collect(Collectors.toList());
    }

    public List<Object> removeInitiatorgetUserList2(Context context, WorkFlowProcessRest workFlowProcessRest) {
        List<Object> userlist = new ArrayList<>();
        List<WorkflowProcessEpersonRest> removeInitiatorafterlist = workFlowProcessRest.getWorkflowProcessEpersonRests().stream()
                .filter(wei -> !wei.getUserType().getPrimaryvalue().equals(WorkFlowUserType.INITIATOR.getAction()))
                .sorted(Comparator.comparing(WorkflowProcessEpersonRest::getSequence)).collect(Collectors.toList());

        List<WorkflowProcessEpersonRest> tmplist = removeInitiatorafterlist;
        List<Integer> indexAllEpersonList = removeInitiatorafterlist.stream().map(d -> d.getIndex()).collect(Collectors.toList());
        //this check how many time dublicate index
        Map<Integer, Long> multipleusersameindex = indexAllEpersonList.stream().collect(Collectors.groupingBy(i -> i, Collectors.counting()));
        System.out.println("::::::::::::::size::::::::::::::::::" + removeInitiatorafterlist.size());
        for (int i = 1; i <= removeInitiatorafterlist.size(); i++) {
            System.out.println("::::::test ::::::::count in loop");
            //check index one then more
            if (indexAllEpersonList.contains(i) && multipleusersameindex.get(i) > 1) {
                int finalIq = i;
                if (tmplist.stream().filter(dd -> dd.getIndex() == finalIq).sorted(Comparator.comparing(WorkflowProcessEpersonRest::getSequence)).map(d -> d.getUuid()).collect(Collectors.toList()) != null) {
                    userlist.add(tmplist.stream().filter(dd -> dd.getIndex() == finalIq).sorted(Comparator.comparing(WorkflowProcessEpersonRest::getSequence)).map(d -> d.getUuid()).collect(Collectors.toList()));
                    System.out.println("mltiuser list :" + removeInitiatorafterlist.stream().filter(dd -> dd.getIndex() == finalIq).map(d -> d.getUuid()).collect(Collectors.toList()));
                }
            } else {
                int finalI = i;
                if (removeInitiatorafterlist.stream().filter(d -> d.getIndex().equals(finalI)).map(d -> d.getId()).findFirst().isPresent()) {
                    System.out.println("::::::::::::::single::::::::::::" + removeInitiatorafterlist.stream().filter(d -> d.getIndex().equals(finalI)).map(d -> d.getId()).findFirst().get().toString());
                    userlist.add(removeInitiatorafterlist.stream().filter(d -> d.getIndex().equals(finalI)).map(d -> d.getId()).findFirst().get().toString());
                }
            }
        }
        return userlist;
    }

    public List<Object> removeInitiatorgetUserList2Forward(Context context, WorkFlowProcessRest workFlowProcessRest) {
        List<Object> userlist = new ArrayList<>();
        List<WorkflowProcessEpersonRest> removeInitiatorafterlist = workFlowProcessRest.getWorkflowProcessEpersonRests().stream()
                .filter(wei -> !wei.getUserType().getPrimaryvalue().equals(WorkFlowUserType.INITIATOR.getAction()))
                .sorted(Comparator.comparing(WorkflowProcessEpersonRest::getIndex)).collect(Collectors.toList());
        //remove Initiator after workflowEperson index list
        List<WorkflowProcessEpersonRest> tmplist = removeInitiatorafterlist;
        List<Integer> indexAllEpersonList = removeInitiatorafterlist.stream().map(d -> d.getIndex()).collect(Collectors.toList());
        //this check how many time dublicate index
        Map<Integer, Long> multipleusersameindex = indexAllEpersonList.stream().collect(Collectors.groupingBy(i -> i, Collectors.counting()));
        System.out.println("::::::::::::::size::::::::::::::::::" + removeInitiatorafterlist.size());
        int currentuserindex = removeInitiatorafterlist.stream().filter(d -> d.getePersonRest() != null).filter(dd -> dd.getePersonRest().getId() != null).filter(dd -> dd.getePersonRest().getId().equalsIgnoreCase(context.getCurrentUser().getID().toString())).findFirst().get().getIndex();
        System.out.println("current user index is : " + currentuserindex);
        for (int i = currentuserindex; i <= removeInitiatorafterlist.size(); i++) {
            System.out.println("::::::test ::::::::count in loop");
            //check index one then more in this if
            if (indexAllEpersonList.contains(i) && multipleusersameindex.get(i) > 1) {
                int finalIq = i;
                if (tmplist.stream().filter(dd -> dd.getIndex() == finalIq).sorted(Comparator.comparing(WorkflowProcessEpersonRest::getSequence)).map(d -> d.getUuid()).collect(Collectors.toList()) != null) {
                    userlist.add(tmplist.stream().filter(dd -> dd.getIndex() == finalIq).sorted(Comparator.comparing(WorkflowProcessEpersonRest::getSequence)).map(d -> d.getUuid()).collect(Collectors.toList()));
                    System.out.println("::::::::::::::multiuser::::::::::::" + tmplist.stream().filter(dd -> dd.getIndex() == finalIq).sorted(Comparator.comparing(WorkflowProcessEpersonRest::getSequence)).map(d -> d.getUuid()).collect(Collectors.toList()));
                }
            } else {
                int finalI = i;
                if (removeInitiatorafterlist.stream().filter(d -> d.getIndex().equals(finalI)).map(d -> d.getId()).findFirst().isPresent()) {
                    System.out.println("::::::::::::::single::::::::::::" + removeInitiatorafterlist.stream().filter(d -> d.getIndex().equals(finalI)).map(d -> d.getId()).findFirst().get().toString());
                    userlist.add(removeInitiatorafterlist.stream().filter(d -> d.getIndex().equals(finalI)).map(d -> d.getId()).findFirst().get().toString());
                }
            }
        }
        return userlist;
    }

    public static int countOccurrences(List<Integer> inputList, int numberToFind) {
        return Collections.frequency(inputList, numberToFind);
    }

    public List<Object> noteremoveInitiatorgetUserList(WorkFlowProcessRest workFlowProcessRest) {
        List<Object> ll = new ArrayList<>();
        return ll;
    }

    public List<String> getDispatchUsers(WorkFlowProcessRest workFlowProcessRest) {
        return workFlowProcessRest.getWorkflowProcessEpersonRests().stream()
                .filter(wei -> wei.getUserType().getPrimaryvalue().equals(WorkFlowUserType.DISPATCH.getAction()))
                .sorted(Comparator.comparing(WorkflowProcessEpersonRest::getIndex)).map(d -> d.getUuid()).collect(Collectors.toList());
    }

    public WorkFlowProcessHistory perfomeAction(Context context, WorkflowProcess workflowProcess, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
        WorkFlowProcessHistory workFlowAction = new WorkFlowProcessHistory();
        System.out.println("Action::::" + this.getAction() + this.getWorkFlowProcessMasterService());
        WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
        System.out.println("workFlowProcessMaster Master Name::" + workFlowProcessMaster.getMastername());
        WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setComment(this.getComment());
        return this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
    }

    public WorkFlowProcessHistory storeWorkFlowHistory(Context context, WorkflowProcess workflowProcess, WorkflowProcessEperson workflowProcessEperson, WorkFlowProcessRest workFlowProcessRest) throws SQLException, AuthorizeException {
        System.out.println("::::::IN :storeWorkFlowHistory:::::::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
        WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setWorkflowProcess(workflowProcess);
        workFlowAction.setAction(workFlowProcessMasterValue);
        Optional<WorkflowProcessEperson> sentto = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getOwner()).findFirst();
        if (sentto.isPresent()) {
            workFlowAction.setSentto(sentto.get());

        }
        if (workFlowProcessRest.getCurrentrecipient() != null) {
            workFlowAction.setSenttoname(workFlowProcessRest.getCurrentrecipient());
        }else{
            if(sentto.isPresent()) {
                workFlowAction.setSenttoname(sentto.get().getePerson().getFullName());
            }
        }
        if (workFlowProcessRest.getSendername() != null) {
            workFlowAction.setSentbyname(workFlowProcessRest.getSendername());
        }
        if (workFlowProcessMasterValue != null && workFlowProcessMasterValue.getPrimaryvalue() != null && workFlowProcessMasterValue.getPrimaryvalue().equalsIgnoreCase("Received")) {
            workFlowAction.setComment("Dack received by " + workflowProcessEperson.getePerson().getFullName() + ".");
            return workFlowAction;
        }
        if (workFlowProcessMasterValue != null && workFlowProcessMasterValue.getPrimaryvalue() != null && workFlowProcessMasterValue.getPrimaryvalue().equalsIgnoreCase("CallBack")) {
            workFlowAction.setComment(" Received.");
            return workFlowAction;
        }
        if (!DateUtils.isNullOrEmptyOrBlank(workFlowProcessRest.getRemark())) {
            System.out.println("remark :" + workFlowProcessRest.getRemark());
            workFlowAction.setComment(workFlowProcessRest.getRemark());
        }

        if (this.getComment() != null && !this.getComment().isEmpty()) {
            if (workflowProcess.getWorkflowType().getPrimaryvalue().equals("Draft")) {
                String htmlcomment = "<div>" + this.getComment() + "</div>";
                System.out.println("::::::html::::::::::" + htmlcomment);
               // System.out.println("::::::text:::::" + PdfUtils.htmlToText(htmlcomment));
                WorkFlowProcessComment workFlowProcessComment = new WorkFlowProcessComment();
                //workFlowProcessComment.setComment(PdfUtils.htmlToText(htmlcomment));
                workFlowProcessComment.setWorkFlowProcessHistory(workFlowAction);
                workFlowProcessComment.setSubmitter(context.getCurrentUser());
                workFlowProcessComment.setWorkFlowProcess(workflowProcess);
            }
        }
        System.out.println("::::::OUT :storeWorkFlowHistory:::::::::: ");
        return workFlowAction;
    }

    // this history call when draft note create time Attaged Reference Doc.
    public WorkFlowProcessHistory storeWorkFlowHistoryforDocumentReference(Context context, WorkflowProcess workflowProcess, WorkflowProcessEperson workflowProcessEperson, WorkFlowProcessRest workFlowProcessRest) {
        WorkFlowProcessHistory workFlowAction = null;
        try {
            if (workflowProcess.getWorkflowType() != null && workflowProcess.getWorkflowType().getPrimaryvalue() != null && workflowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Inward") || workflowProcess.getWorkflowType() != null && workflowProcess.getWorkflowType().getPrimaryvalue() != null && workflowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Outward")) {
                if (workflowProcess.getDispatchmode() != null && workflowProcess.getWorkFlowProcessInwardDetails() != null) {
                    workFlowAction = new WorkFlowProcessHistory();
                    workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                    if (workFlowProcessRest.getCurrentrecipient() != null) {
                        workFlowAction.setSenttoname(workFlowProcessRest.getCurrentrecipient());
                    }
                    if (workFlowProcessRest.getSendername() != null) {
                        workFlowAction.setSentbyname(workFlowProcessRest.getSendername());
                    }
                    workFlowAction.setActionDate(new Date());
                    workflowProcess.getWorkFlowProcessInwardDetails().getReceivedDate();
                    if (workflowProcess.getWorkFlowProcessInwardDetails() != null && workflowProcess.getWorkFlowProcessInwardDetails().getReceivedDate() != null) {
                        workFlowAction.setReceivedDate(workflowProcess.getWorkFlowProcessInwardDetails().getReceivedDate());
                    }
                    Optional<WorkflowProcessEperson> sento = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getOwner()).findFirst();
                    if (sento.isPresent()) {
                        System.out.println("sent to::::::::" + sento.get().getePerson().getEmail());
                        workFlowAction.setSentto(sento.get());
                    }
                    WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                    WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                    workFlowAction.setAction(workFlowProcessMasterValue);
                    workFlowAction.setWorkflowProcess(workflowProcess);
                    workFlowAction.setComment((workflowProcess.getRemark() != null ? workflowProcess.getRemark() : "-"));
                    this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                }
                if (workflowProcess.getWorkFlowProcessOutwardDetails() != null && workflowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium() != null) {
                    //add Notsheet  Histoy
                    workFlowAction = new WorkFlowProcessHistory();
                    WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                    workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                    WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                    workFlowAction.setActionDate(new Date());
                    workFlowAction.setAction(workFlowProcessMasterValue);
                    workFlowAction.setWorkflowProcess(workflowProcess);
                    workFlowAction.setComment("Outward Medium is " + workflowProcess.getWorkFlowProcessOutwardDetails().getOutwardmedium().getPrimaryvalue() + ".");
                    this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                }
            }
            if (workflowProcess.getWorkflowProcessReferenceDocs() != null && workflowProcess.getWorkflowProcessReferenceDocs().size() != 0) {
                System.out.println("::::::IN :storeWorkFlowHistory::::DocumentReference:::::: ");
                Comparator<WorkflowProcessReferenceDoc> c = (a, b) -> a.getCreatedate().compareTo(b.getCreatedate());
                List<WorkflowProcessReferenceDoc> list = workflowProcess.getWorkflowProcessReferenceDocs().stream().sorted(c).collect(Collectors.toList());
                System.out.println("size:::::::::::::::" + list.size());
                if (workflowProcess.getWorkflowType() != null && workflowProcess.getWorkflowType().getPrimaryvalue() != null && workflowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Draft")) {
                    for (WorkflowProcessReferenceDoc doc : list) {
                        if (doc.getDrafttype() != null && doc.getDrafttype().getPrimaryvalue() != null && doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Document")) {
                            workFlowAction = new WorkFlowProcessHistory();
                            WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                            workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                            WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                            workFlowAction.setActionDate(new Date());
                            workFlowAction.setAction(workFlowProcessMasterValue);
                            workFlowAction.setWorkflowProcess(workflowProcess);
                            Optional<WorkflowProcessEperson> sento = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getOwner()).findFirst();
                            if (sento.isPresent()) {
                                System.out.println("sent to::::::::" + sento.get().getePerson().getEmail());
                                workFlowAction.setSentto(sento.get());
                            }
                            workFlowAction.setComment("Attached " + doc.getDrafttype().getPrimaryvalue() + " In " + doc.getItemname());
                            this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                        }
                        if (doc.getDrafttype() != null && doc.getDrafttype().getPrimaryvalue() != null && doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reference Noting")) {
                            workFlowAction = new WorkFlowProcessHistory();
                            WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                            workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                            WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                            workFlowAction.setActionDate(new Date());
                            workFlowAction.setAction(workFlowProcessMasterValue);
                            workFlowAction.setWorkflowProcess(workflowProcess);
                            workFlowAction.setComment("Attached " + doc.getDrafttype().getPrimaryvalue() + " In " + doc.getSubject());
                            this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                        }
                        if (doc.getDrafttype() != null && doc.getDrafttype().getPrimaryvalue() != null && doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Document")) {
                            //add Document Histoy
                            workFlowAction = new WorkFlowProcessHistory();
                            WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                            workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                            WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                            workFlowAction.setActionDate(new Date());
                            workFlowAction.setAction(workFlowProcessMasterValue);
                            workFlowAction.setWorkflowProcess(workflowProcess);
                            workFlowAction.setComment("Create Version 1 for " + doc.getDrafttype().getPrimaryvalue() + " ");
                            this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                        }
                        if (doc.getDrafttype() != null && doc.getDrafttype().getPrimaryvalue() != null && doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Reply Tapal")) {
                            //add Notsheet  Histoy
                            workFlowAction = new WorkFlowProcessHistory();
                            WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                            workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                            WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                            workFlowAction.setActionDate(new Date());
                            workFlowAction.setAction(workFlowProcessMasterValue);
                            workFlowAction.setWorkflowProcess(workflowProcess);
                            workFlowAction.setComment("Create Version 1 for " + doc.getDrafttype().getPrimaryvalue() + " " + doc.getSubject());
                            this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                            Optional<WorkflowProcessEperson> sento = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getOwner()).findFirst();
                            if (sento.isPresent()) {
                                System.out.println("sent to::::::::" + sento.get().getePerson().getEmail());
                                workFlowAction.setSentto(sento.get());
                            }
                        }
                        if (doc.getDrafttype() != null && doc.getDrafttype().getPrimaryvalue() != null && doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Notesheet")) {
                            //add Notsheet  Histoy
                            workFlowAction = new WorkFlowProcessHistory();
                            WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                            workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                            WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                            workFlowAction.setActionDate(new Date());
                            workFlowAction.setAction(workFlowProcessMasterValue);
                            workFlowAction.setWorkflowProcess(workflowProcess);
                            workFlowAction.setComment("Create Version 1 for " + doc.getDrafttype().getPrimaryvalue() + " " + doc.getSubject());

                            this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                        }
                        if (doc.getDrafttype() != null && doc.getDrafttype().getPrimaryvalue() != null && doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Referral File")) {
                            System.out.println("in referal file ,,,");
                            workFlowAction = new WorkFlowProcessHistory();
                            WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                            workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                            WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                            workFlowAction.setActionDate(new Date());
                            workFlowAction.setAction(workFlowProcessMasterValue);
                            workFlowAction.setWorkflowProcess(workflowProcess);
                            if (doc.getItemname() != null) {
                                String[] s = doc.getItemname().split(":");
                                workFlowAction.setComment("Attached " + doc.getDrafttype().getPrimaryvalue() + " " + s[0]);
                            } else {
                                workFlowAction.setComment("Attached " + doc.getDrafttype().getPrimaryvalue());
                            }
                            Optional<WorkflowProcessEperson> sento = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getOwner()).findFirst();
                            if (sento.isPresent()) {
                                System.out.println("sent to::::::::" + sento.get().getePerson().getEmail());
                                workFlowAction.setSentto(sento.get());
                            }
                            this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                        }
                        if (doc.getDrafttype() != null && doc.getDrafttype().getPrimaryvalue() != null && doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Inward")) {
                            //add Notsheet  Histoy
                            workFlowAction = new WorkFlowProcessHistory();
                            WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                            workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                            WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                            workFlowAction.setActionDate(new Date());
                            workFlowAction.setAction(workFlowProcessMasterValue);
                            workFlowAction.setWorkflowProcess(workflowProcess);
                            Optional<WorkflowProcessEperson> sento = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getOwner()).findFirst();
                            if (sento.isPresent()) {
                                System.out.println("sent to::::::::" + sento.get().getePerson().getEmail());
                                workFlowAction.setSentto(sento.get());
                            }
                            workFlowAction.setComment("Attached " + doc.getDrafttype().getPrimaryvalue() + " " + doc.getSubject());
                            this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                        }
                        if (doc.getDrafttype() != null && doc.getDrafttype().getPrimaryvalue() != null && doc.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Outward")) {
                            //add Notsheet  Histoy
                            workFlowAction = new WorkFlowProcessHistory();
                            WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
                            workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
                            WorkFlowProcessMasterValue workFlowProcessMasterValue = this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
                            workFlowAction.setActionDate(new Date());
                            workFlowAction.setAction(workFlowProcessMasterValue);
                            workFlowAction.setWorkflowProcess(workflowProcess);
                            workFlowAction.setComment("Attached " + doc.getDrafttype().getPrimaryvalue() + " " + doc.getSubject());
                            this.getWorkFlowProcessHistoryService().create(context, workFlowAction);
                        }
                    }
                }
                System.out.println("::::::OUT :storeWorkFlowHistory::::DocumentReference:::::: ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workFlowAction;
    }

    public WorkflowProcessEperson changeOwnerByReject(Context context, WorkflowProcess workflowProcess) throws SQLException, AuthorizeException {
        WorkflowProcessEperson currentOwner = null;
        WorkflowProcessEperson initiator = null;
        currentOwner = workflowProcess.getWorkflowProcessEpeople()
                .stream()
                .filter(d -> d.getOwner() != null)
                .filter(s -> s.getOwner()).findFirst().get();
        if (currentOwner != null) {
            System.out.println("getPerformed_by:::::::::: " + currentOwner.getePerson().getEmail());
            currentOwner.setOwner(false);
            this.getWorkflowProcessEpersonService().update(context, currentOwner);
        }
        initiator = workflowProcess.getWorkflowProcessEpeople().stream()
                .filter(wei -> wei.getUsertype().getPrimaryvalue().equals(WorkFlowUserType.INITIATOR.getAction())).findFirst().get();

        if (initiator != null) {
            System.out.println("next User:::::::::: " + initiator.getePerson().getEmail());
            initiator.setOwner(true);
            this.getWorkflowProcessEpersonService().update(context, currentOwner);
        }
        return currentOwner;
    }

    public WorkflowProcessReferenceDocVersion getcurentVersion(Context context, WorkflowProcess workflowProcess) {
        WorkflowProcessReferenceDocVersion version = null;
        Optional<WorkflowProcessReferenceDoc> workflowProcessReferenceDoc = workflowProcess.getWorkflowProcessReferenceDocs().stream().filter(d -> d != null)
                .filter(d -> d.getDrafttype() != null)
                .filter(d -> d.getDrafttype().getPrimaryvalue() != null)
                .filter(d -> d.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Notesheet"))
                .findFirst();
        if (workflowProcessReferenceDoc.isPresent()) {
            Optional<WorkflowProcessReferenceDocVersion> versionOptional = workflowProcessReferenceDoc.get().getWorkflowProcessReferenceDocVersion().stream().filter(d -> d != null)
                    .filter(d -> d.getIsactive()).findFirst();

            if (versionOptional.isPresent()) {
                version = versionOptional.get();
            }
        }
        return version;
    }

    public WorkflowProcessEperson changeOwnership(Context context, JBPMResponse_ jbpmResponse, WorkflowProcess workflowProcess) throws SQLException, AuthorizeException {
        WorkflowProcessEperson currentOwner = null;
        if (!this.getAction().equalsIgnoreCase("Received")) {
            if (!this.getAction().equalsIgnoreCase("CallBack")) {
                for (WorkflowProcessEperson w : workflowProcess.getWorkflowProcessEpeople()) {
                    System.out.println("All false");
                    w.setSender(false);
                    w.setOwner(false);
                    this.getWorkflowProcessEpersonService().update(context, w);
                }
            }
        }

        if (jbpmResponse.getPerformed_by_user() != null && !jbpmResponse.getPerformed_by_user().isEmpty()) {
            currentOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getID().equals(UUID.fromString(jbpmResponse.getPerformed_by_user()))).findFirst().get();
            if (this.isrefer) {
                System.out.println("getPerformed_by::::::::::::" + currentOwner.getePerson().getEmail());
                currentOwner.setOwner(false);
                currentOwner.setSender(true);
                this.getWorkflowProcessEpersonService().update(context, currentOwner);
                return currentOwner;
            }
            if (currentOwner.getePerson().getEmail() != null) {
                System.out.println("getPerformed_by::::::::::::" + currentOwner.getePerson().getEmail());
            }
            if (this.isbackward) {
                currentOwner.setIssequence(false);
                currentOwner.setOwner(false);
                currentOwner.setSender(true);

            } else {
                currentOwner.setIssequence(true);
                currentOwner.setOwner(false);
                currentOwner.setSender(true);
            }
            if (this.iscallback) {
                System.out.println("in Call Back current owner");
                currentOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getOwner()).findFirst().get();
                currentOwner.setIssequence(false);
                currentOwner.setOwner(false);
                currentOwner.setSender(true);
            }
           /* if(this.action.equalsIgnoreCase("Complete")){
                System.out.println("in Complete Action ");
                currentOwner.setOwner(true);
                currentOwner.setSender(true);
            }*/
            this.getWorkflowProcessEpersonService().update(context, currentOwner);
        }
        if (jbpmResponse.getPerformed_by_group() != null && jbpmResponse.getPerformed_by_group().size() != 0) {
            System.out.println("in   Performed_by_group flow !");
            List<WorkflowProcessEperson> workflowProcessEpersonOwners = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> jbpmResponse.getPerformed_by_group().stream().map(d -> d).anyMatch(d -> d.equals(we.getID().toString()))).collect(Collectors.toList());
            if (workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson() != null).filter(d -> d.getePerson().getID() != null).filter(d -> d.getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())).findFirst().get() != null) {
                currentOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson() != null).filter(d -> d.getePerson().getID() != null).filter(d -> d.getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())).findFirst().get();
            }
            if (this.iscallback) {
                workflowProcess.getWorkflowProcessEpeople().stream().forEach(d -> {
                    System.out.println("next user " + d.getePerson().getEmail());
                    try {
                        d.setOwner(false);
                        d.setSender(true);
                        d.setIssequence(false);
                        this.getWorkflowProcessEpersonService().update(context, d);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (AuthorizeException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            workflowProcessEpersonOwners.stream().forEach(d -> {
                System.out.println("next " + d.getePerson().getEmail());
                try {
                    d.setOwner(false);
                    d.setSender(true);

                    this.getWorkflowProcessEpersonService().update(context, d);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (AuthorizeException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        if (jbpmResponse.getNext_user() != null && jbpmResponse.getNext_user().trim().length() != 0) {
            WorkflowProcessEperson workflowProcessEpersonOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> we.getID().equals(UUID.fromString(jbpmResponse.getNext_user()))).findFirst().get();
            currentOwner = workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getePerson().getID().toString().equalsIgnoreCase(context.getCurrentUser().getID().toString())).findFirst().get();
            if (workflowProcessEpersonOwner.getePerson().getEmail() != null) {
                System.out.println(":::::::getNext_user::::::::::::" + workflowProcessEpersonOwner.getePerson().getEmail());
              /*  try {
                    System.out.println("sent Email to next recipent ");
                   if(workflowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Inward")) {
                       this.getWorkflowProcessEpersonService().sendEmail(context, workflowProcessEpersonOwner.getePerson().getEmail(), workflowProcessEpersonOwner.getePerson().getFullName(), workflowProcess.getSubject());
                   }
                   } catch (Exception e) {
              e.printStackTrace();
                }*/
            }
            workflowProcessEpersonOwner.setOwner(true);
            workflowProcessEpersonOwner.setSender(false);
            workflowProcessEpersonOwner.setIssequence(true);
            this.getWorkflowProcessEpersonService().update(context, workflowProcessEpersonOwner);
        }
        if (jbpmResponse.getNext_group() != null && jbpmResponse.getNext_group().size() != 0) {
            List<WorkflowProcessEperson> workflowProcessEpersonOwners = workflowProcess.getWorkflowProcessEpeople().stream().filter(we -> jbpmResponse.getNext_group().stream().map(d -> d).anyMatch(d -> d.equals(we.getID().toString()))).collect(Collectors.toList());
            workflowProcessEpersonOwners.stream().forEach(d -> {
                if (d.getePerson() != null && d.getePerson().getEmail() != null) {
                    System.out.println("next Group :::" + d.getePerson().getEmail());
                    /*try {
                        System.out.println("sent Email to next recipents ");
                        if(workflowProcess.getWorkflowType().getPrimaryvalue().equalsIgnoreCase("Inward")) {
                            this.getWorkflowProcessEpersonService().sendEmail(context, d.getePerson().getEmail(), d.getePerson().getFullName(), workflowProcess.getSubject());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
                d.setOwner(true);
                d.setSender(false);
                d.setIssequence(true);
                try {
                    this.getWorkflowProcessEpersonService().update(context, d);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (AuthorizeException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return currentOwner;
    }

    public WorkFlowProcessMaster getMaster(Context context) throws SQLException {
        System.out.println("Mastyer::::" + this.getAction());
        return this.getWorkFlowProcessMasterService().findByName(context, this.getAction());
    }

    public void setAction(String action) {
        this.action = action;
    }

    public WorkFlowProcessHistoryService getWorkFlowProcessHistoryService() {
        return workFlowProcessHistoryService;
    }

    public void setWorkFlowProcessHistoryService(WorkFlowProcessHistoryService workFlowProcessHistoryService) {
        this.workFlowProcessHistoryService = workFlowProcessHistoryService;
    }

    public WorkFlowProcessMasterValueService getWorkFlowProcessMasterValueService() {
        return workFlowProcessMasterValueService;
    }

    public void setWorkFlowProcessMasterValueService(WorkFlowProcessMasterValueService workFlowProcessMasterValueService) {
        this.workFlowProcessMasterValueService = workFlowProcessMasterValueService;
    }

    public WorkFlowProcessMasterService getWorkFlowProcessMasterService() {
        return workFlowProcessMasterService;
    }

    public void setWorkFlowProcessMasterService(WorkFlowProcessMasterService workFlowProcessMasterService) {
        this.workFlowProcessMasterService = workFlowProcessMasterService;
    }

    public JbpmServerImpl getJbpmServer() {
        return jbpmServer;
    }

    public void setJbpmServer(JbpmServerImpl jbpmServer) {
        this.jbpmServer = jbpmServer;
    }



    public WorkflowProcessEpersonService getWorkflowProcessEpersonService() {
        return workflowProcessEpersonService;
    }

    public void setWorkflowProcessEpersonService(WorkflowProcessEpersonService workflowProcessEpersonService) {
        this.workflowProcessEpersonService = workflowProcessEpersonService;
    }

    public WorkFlowProcessCommentService getWorkFlowProcessCommentService() {
        return workFlowProcessCommentService;
    }

    public void setWorkFlowProcessCommentService(WorkFlowProcessCommentService workFlowProcessCommentService) {
        this.workFlowProcessCommentService = workFlowProcessCommentService;
    }

    public WorkflowProcessReferenceDocService getWorkflowProcessReferenceDocService() {
        return workflowProcessReferenceDocService;
    }

    public void setWorkflowProcessReferenceDocService(WorkflowProcessReferenceDocService workflowProcessReferenceDocService) {
        this.workflowProcessReferenceDocService = workflowProcessReferenceDocService;
    }
}
