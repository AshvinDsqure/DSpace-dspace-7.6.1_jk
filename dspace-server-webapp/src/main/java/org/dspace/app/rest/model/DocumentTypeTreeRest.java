/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.DocumentTypeTree;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Transient;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Item REST Resource
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@LinksRest(links = {
        @LinkRest(
                name = DocumentTypeTreeRest.ROOT,
                method = "getAllRootTree"
        ),
        @LinkRest(
                name = DocumentTypeTreeRest.searchDocumentTreeByDocumentTypeName,
                method = "searchDocumentTreeByDocumentTypeName"
        ),
})
public class DocumentTypeTreeRest extends DSpaceObjectRest {
    public static final String NAME = "documenttypetree";
    public static final String PLURAL_NAME = "documenttypetree";
    public static final String CATEGORY = RestAddressableModel.DOCUMENTTYPETREE;
    public static final String CORE = RestAddressableModel.CORE;
    public static final String ROOT = "ROOT";
    public static final String searchDocumentTreeByDocumentTypeName = "searchDocumentTreeByDocumentTypeName";

    private String desc;
    private  String templetName;
    private  Boolean isTemplet;
    private  Boolean isDate;
    private  Boolean isRemark;
    private  Boolean isDescription;
    private  Boolean isSubchild;
    private  Boolean hasSubChild;
    private  Boolean nonrepetitive;
    private  Boolean isRootOfmaster;
    private  Boolean isDisplay=false;
    private Boolean isforScanning;
    private Date doc_date = new Date();
    private int indexUpdateAction=0; //1 for up //2 for down
    DocumentTypeRest documentType;
    @Transient
    DocumentTypeTreeRest parent;
    @Transient
    PDOutlineItem bookmark = new PDOutlineItem();
    UUID parentuuid;
    private  Boolean hasChildren =false;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    List<DocumentTypeTreeRest> children=new ArrayList<>();
    ItemRest item;
    BitstreamRest bitstream;
    DocumentTypeTreeRest templetTree;
    private  String remarkdesc;
    private Integer index;


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String entityType = null;

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return NAME;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getDoc_date() {
        return doc_date;
    }

    public void setDoc_date(Date doc_date) {
        this.doc_date = doc_date;
    }

    public List<DocumentTypeTreeRest> getChildren() {
        return children;
    }
    public void setToFirst(DocumentTypeTreeRest children) {
        if(this.getNonrepetitive() != null && this.getNonrepetitive()){
            this.setBitstream(children.getBitstream());
            this.setDesc(children.getDocumentType().getDocumenttypename());
            this.setIsTemplet(children.getIsTemplet());
            this.setIsSubchild(children.getIsSubchild());

        }else {
            LinkedList<DocumentTypeTreeRest> sortChild = new LinkedList<DocumentTypeTreeRest>(this.getChildren());
            sortChild.addFirst(children);
            List<DocumentTypeTreeRest> aList = new ArrayList<DocumentTypeTreeRest>(sortChild);
            this.children = aList;
        }

    }
    public void setChildren(DocumentTypeTreeRest children) {
        this.children.add(children);
       /* LinkedList<DocumentTypeTreeRest> sortChild= new LinkedList<DocumentTypeTreeRest>(this.children);
        sortChild.addFirst(children);
        List<DocumentTypeTreeRest> aList = new ArrayList<DocumentTypeTreeRest>(this.children);
        this.children=aList;*/

    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public DocumentTypeTreeRest getParent() {
        return parent;
    }

    public void setParent(DocumentTypeTreeRest parent) {
        this.parent = parent;
    }

    public DocumentTypeRest getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentTypeRest documentType) {
        this.documentType = documentType;
    }
    public String getTempletName() {
        return templetName;
    }

    public void setTempletName(String templetName) {
        this.templetName = templetName;
    }

    public Boolean getIsTemplet() {
        return isTemplet;
    }

    public void setIsTemplet(Boolean isTemplet) {
        this.isTemplet = isTemplet;
    }

    public Boolean getIsDate() {
        return isDate;
    }

    public void setIsDate(Boolean isDate) {
        this.isDate = isDate;
    }

