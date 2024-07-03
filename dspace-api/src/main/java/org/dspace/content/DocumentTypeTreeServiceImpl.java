/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.DocumentTypeDAO;
import org.dspace.content.dao.DocumentTypeTreeDAO;
import org.dspace.content.service.DocumentTypeService;
import org.dspace.content.service.DocumentTypeTreeService;
import org.dspace.core.Context;
import org.dspace.core.LogHelper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.print.Doc;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for the Item object.
 * This class is responsible for all business logic calls for the Item object and is autowired by spring.
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class DocumentTypeTreeServiceImpl extends DSpaceObjectServiceImpl<DocumentTypeTree> implements DocumentTypeTreeService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);

    @Autowired(required = true)
    protected DocumentTypeTreeDAO documentTypeTreeDAO;
    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    protected DocumentTypeTreeServiceImpl() {
        super();
    }


    @Override
    public DocumentTypeTree findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public DocumentTypeTree findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }

    @Override
    public DocumentTypeTree find(Context context, UUID uuid) throws SQLException {
        return documentTypeTreeDAO.findByID(context, DocumentTypeTree.class, uuid);
    }

    @Override
    public void updateLastModified(Context context, DocumentTypeTree dso) throws SQLException, AuthorizeException {

    }

    @Override
    public void delete(Context context, DocumentTypeTree dso) throws SQLException, AuthorizeException, IOException {
        documentTypeTreeDAO.delete(context, dso);
    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public DocumentTypeTree create(Context context, DocumentTypeTree documentTypeTree) throws SQLException, AuthorizeException {
        return documentTypeTreeDAO.create(context, documentTypeTree);
    }

    @Override
    public DocumentTypeTree create(Context context) throws SQLException, AuthorizeException {
        if (!this.authorizeService.isAdmin(context)) {
            throw new AuthorizeException("You must be an admin to create an EPerson");
        } else {
            DocumentTypeTree e = (DocumentTypeTree) this.documentTypeTreeDAO.create(context, new DocumentTypeTree());
            this.log.info(LogHelper.getHeader(context, "create_DocumentType", "eperson_id=" + e.getID()));
            //context.addEvent(new Event(1, 7, e.getID(), (String)null, this.getIdentifiers(context, e)));
            return e;
        }
    }

    @Override
    public List<DocumentTypeTree> findAll(Context context) throws SQLException {
        return documentTypeTreeDAO.findAll(context);
    }

    @Override
    public List<DocumentTypeTree> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return documentTypeTreeDAO.findAll(context, limit, offset);
    }

    @Override
    public List<DocumentTypeTree> getAllDocumentTypeByItemID(Context context, Item item) throws SQLException {
        return documentTypeTreeDAO.getAllDocumentTypeByItemID(context, item);
    }

    @Override
    public List<DocumentTypeTree> getAllDocumentTypeByItemIDforSubchild(Context context, Item item) throws SQLException {
        return documentTypeTreeDAO.getAllDocumentTypeByItemIDforSubchild(context, item);
    }

    @Override
    public List<DocumentTypeTree> getAllIsSubchildDocumentTypeByItemID(Context context, Item item, DocumentTypeTree parent) throws SQLException {
        return documentTypeTreeDAO.getAllIsSubchildDocumentTypeByItemID(context, item, parent);
    }

    @Override
    public List<DocumentTypeTree> getAllDocumentTypeByItemIDforSubchildwithBitstream(Context context, Item item) throws SQLException {
        return documentTypeTreeDAO.getAllDocumentTypeByItemIDforSubchildwithBitstream(context, item);
    }

    @Override
    public DocumentTypeTree getAllDocumentTypeByBitstreamID(Context context, Bitstream bitstream) throws SQLException {
        return documentTypeTreeDAO.getAllDocumentTypeByBitstreamID(context, bitstream);
    }

    @Override
    public List<DocumentTypeTree> getChildByNodeID(Context context, DocumentTypeTree documentTypeTreeParent, Boolean istemplet) throws SQLException {
        return documentTypeTreeDAO.getChildByNodeID(context, documentTypeTreeParent, istemplet);
    }

    @Override
    public Integer getLastnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException {
        return documentTypeTreeDAO.getLastnodeIndexByParent(context, documentTypeTreeRoot);
    }

    @Override
    public DocumentTypeTree getByPerentidanditemidID(Context context, Item item, DocumentTypeTree perent) throws SQLException {
        return documentTypeTreeDAO.getByPerentidanditemidID(context,item,perent);
    }

    @Override
    public List<DocumentTypeTree> transformDocumentTypes(Context context, Item item) throws SQLException {
        List<DocumentTypeTree> documentTypeTreesItem = new ArrayList<>();
        List<DocumentTypeTree> documentTypeTreessuchild = this.getAllDocumentTypeByItemIDforSubchild(context, item);
        List<DocumentTypeTree> documentTypeTreesItemnonsubchild = this.getAllDocumentTypeByItemIDforSubchildwithBitstream(context, item);
        List<DocumentTypeTree> documentTypeTreesItemnonsubchildwithbitstream = this.getAllDocumentTypeByItemID(context, item);
        documentTypeTreesItem.addAll(documentTypeTreessuchild);
        documentTypeTreesItem.addAll(documentTypeTreesItemnonsubchild);
        documentTypeTreesItem.addAll(documentTypeTreesItemnonsubchildwithbitstream);
        if (documentTypeTreesItem != null && documentTypeTreesItem.size() != 0) {
            List<DocumentTypeTree> rootdocumentTypeTrees = this.getAllRootTree(context);
            documentTypeTreesItem.forEach(rsd -> {
                rootdocumentTypeTrees.forEach(d -> d.matchAndAddChild(rsd));
            });
            return rootdocumentTypeTrees.stream().filter(d -> d.getDisplay()).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }

    }

    public List<DocumentTypeTree> transformDocumentTypesFromChildToparent_old(Context context, Item item) throws SQLException {
        List<DocumentTypeTree> documentTypeTreesItem = new ArrayList<>();
        List<DocumentTypeTree> documentTypeTreessuchild = this.getAllDocumentTypeByItemID(context, item);
        return this.transformParentNodeFromChildNode(documentTypeTreessuchild, documentTypeTreesItem, new ArrayList<DocumentTypeTree>());

    }

    @Override
    public List<DocumentTypeTree> transformDocumentTypesFromChildToparent(Context context, Item item) throws SQLException {
        List<DocumentTypeTree> documentTypeTreesItem = new ArrayList<>();
        List<DocumentTypeTree> documentTypeTreessuchild = this.getAllDocumentTypeByItemID(context, item);
        return this.transformParentNodeFromChildNode(documentTypeTreessuchild, documentTypeTreesItem, new ArrayList<DocumentTypeTree>());

    }

    @Override
    public List<DocumentTypeTree> updateDocTypeIndex(Context context, Item item) throws SQLException {
        return documentTypeTreeDAO.updateDocTypeIndex(context, item);
    }

    public List<DocumentTypeTree> transformParentNodeFromChildNode(List<DocumentTypeTree> childNode, List<DocumentTypeTree> parentNode, List<DocumentTypeTree> finalparentnode) {
      //  List<DocumentTypeTree>  dummy=  childNode.stream().peek(d->System.out.println("class"+d.getClass())).filter(d -> d.getParent() == null).collect(Collectors.toList());
        finalparentnode.addAll(childNode.stream().filter(d -> d.getParent() == null).collect(Collectors.toList()));
        Map<DocumentTypeTree, List<DocumentTypeTree>> result1 = childNode.stream().filter(d -> d.getParent() != null).collect(Collectors.groupingBy(DocumentTypeTree::getParent));
        if (result1 != null && !result1.isEmpty()) {
            parentNode.clear();
            result1.forEach((parent, childs) -> {
                childs.forEach(c -> parent.addTocloneNode(c));
                if (!parentNode.contains(parent)) {
                    parentNode.add(parent);
                }
            });
            this.transformParentNodeFromChildNode(parentNode, parentNode, finalparentnode);
        } else {
            finalparentnode.forEach(d -> {
                if (!parentNode.contains(d))
                    parentNode.add(d);
            });
        }
       Collections.sort(parentNode, new Comparator<DocumentTypeTree>() {
            @Override
            public int compare(DocumentTypeTree abc1, DocumentTypeTree abc2) {
                return Integer.compare(abc1.getIndex(), abc2.getIndex());
            }
        });
        return parentNode;
    }

    @Override
    public List<DocumentTypeTree> getAllRootTree(Context context) throws SQLException {
        return documentTypeTreeDAO.getAllRootTree(context);
    }

    @Override
    public DocumentTypeTree getRootTreeByName(Context context, String name) throws SQLException {
        return documentTypeTreeDAO.getRootTreeByName(context, name);
    }

    @Override
    public List<DocumentTypeTree> SearchDocumentTreeByDocumentTypeName(Context context, String documentTypeName) throws SQLException {
        return documentTypeTreeDAO.SearchDocumentTreeByDocumentTypeName(context, documentTypeName);
    }

    @Override
    public void update(Context context, DocumentTypeTree documentTypedoTypeTree) throws SQLException, AuthorizeException {
        log.info(LogHelper.getHeader(context, "update_bundle", "bundle_id=" + documentTypedoTypeTree.getID()));
        super.update(context, documentTypedoTypeTree);
        this.documentTypeTreeDAO.save(context, documentTypedoTypeTree);
    }
    @Override
    public List<DocumentTypeTree> SearchDocumentTreeByDocumentTypeNamescanning(Context context,String documenttypename) throws SQLException{
       return this.documentTypeTreeDAO.SearchDocumentTreeByDocumentTypeNamescanning(context, documenttypename);
    }

    @Override
    public Integer getPreviousnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException {
        return this.documentTypeTreeDAO.getPreviousnodeIndexByParent(context, documentTypeTreeRoot);
    }

    @Override
    public Integer getNextnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException {
        return this.documentTypeTreeDAO.getNextnodeIndexByParent(context, documentTypeTreeRoot);
    }
}
