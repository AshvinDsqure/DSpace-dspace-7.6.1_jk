/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content;

import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.WorkFlowProcessMasterDAO;
import org.dspace.content.service.WorkFlowProcessMasterService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WorkFlowProcessMasterServiceImpl extends DSpaceObjectServiceImpl<WorkFlowProcessMaster> implements WorkFlowProcessMasterService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);
    @Autowired(required = true)
    protected WorkFlowProcessMasterDAO workFlowProcessMasterDAO;
    protected WorkFlowProcessMasterServiceImpl() {
        super();
    }
    @Override
    public WorkFlowProcessMaster findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }
    @Override
    public WorkFlowProcessMaster findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }
    @Override
    public void updateLastModified(Context context, WorkFlowProcessMaster dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }
    @Override
    public void delete(Context context, WorkFlowProcessMaster dso) throws SQLException, AuthorizeException, IOException {
        workFlowProcessMasterDAO.delete(context,dso);
    }
    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public List<WorkFlowProcessMaster> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workFlowProcessMasterDAO.findAll(context,WorkFlowProcessMaster.class)).orElse(new ArrayList<>());

    }
    @Override
    public WorkFlowProcessMaster create(Context context, WorkFlowProcessMaster workFlowProcessMaster) throws SQLException, AuthorizeException {
        workFlowProcessMaster= workFlowProcessMasterDAO.create(context,workFlowProcessMaster);
        return workFlowProcessMaster;
    }
    @Override
    public List<WorkFlowProcessMaster> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workFlowProcessMasterDAO.findAll(context,WorkFlowProcessMaster.class,limit,
                offset)).orElse(new ArrayList<>());
    }
    @Override
    public int countRows(Context context) throws SQLException {
        return workFlowProcessMasterDAO.countRows(context);
    }

    @Override
    public WorkFlowProcessMaster findByName(Context context, String name)  throws SQLException {
        return workFlowProcessMasterDAO.findByName(context,name);
    }

    @Override
    public WorkFlowProcessMaster find(Context context, UUID uuid) throws SQLException {
        return workFlowProcessMasterDAO.findByID(context,WorkFlowProcessMaster.class,uuid);
    }
    @Override
    public void update(Context context, WorkFlowProcessMaster workFlowProcessMaster) throws SQLException, AuthorizeException {
        this.workFlowProcessMasterDAO.save(context, workFlowProcessMaster);
    }
}