/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao;

import org.dspace.content.WorkFlowProcessOutwardDetails;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

public interface WorkFlowProcessOutwardDetailsDAO extends DSpaceObjectLegacySupportDAO<WorkFlowProcessOutwardDetails>{
    int countRows(Context context) throws SQLException;
    WorkFlowProcessOutwardDetails getByOutwardNumber(Context context, String outwardnumber) throws SQLException;
    List<WorkFlowProcessOutwardDetails> searchOutwardNumber(Context context, String name) throws SQLException;

}
