/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.WorkflowProcessEperson;
import org.dspace.content.dao.WorkflowProcessEpersonDAO;
import org.dspace.core.AbstractHibernateDSODAO;

/**
 * Hibernate implementation of the Database Access Object interface class for the Item object.
 * This class is responsible for all database calls for the Item object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class WorkflowProcessEpersonDAOImpl extends AbstractHibernateDSODAO<WorkflowProcessEperson> implements WorkflowProcessEpersonDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkflowProcessDefinitionDAOImpl.class);
    protected WorkflowProcessEpersonDAOImpl() {
        super();
    }
}
