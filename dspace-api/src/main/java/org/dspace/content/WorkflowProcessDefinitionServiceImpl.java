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
import org.dspace.content.dao.WorkflowProcessDefinitionDAO;
import org.dspace.content.service.WorkflowProcessDefinitionService;
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

/**
 * Service implementation for the Item object.
 * This class is responsible for all business logic calls for the Item object and is autowired by spring.
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class WorkflowProcessDefinitionServiceImpl extends DSpaceObjectServiceImpl<WorkflowProcessDefinition> implements WorkflowProcessDefinitionService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);

    @Autowired(required = true)
    protected WorkflowProcessDefinitionDAO workflowProcessDefinitionDAO;

    protected WorkflowProcessDefinitionServiceImpl() {
        super();
    }

    @Override
    public WorkflowProcessDefinition findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcessDefinition findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }

    @Override
    public WorkflowProcessDefinition find(Context context, UUID uuid) throws SQLException {
        return workflowProcessDefinitionDAO.findByID(context,WorkflowProcessDefinition.class,uuid);

    }

    @Override
    public void updateLastModified(Context context, WorkflowProcessDefinition dso) throws SQLException, AuthorizeException {
        update(context, dso);
        //Also fire a modified event since the item HAS been modified
        context.addEvent(new Event(Event.MODIFY, Constants.ITEM, dso.getID(), null, getIdentifiers(context, dso)));
    }

    @Override
    public void delete(Context context, WorkflowProcessDefinition dso) throws SQLException, AuthorizeException, IOException {

    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public WorkflowProcessDefinition create(Context context, WorkflowProcessDefinition workflowProcessDefinition) throws SQLException, AuthorizeException {
        workflowProcessDefinition= workflowProcessDefinitionDAO.create(context,workflowProcessDefinition);
        return workflowProcessDefinition;
    }

    @Override
    public List<WorkflowProcessDefinition> findAll(Context context) throws SQLException {
        return Optional.ofNullable(workflowProcessDefinitionDAO.findAll(context,WorkflowProcessDefinition.class)).orElse(new ArrayList<>());
    }

    @Override
    public List<WorkflowProcessDefinition> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return  Optional.ofNullable(workflowProcessDefinitionDAO.findAll(context,WorkflowProcessDefinition.class,limit,
                offset)).orElse(new ArrayList<>());
    }
}