    public Boolean getIsRemark() {
        return isRemark;
    }

    public void setIsRemark(Boolean isRemark) {
        this.isRemark = isRemark;
    }

    public Boolean getIsDescription() {
        return this.isDescription;
    }

    public void setIsDescription(Boolean isDescription) {
        this.isDescription = isDescription;
    }

    public Boolean getRootOfmaster() {
        return isRootOfmaster;
    }

    public void setRootOfmaster(Boolean rootOfmaster) {
        isRootOfmaster = rootOfmaster;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public ItemRest getItem() {
        return item;
    }

    public void setItem(ItemRest item) {
        this.item = item;
    }

    public BitstreamRest getBitstream() {
        return bitstream;
    }

    public void setBitstream(BitstreamRest bitstream) {
        this.bitstream = bitstream;
    }

    public DocumentTypeTreeRest getTempletTree() {
        return templetTree;
    }

    public void setTempletTree(DocumentTypeTreeRest templetTree) {
        this.templetTree = templetTree;
    }

    public String getRemarkdesc() {
        return remarkdesc;
    }

    public void setRemarkdesc(String remarkdesc) {
        this.remarkdesc = remarkdesc;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public UUID getParentuuid() {
        return parentuuid;
    }

    public void setParentuuid(UUID parentuuid) {
        this.parentuuid = parentuuid;
    }

    public Boolean getDisplay() {
        return isDisplay;
    }

    public void setDisplay(Boolean display) {
        isDisplay = display;
    }

    public Boolean getIsSubchild() {
        return isSubchild;
    }

    public void setIsSubchild(Boolean isSubchild) {
        this.isSubchild = isSubchild;
    }

    public Boolean getHasSubChild() {
        return hasSubChild;
    }

    public void setHasSubChild(Boolean hasSubChild) {
        this.hasSubChild = hasSubChild;
    }

    public Boolean getNonrepetitive() {
        return nonrepetitive;
    }

    public void setNonrepetitive(Boolean nonrepetitive) {
        this.nonrepetitive = nonrepetitive;
    }

    public void addTitle(Context context,List<PDDocument> podocuments,BitstreamService bitstreamService) throws IOException, SQLException, AuthorizeException {
        String desc="";
        if(!this.getIsTemplet()) {
            if(this.getIsSubchild() && this.getDesc() != null){
                desc=this.getDesc();
            }else if(this.getIsSubchild() && this.getDesc() == null){
                desc=this.getDocumentType().getDocumenttypename();
            }else{
                String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(this.getDoc_date());
                String date =desc = this.getDesc() != null ? this.getDesc() : formattedDate;
            }

        }
        else
         desc=this.getDocumentType().getDocumenttypename();
        this.bookmark.setTitle(desc);
        if(this.getBitstream() != null){
            Bitstream bitstream = bitstreamService.find(context, UUID.fromString(this.getBitstream().getId()));
            //bitstreams.add(childDocTypeTreeRest.getBitstream());
            InputStream is = bitstreamService.retrieve(context, bitstream);
            PDDocument document = PDDocument.load(is);
            PDPageFitWidthDestination dest = new PDPageFitWidthDestination();
            Iterator<PDPage> pDPages = document.getPages().iterator();
            podocuments.add(document);
            while (pDPages.hasNext()) {
                PDPage p = pDPages.next();
                dest.setPage(p);
                break;
            }
            this.bookmark.setDestination(dest);

        }
    }

    public Boolean getIsforScanning() {
        return isforScanning;
    }

    public void setIsforScanning(Boolean isforScanning) {
        this.isforScanning = isforScanning;
    }

    public void addtoChildBookmarkPDOutlineItem(PDOutlineItem childBookmark){
        this.bookmark.addLast(childBookmark);
    }
    @Transient
    public PDOutlineItem retuenChildbookmark() {
        return bookmark;
    }

    public int getIndexUpdateAction() {
        return indexUpdateAction;
    }

    public void setIndexUpdateAction(int indexUpdateAction) {
        this.indexUpdateAction = indexUpdateAction;
    }
}

