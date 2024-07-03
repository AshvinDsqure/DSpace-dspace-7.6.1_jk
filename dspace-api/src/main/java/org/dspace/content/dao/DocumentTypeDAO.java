/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

/**
 * Database Access Object interface class for the Item object.
 * The implementation of this class is responsible for all database calls for the Item object and is autowired by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 * @author kevinvandevelde at atmire.com
 */
public interface DocumentTypeDAO extends DSpaceObjectLegacySupportDAO<DocumentType> {
    /**
     * Create a new DocumentType, with a new internal ID. Authorization is done
     * inside of this method.
     *
     * @param context       DSpace context object
     * @param documentType  DocumentType
     * @return the newly created item
     * @throws SQLException       if database error
     * @throws AuthorizeException if authorization error
     */
    public DocumentType create(Context context, DocumentType documentType) throws SQLException;

    /**
     * Get All DocumentType
     *
     * @param context DSpace context object
     * @return an iterator over the items in the archive.
     * @throws SQLException if database error
     */
    public List<DocumentType> findAll(Context context) throws SQLException;

    /**
     * get All DocumentType with limit and offset
     *
     * @param context DSpace context object
     * @param limit   limit
     * @param offset  offset
     * @return an iterator over the items in the archive.
     * @throws SQLException if database error
     */
    public List<DocumentType> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    int countRows(Context context) throws SQLException;

}
