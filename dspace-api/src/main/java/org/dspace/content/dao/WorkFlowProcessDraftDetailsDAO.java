/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao;

import org.dspace.content.WorkFlowProcessDraftDetails;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.UUID;

public interface WorkFlowProcessDraftDetailsDAO extends DSpaceObjectLegacySupportDAO<WorkFlowProcessDraftDetails>{
    int countRows(Context context) throws SQLException;

    WorkFlowProcessDraftDetails getbyDocumentsignator(Context context, UUID workflowprocessid) throws SQLException;

}
