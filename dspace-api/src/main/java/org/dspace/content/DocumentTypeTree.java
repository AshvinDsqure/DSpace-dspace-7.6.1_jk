/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.*;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class representing an item in DSpace.
 * <P>
 * This class holds in memory the item Dublin Core metadata, the bundles in the
 * item, and the bitstreams in those bundles. When modifying the item, if you
 * modify the Dublin Core or the "in archive" flag, you must call
 * <code>update</code> for the changes to be written to the database.
 * Creating, adding or removing bundles or bitstreams has immediate effect in
 * the database.
 *
 * @author Robert Tansley
 * @author Martin Hald
 */
@Entity
@Table(name = "item2documentype")
public class DocumentTypeTree extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "documenttype_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "description")
    private String desc;
    @Column(name = "templetname")
    private String templetName;
    @Column(name = "istemplet")
    private Boolean istemplet;
    @Column(name = "isdate")
    private Boolean isDate;
    @Column(name = "isremark")
    private Boolean isRemark;
    @Column(name = "issubchild")
    private Boolean isSubchild;
    @Column(name = "nonrepetitive")
    private Boolean nonrepetitive;
    @Column(name = "hassubchild")
    private Boolean hasSubChild;
    @Column(name = "isdescription")
    private Boolean isDescription;
    @Column(name = "isrootofmaster")
    private Boolean isRootOfmaster;
    @Column(name = "isforscanning")
    private Boolean isforScanning;
    @Column(name = "doc_date", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date doc_date;
    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "parent_uuid")
    private DocumentTypeTree parent;
    @Where(clause = "isSubchild = true")
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "parent")
    @OrderBy("index")
    private Set<DocumentTypeTree> subchildEnablechildren;
    @Where(clause = "istemplet = true")
    //@Where(clause = "istemplet = true and isRemark = false")
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "parent")
    @OrderBy("index")
    private Set<DocumentTypeTree> children=new HashSet<>();
    /*@Where(clause = "istemplet = true and isRemark = true")
    @Where(clause = "istemplet = true ")
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "parent")
    @OrderBy("index")
    private Set<DocumentTypeTree> nonSearchablechildren=new HashSet<>();*/
    @Where(clause = "istemplet = false")
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "parent")
    @OrderBy("index")
    private Set<DocumentTypeTree> NonTempletChildren;
    @OneToOne(cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "documenttype_ref_id")
    private  DocumentType documentType;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "item_id")
    private Item item;
    @ManyToOne(cascade = CascadeType.PERSIST,fetch = FetchType.LAZY)
    @JoinColumn(name = "templet_id")
    private DocumentTypeTree templet;
    @ManyToOne(cascade = CascadeType.PERSIST,fetch = FetchType.LAZY)
    @JoinColumn(name = "bitstream_id")
    private Bitstream bitstream;
    @Column(name = "bitstream_id",insertable=false, updatable=false)
    private UUID bitstream_id;
    @Column(name = "remark")
    private  String remark;
    @Column(name="index")
    private  Integer index;
    @Column(name = "created_date", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private  Date created_date=new Date();
    @Transient
    private Boolean isAllLoaded=false;
    @Transient
    private Boolean isDisplay=false;
    @Transient
    private List<DocumentTypeTree> clonechildNode=new ArrayList<>();

    /**
     * Protected constructor, create object using:
     * {@link ItemService#create(Context, WorkspaceItem)}
     */
    protected DocumentTypeTree() {

    }

    @Override
    public int getType() {
        return Constants.DOCTYPE;
    }

    @Override
    public String getName() {
        return "";
    }

    /**
     * Takes a pre-determined UUID to be passed to the object to allow for the
     * restoration of previously defined UUID's.
     *
     * @param uuid Takes a uuid to be passed to the Pre-Defined UUID Generator
     */
    protected DocumentTypeTree(UUID uuid) {
        this.predefinedUUID = uuid;
    }

    @Override
    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTempletName() {
        return templetName;
    }

    public void setTempletName(String templetName) {
        this.templetName = templetName;
    }

    public Boolean getIstemplet() {
        return istemplet;
    }

    public void setIstemplet(Boolean istemplet) {
        this.istemplet = istemplet;
    }

    public Boolean getDate() {
        return isDate;
    }

    public void setIsDate(Boolean isDate) {
        this.isDate = isDate;
    }

    public Boolean getIsRemark() {
        return this.isRemark;
    }
    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Date getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Date created_date) {
        this.created_date = created_date;
    }

    public void setIsRemark(Boolean isremark) {
        isRemark = isremark;
    }

    public Boolean getDescription() {
        return isDescription;
    }

    public void setDescription(Boolean description) {
        isDescription = description;
    }

    public Boolean getRootOfmaster() {
        return isRootOfmaster;
    }

    public void setRootOfmaster(Boolean rootOfmaster) {
        isRootOfmaster = rootOfmaster;
    }

    public Date getDoc_date() {
        return doc_date;
    }

    public void setDoc_date(Date doc_date) {
        this.doc_date = doc_date;
    }

    public DocumentTypeTree getParent() {
        return parent;
    }

    public void setParent(DocumentTypeTree parent) {
        this.parent = parent;
    }

    public Set<DocumentTypeTree> getChildren() {
        return children;
    }

    public void setChildren(Set<DocumentTypeTree> children) {
        this.children = children;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public DocumentTypeTree getTemplet() {
        return templet;
    }

    public void setTemplet(DocumentTypeTree templet) {
        this.templet = templet;
    }

    public Bitstream getBitstream() {
        return bitstream;
    }

    public void setBitstream(Bitstream bitstream) {
        this.bitstream = bitstream;
    }

    public Set<DocumentTypeTree> getNonTempletChildren() {
        return NonTempletChildren;
    }

    public void setNonTempletChildren(Set<DocumentTypeTree> nonTempletChildren) {
        NonTempletChildren = nonTempletChildren;
    }

    public Boolean getAllLoaded() {
        return isAllLoaded;
    }

    public void setAllLoaded(Boolean allLoaded) {
        isAllLoaded = allLoaded;
    }

    public Boolean getDisplay() {
        return isDisplay;
    }

    public void setDisplay(Boolean display) {
        isDisplay = display;
    }

    public Boolean getSubchild() {
        return isSubchild;
    }

    public void setSubchild(Boolean subchild) {
        isSubchild = subchild;
    }

    public Boolean getHasSubChild() {
        return hasSubChild;
    }

    public void setHasSubChild(Boolean hasSubChild) {
        this.hasSubChild = hasSubChild;
    }

    public Set<DocumentTypeTree> getSubchildEnablechildren() {
        return subchildEnablechildren;
    }

    public void setSubchildEnablechildren(Set<DocumentTypeTree> subchildEnablechildren) {
        this.subchildEnablechildren = subchildEnablechildren;
    }
    public Boolean getNonrepetitive() {
        return nonrepetitive;
    }

    public void setNonrepetitive(Boolean nonrepetitive) {
        this.nonrepetitive = nonrepetitive;
    }

    public List<DocumentTypeTree> getClonechildNode() {
        return this.clonechildNode;
    }

    public UUID getBitstream_id() {
        return bitstream_id;
    }

    public void setBitstream_id(UUID bitstream_id) {
        this.bitstream_id = bitstream_id;
    }

    public  void addTocloneNode(DocumentTypeTree node){
        if(!clonechildNode.contains(node)) {
            clonechildNode.add(node);
        }
        if(this.istemplet){
           List<DocumentTypeTree> nonTempletNode= clonechildNode.stream().filter(d->!d.getIstemplet()).collect(Collectors.toList());
           List<DocumentTypeTree> TempletNode= clonechildNode.stream().filter(d->d.getIstemplet()).collect(Collectors.toList());
           this.clonechildNode.clear();
            Collections.sort(TempletNode, new Comparator<DocumentTypeTree>() {
                @Override
                public int compare(DocumentTypeTree abc1, DocumentTypeTree abc2) {
                    return Integer.compare(abc1.getIndex(),abc2.getIndex());
                }
            });
            Collections.sort(nonTempletNode, new Comparator<DocumentTypeTree>() {
                @Override
                public int compare(DocumentTypeTree abc1, DocumentTypeTree abc2) {
                    return abc2.getDoc_date().compareTo(abc1.getDoc_date());
                }
            });
            this.clonechildNode.addAll(nonTempletNode);
            this.clonechildNode.addAll(TempletNode);
        }else{
            if(this.getHasSubChild() != null && !this.getHasSubChild() && this.isSubchild != null && this.isSubchild){
                Collections.sort(clonechildNode, new Comparator<DocumentTypeTree>() {
                    @Override
                    public int compare(DocumentTypeTree abc1, DocumentTypeTree abc2) {
                        if(abc1.getIndex()== null)abc1.setIndex(0);
                        if(abc2.getIndex()== null)abc2.setIndex(0);
                        return Integer.compare(abc1.getIndex(),abc2.getIndex());
                    }
                });
            }
        }

    }
    public  void addTocloneNode_old(DocumentTypeTree node){
        if(!clonechildNode.contains(node)) {
            clonechildNode.add(node);
        }
        if(this.istemplet){
            List<DocumentTypeTree> nonTempletNode= clonechildNode.stream().filter(d->!d.getIstemplet()).collect(Collectors.toList());
            List<DocumentTypeTree> TempletNode= clonechildNode.stream().filter(d->d.getIstemplet()).collect(Collectors.toList());
            this.clonechildNode.clear();
            Collections.sort(TempletNode, new Comparator<DocumentTypeTree>() {
                @Override
                public int compare(DocumentTypeTree abc1, DocumentTypeTree abc2) {
                    return Integer.compare(abc1.getIndex(),abc2.getIndex());
                }
            });
            Collections.sort(nonTempletNode, new Comparator<DocumentTypeTree>() {
                @Override
                public int compare(DocumentTypeTree abc1, DocumentTypeTree abc2) {
                    if (abc1.getDoc_date() == null || abc2.getDoc_date() == null)
                        return 0;
                    return abc2.getDoc_date().compareTo(abc1.getDoc_date());
                }
            });
            this.clonechildNode.addAll(nonTempletNode);
            this.clonechildNode.addAll(TempletNode);
        }else{
            if(this.getHasSubChild() != null && !this.getHasSubChild() && this.isSubchild != null && this.isSubchild){
                Collections.sort(clonechildNode, new Comparator<DocumentTypeTree>() {
                    @Override
                    public int compare(DocumentTypeTree abc1, DocumentTypeTree abc2) {
                        if(abc1.getIndex()== null)abc1.setIndex(0);
                        if(abc2.getIndex()== null)abc2.setIndex(0);
                        return Integer.compare(abc1.getIndex(),abc2.getIndex());
                    }
                });
            }
        }

    }
    public void setClonechildNode(List<DocumentTypeTree> clonechildNode) {
        this.clonechildNode = clonechildNode;
    }

    /*public Set<DocumentTypeTree> getNonSearchablechildren() {
        return nonSearchablechildren;
    }

    public void setNonSearchablechildren(Set<DocumentTypeTree> nonSearchablechildren) {
        this.nonSearchablechildren = nonSearchablechildren;
    }*/

    public void setChildNode(DocumentTypeTree childNode) {
        this.children.add(childNode);
    }

    public Stream<DocumentTypeTree> flattened() {
        this.getClonechildNode().addAll (this.getChildren());
       /* if(!this.getSubchild()){
            this.getChildren().addAll(this.getNonSearchablechildren());
        }*/
        return Stream.concat(
                Stream.of(this),
                children.stream().flatMap(DocumentTypeTree::flattened));
    }
    public DocumentTypeTree matchAndAddChild(DocumentTypeTree childNode){
       DocumentTypeTree documentTypeTree=this.flattened().filter(dn->dn.getID().equals(childNode.getParent().getID())).findFirst().orElse(null);
       if(documentTypeTree != null) {
           childNode.setDisplay(true);
           List<DocumentTypeTree>tempDoc=new ArrayList<>();
           tempDoc.add(childNode);
           tempDoc.addAll(documentTypeTree.getClonechildNode());
           documentTypeTree.getClonechildNode().clear();
           documentTypeTree.getClonechildNode().addAll(tempDoc);
          // documentTypeTree.setClonechildNode(documentTypeTree.getChildren());
           while(documentTypeTree.getParent() != null){
               documentTypeTree.setDisplay(true);
               documentTypeTree=documentTypeTree.getParent();
           }
           documentTypeTree.setDisplay(true);
           return  documentTypeTree;
       }
       return  null;
    }
    public DocumentTypeTree matchAndAddChildwithMap(DocumentTypeTree parent,List<DocumentTypeTree> node){
        DocumentTypeTree documentTypeTree=this.flattened().filter(dn->dn.getID().equals(parent.getID())).findFirst().orElse(null);
        if(documentTypeTree != null) {
            List<DocumentTypeTree>tempDoc=new ArrayList<>();
            node.stream().forEach(d->d.setDisplay(true));
            tempDoc.addAll(node);
            tempDoc.addAll(documentTypeTree.getClonechildNode());
            documentTypeTree.getClonechildNode().clear();
            documentTypeTree.getClonechildNode().addAll(tempDoc);
            // documentTypeTree.setClonechildNode(documentTypeTree.getChildren());
            while(documentTypeTree.getParent() != null){
                documentTypeTree.setDisplay(true);
                documentTypeTree=documentTypeTree.getParent();
            }
            documentTypeTree.setDisplay(true);
            return  documentTypeTree;
        }
        return  null;
    }

    public Boolean getIsforScanning() {
        return isforScanning;
    }

    public void setIsforScanning(Boolean isforScanning) {
        this.isforScanning = isforScanning;
    }
}
