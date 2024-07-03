/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.DocumentType;
import org.dspace.content.DocumentTypeTree;
import org.dspace.content.Item;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface class for the Item object.
 * The implementation of this class is responsible for all business logic calls for the Item object and is autowired
 * by spring
 *
 * @author kevinvandevelde at atmire.com
 */
public interface DocumentTypeTreeService extends DSpaceObjectService<DocumentTypeTree>, DSpaceObjectLegacySupportService<DocumentTypeTree> {
    public DocumentTypeTree create(Context context, DocumentTypeTree documentTypeTree) throws SQLException, AuthorizeException;
    public DocumentTypeTree create(Context context) throws SQLException, AuthorizeException;
    public List<DocumentTypeTree> findAll(Context context) throws SQLException;
    public List<DocumentTypeTree> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public List<DocumentTypeTree> getAllDocumentTypeByItemID(Context context, Item item) throws SQLException;
    public List<DocumentTypeTree> getAllDocumentTypeByItemIDforSubchild(Context context, Item item) throws SQLException;
    public List<DocumentTypeTree> getAllIsSubchildDocumentTypeByItemID(Context context, Item item, DocumentTypeTree parent) throws SQLException;
    public List<DocumentTypeTree> getAllDocumentTypeByItemIDforSubchildwithBitstream(Context context, Item item) throws SQLException;
    public List<DocumentTypeTree> getAllRootTree(Context context) throws SQLException;
    public DocumentTypeTree getRootTreeByName(Context context,String name) throws SQLException;
    public List<DocumentTypeTree> SearchDocumentTreeByDocumentTypeName(Context context,String documentTypeName) throws SQLException;
    public DocumentTypeTree getAllDocumentTypeByBitstreamID(Context context, Bitstream bitstream) throws SQLException;
    public List<DocumentTypeTree> getChildByNodeID(Context context, DocumentTypeTree documentTypeTreeParent,Boolean istemplet) throws SQLException;
    public Integer getLastnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException;
    public DocumentTypeTree getByPerentidanditemidID(Context context, Item item,DocumentTypeTree perent) throws SQLException;
    public List<DocumentTypeTree> transformDocumentTypes(Context context, Item item) throws SQLException;
    public List<DocumentTypeTree> transformDocumentTypesFromChildToparent(Context context, Item item) throws SQLException;
    public List<DocumentTypeTree> updateDocTypeIndex(Context context, Item item) throws SQLException;
    public List<DocumentTypeTree> SearchDocumentTreeByDocumentTypeNamescanning(Context context,String documenttypename) throws SQLException;
    public Integer getPreviousnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException;
    public Integer getNextnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException;
}
