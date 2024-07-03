/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.DocumentType;
import org.dspace.content.DocumentTypeTree;
import org.dspace.content.Item;
import org.dspace.content.dao.DocumentTypeDAO;
import org.dspace.content.dao.DocumentTypeTreeDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Hibernate implementation of the Database Access Object interface class for the Item object.
 * This class is responsible for all database calls for the Item object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class DocumentTypeTreeDAOImpl extends AbstractHibernateDSODAO<DocumentTypeTree> implements DocumentTypeTreeDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(DocumentTypeTreeDAOImpl.class);
    protected DocumentTypeTreeDAOImpl() {
        super();
    }



    @Override
    public List<DocumentTypeTree> findAll(Context context) throws SQLException {
        Query query = createQuery(context, "FROM DocumentTypeTree ORDER BY id");
        return query.getResultList();
    }
    @Override
    public List<DocumentTypeTree> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        Query query = createQuery(context, "FROM DocumentTypeTree ORDER BY id");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }
    @Override
    public List<DocumentTypeTree> getAllDocumentTypeByItemID(Context context, Item item) throws SQLException{
        // Query query = createQuery(context, "FROM DocumentTypeTree as d WHERE d.item=:item and isSubchild=false and bitstream_id IS NOT NULL ORDER BY d.isSubchild,d.doc_date ASC");
        Query query = createQuery(context, "select d  FROM DocumentTypeTree as d WHERE d.item=:item and isSubchild=false  and bitstream_id IS NOT NULL ORDER BY d.doc_date desc");
        Query query2 = createQuery(context, "select d  FROM DocumentTypeTree as d join d.templet as  dt WHERE d.item=:item and d.isSubchild=true  and d.bitstream_id IS NOT NULL ORDER BY d.parent.doc_date desc, dt.index");
         query.setParameter("item", item);
        query2.setParameter("item", item);
        List<DocumentTypeTree> documentTypeTreess=list(query);
        documentTypeTreess.addAll(list(query2));
        return documentTypeTreess;
    }
    public List<DocumentTypeTree> getAllDocumentTypeByItemID_old(Context context, Item item) throws SQLException{
        // Query query = createQuery(context, "FROM DocumentTypeTree as d WHERE d.item=:item and isSubchild=false and bitstream_id IS NOT NULL ORDER BY d.isSubchild,d.doc_date ASC");
        Query query = createQuery(context, "FROM DocumentTypeTree as d WHERE d.item=:item and hasSubChild=false  and bitstream_id IS NOT NULL ORDER BY d.doc_date desc");
        query.setParameter("item", item);
        return query.getResultList();
    }
    @Override
    public List<DocumentTypeTree> getAllDocumentTypeByItemIDforSubchild(Context context, Item item) throws SQLException{
        Query query = createQuery(context, "FROM DocumentTypeTree as d WHERE d.item=:item and  isSubchild=true and bitstream_id IS  NULL ORDER BY d.isSubchild,d.doc_date ASC ");
        query.setParameter("item", item);
        return query.getResultList();
    }
    @Override
    public List<DocumentTypeTree> getAllDocumentTypeByItemIDforSubchildwithBitstream(Context context, Item item) throws SQLException{
        Query query = createQuery(context, "FROM DocumentTypeTree as d WHERE d.item=:item and  isSubchild=true and bitstream_id IS NOT NULL ORDER BY d.isSubchild,d.index DESC");
        query.setParameter("item", item);
        return query.getResultList();
    }

    @Override
    public List<DocumentTypeTree> getAllIsSubchildDocumentTypeByItemID(Context context, Item item, DocumentTypeTree parent) throws SQLException {
        Query query = createQuery(context, "FROM DocumentTypeTree as d WHERE d.item=:item and isSubchild=true and d.parent=:parent  ORDER BY d.index");
        query.setParameter("item", item);
        query.setParameter("parent", parent);
        return query.getResultList();
    }

    @Override
    public DocumentTypeTree getAllDocumentTypeByBitstreamID(Context context, Bitstream bitstream) throws SQLException{
        Query query = createQuery(context, " select d FROM DocumentTypeTree as d WHERE d.bitstream=:bitstream ORDER BY d.id");
        query.setParameter("bitstream", bitstream);
        return (DocumentTypeTree)query.getSingleResult();
    }

    @Override
    public DocumentTypeTree getByPerentidanditemidID(Context context, Item item,DocumentTypeTree parent) throws SQLException {
        Query query = createQuery(context, " select d FROM DocumentTypeTree as d WHERE  d.item=:item and d.parent=:parent ORDER BY d.id");
        query.setParameter("item", item);
        query.setParameter("parent", parent);
        return (DocumentTypeTree)query.getSingleResult();
    }

    @Override
    public Integer getLastnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("FROM DocumentTypeTree as d WHERE  ");
        if(documentTypeTreeRoot != null){
            queryStr.append(" d.parent=:parent and d.index=(select max(d.index) FROM DocumentTypeTree as d WHERE  d.parent=:parent ) ");
        }else{
            queryStr.append("d.isRootOfmaster=:isRootOfmaster and d.index=(select max(d.index) FROM DocumentTypeTree as d WHERE d.isRootOfmaster=:isRootOfmaster) ");
        }
        System.out.println("queryStr:::::"+queryStr);
        Query query = createQuery(context, queryStr.toString());
        if(documentTypeTreeRoot != null){
            query.setParameter("parent", documentTypeTreeRoot);
        }else {
            query.setParameter("isRootOfmaster", true);
        }
        int LastIndex=-1;
        try {
            DocumentTypeTree documentTypeTree=(DocumentTypeTree) query.getSingleResult();
            LastIndex= documentTypeTree.getIndex();
        }catch (Exception e){
            e.printStackTrace();
            return  LastIndex;
        }
        return LastIndex;
    }
    @Override
    public Integer getNextnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("FROM DocumentTypeTree i WHERE i.index = ( SELECT MIN(index) FROM DocumentTypeTree WHERE index > :currentIndex and  i.parent=:parent and i.istemplet=false )");
        System.out.println("Next queryStr:::::"+queryStr);
        Query query = createQuery(context, queryStr.toString());
        query.setParameter("parent", documentTypeTreeRoot.getParent());
        query.setParameter("currentIndex", documentTypeTreeRoot.getIndex());
        int LastIndex=-1;
        try {
            DocumentTypeTree documentTypeTree=(DocumentTypeTree) query.getSingleResult();
            LastIndex= documentTypeTree.getIndex();
        }catch (Exception e){
            e.printStackTrace();
        }
        return LastIndex;
    }
    @Override
    public Integer getPreviousnodeIndexByParent(Context context, DocumentTypeTree documentTypeTreeRoot) throws SQLException {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("FROM DocumentTypeTree i WHERE i.index = ( SELECT MAX(index) FROM DocumentTypeTree WHERE index < :currentIndex and  i.parent=:parent and i.istemplet=false )");
        System.out.println("getPreviousnodeIndexByParent queryStr:::::"+queryStr);
        Query query = createQuery(context, queryStr.toString());
        query.setParameter("parent", documentTypeTreeRoot.getParent());
        query.setParameter("currentIndex", documentTypeTreeRoot.getIndex());
        int LastIndex=-1;
        try {
            DocumentTypeTree documentTypeTree=(DocumentTypeTree) query.getSingleResult();
            LastIndex= documentTypeTree.getIndex();
        }catch (Exception e){
            e.printStackTrace();
        }
        return LastIndex;
    }

    @Override
    public DocumentTypeTree getRootTreeByName(Context context, String name) throws SQLException {
        Query query = createQuery(context,"FROM DocumentTypeTree as d WHERE  d.isRootOfmaster=:isRootOfmaster  and  d.documentType.documenttypename=:documenttypename");
        query.setParameter("isRootOfmaster", true);
        query.setParameter("documenttypename", name);
        return (DocumentTypeTree)query.getSingleResult();
    }
    @Override
    public List<DocumentTypeTree> updateDocTypeIndex(Context context, Item item) throws SQLException{
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("FROM DocumentTypeTree as d WHERE  d.bitstream IS NOT NULL and d.parent.isSubchild=true and d.istemplet=false");
        if (item != null) {
            queryStr.append(" and d.item=:item");
        }
        Query query = createQuery(context, queryStr.toString());
        if (item != null)
            query.setParameter("item", item);
        return query.getResultList();
    }
    @Override
    public List<DocumentTypeTree> getAllRootTree(Context context) throws SQLException {
        Query query = createQuery(context, "FROM DocumentTypeTree as d WHERE d.isRootOfmaster=:isRootOfmaster ORDER BY d.index");
        query.setParameter("isRootOfmaster", true);
        return query.getResultList();
    }

    @Override
    public List<DocumentTypeTree> getChildByNodeID(Context context, DocumentTypeTree documentTypeTreeParent,Boolean istemplet) throws SQLException {
        Query query = createQuery(context, "select d from DocumentTypeTree as d   WHERE   d.istemplet=:istemplet AND d.parent=:parent   ORDER BY d.index");
        System.out.println("DocumentTypeTreeDAOImpl:::::" + query);
        query.setParameter("parent", documentTypeTreeParent);
        query.setParameter("istemplet", istemplet);
        return   list(query);
    }

    @Override
    public List<DocumentTypeTree> SearchDocumentTreeByDocumentTypeName(Context context,String documenttypename) throws SQLException{
        try {
            Query query = createQuery(context, "select d from DocumentTypeTree as d  join d.documentType as  dt WHERE   d.istemplet=:istemplet AND  lower(dt.documenttypename) LIKE :documenttypename  ORDER BY d.id");
            System.out.println("DocumentTypeTreeDAOImpl:::::" + query);
            query.setParameter("istemplet", true);
            query.setParameter("documenttypename", documenttypename.toLowerCase() + "%");
          return   list(query);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public List<DocumentTypeTree> SearchDocumentTreeByDocumentTypeNamescanning(Context context,String documenttypename) throws SQLException{
        try {
            Query query = createQuery(context, "select d from DocumentTypeTree as d  join d.documentType as  dt WHERE   d.istemplet=:istemplet AND  lower(dt.documenttypename) LIKE :documenttypename  ORDER BY d.id");
            System.out.println("DocumentTypeTreeDAOImpl:::::" + query);
            query.setParameter("istemplet", true);
            //query.setParameter("isforScanning", true);
            query.setParameter("documenttypename", documenttypename.toLowerCase() + "%");
            return   list(query);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
