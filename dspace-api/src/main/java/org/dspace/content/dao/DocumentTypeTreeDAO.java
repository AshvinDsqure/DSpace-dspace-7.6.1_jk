/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.DocumentType;
import org.dspace.content.DocumentTypeTree;
import org.dspace.content.Item;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Database Access Object interface class for the Item object.
 * The implementation of this class is responsible for all database calls for the Item object and is autowired by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 * @author kevinvandevelde at atmire.com
 */
public interface DocumentTypeTreeDAO extends DSpaceObjectLegacySupportDAO<DocumentTypeTree> {
    public DocumentTypeTree create(Context context, DocumentTypeTree documentTypeTree) throws SQLException;
    public List<DocumentTypeTree> findAll(Context context) throws SQLException;
    public List<DocumentTypeTree> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public List<DocumentTypeTree> getAllDocumentTypeByItemID(Context context, Item item) throws SQLException;
    public List<DocumentTypeTree> getAllIsSubchildDocumentTypeByItemID(Context context, Item item, DocumentTypeTree parent) throws SQLException;
    public List<DocumentTypeTree> getAllDocumentTypeByItemIDforSubchild(Context context, Item item) throws SQLException;
    public List<DocumentTypeTree> getAllDocumentTypeByItemIDforSubchildwithBitstream(Context context, Item item) throws SQLException;
    public List<DocumentTypeTree> getAllRootTree(Context context) throws SQLException;
    public List<DocumentTypeTree> getChildByNodeID(Context context,DocumentTypeTree documentTypeTreeRoot,Boolean istemplet) throws SQLException;
    public List<DocumentTypeTree> SearchDocumentTreeByDocumentTypeName(Context context,String documentTypeName) throws SQLException;
    public DocumentTypeTree getAllDocumentTypeByBitstreamID(Context context, Bitstream bitstream) throws SQLException;
    public DocumentTypeTree getByPerentidanditemidID(Context context, Item item,DocumentTypeTree perent) throws SQLException;
    public Integer getLastnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException;
    public DocumentTypeTree getRootTreeByName(Context context, String name) throws SQLException;
    public List<DocumentTypeTree> updateDocTypeIndex(Context context, Item item) throws SQLException;
    public List<DocumentTypeTree> SearchDocumentTreeByDocumentTypeNamescanning(Context context,String documenttypename) throws SQLException;
    public Integer getPreviousnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException;
    public Integer getNextnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException;
}
