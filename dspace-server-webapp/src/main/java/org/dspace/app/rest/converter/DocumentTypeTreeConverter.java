/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;


import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.DocumentTypeRest;
import org.dspace.app.rest.model.DocumentTypeTreeRest;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.DocumentType;
import org.dspace.content.DocumentTypeTree;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.DocumentTypeService;
import org.dspace.content.service.DocumentTypeTreeService;
import org.dspace.core.Context;
import org.dspace.discovery.IndexableObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is the converter from/to the Item in the DSpace API data model and the
 * REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class DocumentTypeTreeConverter
        extends DSpaceObjectConverter<DocumentTypeTree, DocumentTypeTreeRest>
        implements IndexableObjectConverter<DocumentTypeTree, DocumentTypeTreeRest> {

    @Autowired
    private DocumentTypeTreeService documentTypeTreeService;
    @Autowired
    DocumentTypeConverter documentTypeConverter;

    @Autowired
    BitstreamConverter bitstreamConverter;
    @Autowired
    BitstreamService bitstreamService;
    @Autowired
    ItemConverter itemConverter;
    private List<PDDocument> podocument = new ArrayList<>();
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(DocumentTypeTreeConverter.class);

    @Override
    public DocumentTypeTreeRest convert(DocumentTypeTree obj, Projection projection) {
        DocumentTypeTreeRest documentTypeTreeRest = super.convert(obj, projection);
        documentTypeTreeRest.setTempletName(obj.getTempletName());
        documentTypeTreeRest.setIsTemplet(obj.getIstemplet());
        documentTypeTreeRest.setIsDate(obj.getDate());
        documentTypeTreeRest.setIsRemark(obj.getIsRemark());
        documentTypeTreeRest.setIsDescription(obj.getDescription());
        documentTypeTreeRest.setDoc_date(obj.getDoc_date());
        documentTypeTreeRest.setDesc(obj.getDesc());
        documentTypeTreeRest.setRootOfmaster(obj.getRootOfmaster());
        documentTypeTreeRest.setRemarkdesc(obj.getRemark());
        documentTypeTreeRest.setIndex(obj.getIndex());
        documentTypeTreeRest.setDisplay(obj.getDisplay());
        documentTypeTreeRest.setIsSubchild(obj.getSubchild());
        documentTypeTreeRest.setHasSubChild(obj.getHasSubChild());
        documentTypeTreeRest.setNonrepetitive(obj.getNonrepetitive());
        //documentTypeTreeRest.setIsforScanning(obj.getIsforScanning());
        if (obj.getParent() != null) {
            documentTypeTreeRest.setParentuuid(obj.getParent().getID());
            //DocumentTypeTreeRest childDocTypeTreeParentRest=documentTypeTreeConverter.convert(obj.getParent(),projection);
            // documentTypeTreeRest.setParent(childDocTypeTreeParentRest);
        }
        if (obj.getDocumentType() != null) {
            DocumentTypeRest documentTypeRest = documentTypeConverter.convert(obj.getDocumentType(), projection);
            documentTypeTreeRest.setDocumentType(documentTypeRest);
        }
        if (obj.getTemplet() != null) {
            DocumentTypeTreeRest documentTypeTreeRest1 = this.convert(obj.getTemplet(), projection);
            documentTypeTreeRest.setTempletTree(documentTypeTreeRest1);
        }
        if (obj.getBitstream() != null) {
            BitstreamRest bitstreamRest = bitstreamConverter.convertWithoutDocType(obj.getBitstream(), projection);
            documentTypeTreeRest.setBitstream(bitstreamRest);
        }
        if (obj.getItem() != null) {
            ItemRest itemRest = itemConverter.convert(obj.getItem(), projection);
            documentTypeTreeRest.setItem(itemRest);
        }
        Set<DocumentTypeTree> childDocumentTypeTrees = obj.getChildren();
        if (childDocumentTypeTrees.size() != 0) {
            documentTypeTreeRest.setHasChildren(true);
        }
        if (obj.getAllLoaded()) {
            if (childDocumentTypeTrees != null && childDocumentTypeTrees.size() != 0) {
                childDocumentTypeTrees.forEach(documentTypeTree -> {
                    documentTypeTree.setAllLoaded(true);
                    DocumentTypeTreeRest childDocTypeTreeRest = this.convert(documentTypeTree, projection);
                    documentTypeTreeRest.setChildren(childDocTypeTreeRest);
                });
            }
        }
        return documentTypeTreeRest;
    }
    public DocumentTypeTreeRest convertTempletForScanning(DocumentTypeTree obj, Projection projection) {
        try {
            DocumentTypeTreeRest documentTypeTreeRest = new DocumentTypeTreeRest();
            documentTypeTreeRest.setUuid(obj.getID().toString());
            documentTypeTreeRest.setId(obj.getID().toString());
            documentTypeTreeRest.setTempletName(obj.getTempletName());
            documentTypeTreeRest.setIsTemplet(obj.getIstemplet());
            documentTypeTreeRest.setIsDate(obj.getDate());
            documentTypeTreeRest.setIsRemark(obj.getIsRemark());
            documentTypeTreeRest.setIsDescription(obj.getDescription());
            documentTypeTreeRest.setDoc_date(obj.getDoc_date());
            documentTypeTreeRest.setDesc(obj.getDesc());
            documentTypeTreeRest.setRootOfmaster(obj.getRootOfmaster());
            documentTypeTreeRest.setRemarkdesc(obj.getRemark());
            documentTypeTreeRest.setIndex(obj.getIndex());
            documentTypeTreeRest.setDisplay(true);
            documentTypeTreeRest.setIsSubchild(obj.getSubchild());
            documentTypeTreeRest.setHasSubChild(obj.getHasSubChild());
            documentTypeTreeRest.setNonrepetitive(obj.getNonrepetitive()== null?false:obj.getNonrepetitive());//
            if (obj.getParent() != null) {
                documentTypeTreeRest.setParentuuid(obj.getParent().getID());
                //DocumentTypeTreeRest childDocTypeTreeParentRest=documentTypeTreeConverter.convert(obj.getParent(),projection);
                //documentTypeTreeRest.setParent(childDocTypeTreeParentRest);
            }
            if (obj.getDocumentType() != null) {
                DocumentTypeRest documentTypeRest = documentTypeConverter.convertWihoutMataData(obj.getDocumentType(), projection);
                documentTypeTreeRest.setDocumentType(documentTypeRest);
            }
            Set<DocumentTypeTree> childDocumentTypeTrees = obj.getChildren();
            System.out.println("childDocumentTypeTrees:::::"+childDocumentTypeTrees.size());
            if (childDocumentTypeTrees != null && childDocumentTypeTrees.size() != 0) {
                childDocumentTypeTrees.forEach(documentTypeTree -> {
                    documentTypeTree.setAllLoaded(true);
                    DocumentTypeTreeRest childDocTypeTreeRest = this.convertTempletForScanning(documentTypeTree, projection);
                    documentTypeTreeRest.setChildren(childDocTypeTreeRest);
                });
                documentTypeTreeRest.setHasChildren(true);
            }
            return documentTypeTreeRest;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public DocumentTypeTreeRest convertsearch(DocumentTypeTree obj, Projection projection) {
        DocumentTypeTreeRest documentTypeTreeRest = new DocumentTypeTreeRest();
        documentTypeTreeRest.setUuid(obj.getID().toString());
        documentTypeTreeRest.setId(obj.getID().toString());
        documentTypeTreeRest.setTempletName(obj.getTempletName());
        documentTypeTreeRest.setIsTemplet(obj.getIstemplet()!= null ?obj.getIstemplet() :false);
        documentTypeTreeRest.setIsDate(obj.getDate());
        documentTypeTreeRest.setIsRemark(obj.getIsRemark());
        documentTypeTreeRest.setIsDescription(obj.getDescription());
        documentTypeTreeRest.setDoc_date(obj.getDoc_date());
        documentTypeTreeRest.setDesc(obj.getDesc());
        documentTypeTreeRest.setRootOfmaster(obj.getRootOfmaster());
        documentTypeTreeRest.setRemarkdesc(obj.getRemark());
        documentTypeTreeRest.setIndex(obj.getIndex());
        documentTypeTreeRest.setDisplay(obj.getDisplay());
        documentTypeTreeRest.setIsSubchild(obj.getSubchild());
        documentTypeTreeRest.setHasSubChild(obj.getHasSubChild());
        documentTypeTreeRest.setNonrepetitive(obj.getNonrepetitive());
        //documentTypeTreeRest.setIsforScanning(obj.getIsforScanning());
        return documentTypeTreeRest;
    }

    public DocumentTypeTreeRest convertSubchild(DocumentTypeTree obj, Projection projection) {
        DocumentTypeTreeRest documentTypeTreeRest = super.convert(obj, projection);
        documentTypeTreeRest.setTempletName(obj.getTempletName());
        documentTypeTreeRest.setIsTemplet(obj.getIstemplet());
        documentTypeTreeRest.setIsDate(obj.getDate());
        documentTypeTreeRest.setIsRemark(obj.getIsRemark());
        documentTypeTreeRest.setIsDescription(obj.getDescription());
        documentTypeTreeRest.setDoc_date(obj.getDoc_date());
        documentTypeTreeRest.setDesc(obj.getDesc());
        documentTypeTreeRest.setRootOfmaster(obj.getRootOfmaster());
        documentTypeTreeRest.setRemarkdesc(obj.getRemark());
        documentTypeTreeRest.setIndex(obj.getIndex());
        documentTypeTreeRest.setDisplay(obj.getDisplay());
        documentTypeTreeRest.setIsSubchild(obj.getSubchild());
        documentTypeTreeRest.setHasSubChild(obj.getHasSubChild());
        documentTypeTreeRest.setNonrepetitive(obj.getNonrepetitive());
       // documentTypeTreeRest.setIsforScanning(obj.getIsforScanning());
        if (obj.getParent() != null) {
            documentTypeTreeRest.setParentuuid(obj.getParent().getID());

        }
        if (obj.getDocumentType() != null) {
            DocumentTypeRest documentTypeRest = documentTypeConverter.convert(obj.getDocumentType(), projection);
            documentTypeTreeRest.setDocumentType(documentTypeRest);
        }
        return documentTypeTreeRest;
    }

    public DocumentTypeTreeRest convertTODocumentType(DocumentTypeTree obj, Projection projection) {
        DocumentTypeTreeRest documentTypeTreeRest = super.convert(obj, projection);
        if (obj.getDocumentType() != null) {
            DocumentTypeRest documentTypeRest = documentTypeConverter.convert(obj.getDocumentType(), projection);
            documentTypeTreeRest.setDocumentType(documentTypeRest);
        }
        if (obj.getTemplet() != null) {
            DocumentTypeTreeRest documentTypeTreeRest1 = this.convert(obj.getTemplet(), projection);
            documentTypeTreeRest.setTempletTree(documentTypeTreeRest1);
        }
        documentTypeTreeRest.setIsSubchild(obj.getSubchild());
        documentTypeTreeRest.setIndex(obj.getIndex());  
        return documentTypeTreeRest;
    }
    public DocumentTypeTreeRest convertTODocumentTypeBitstream(DocumentTypeTree obj, Projection projection) {
        DocumentTypeTreeRest documentTypeTreeRest = new DocumentTypeTreeRest();
        if (obj.getDocumentType() != null) {
            DocumentTypeRest documentTypeRest = documentTypeConverter.convert(obj.getDocumentType(), projection);
            documentTypeTreeRest.setDocumentType(documentTypeRest);
        }
        if (obj.getTemplet() != null) {
            DocumentTypeTreeRest documentTypeTreeRest1 = new DocumentTypeTreeRest();
            documentTypeTreeRest.setTempletTree(documentTypeTreeRest1);
        }
        documentTypeTreeRest.setIsSubchild(obj.getSubchild());
        documentTypeTreeRest.setIndex(obj.getIndex());
        return documentTypeTreeRest;
    }

    public DocumentTypeTreeRest convertHasDisplay(Context context, DocumentTypeTree obj, Projection projection, Boolean mergePdf, List<PDDocument> podocuments) {
        try {

            DocumentTypeTreeRest documentTypeTreeRest = super.convert(obj, projection);
            documentTypeTreeRest.setTempletName(obj.getTempletName());
            documentTypeTreeRest.setIsTemplet(obj.getIstemplet());
            documentTypeTreeRest.setIsDate(obj.getDate());
            documentTypeTreeRest.setIsRemark(obj.getIsRemark());
            documentTypeTreeRest.setIsDescription(obj.getDescription());
            documentTypeTreeRest.setDoc_date(obj.getDoc_date());
            documentTypeTreeRest.setDesc(obj.getDesc());
            documentTypeTreeRest.setRootOfmaster(obj.getRootOfmaster());
            documentTypeTreeRest.setRemarkdesc(obj.getRemark());
            documentTypeTreeRest.setIndex(obj.getIndex());
            documentTypeTreeRest.setDisplay(true);
            documentTypeTreeRest.setIsSubchild(obj.getSubchild());
            documentTypeTreeRest.setHasSubChild(obj.getHasSubChild());
            documentTypeTreeRest.setNonrepetitive(obj.getNonrepetitive()== null?false:obj.getNonrepetitive());
           // documentTypeTreeRest.setIsforScanning(obj.getIsforScanning());
//
            if (obj.getParent() != null) {
                documentTypeTreeRest.setParentuuid(obj.getParent().getID());
                //DocumentTypeTreeRest childDocTypeTreeParentRest=documentTypeTreeConverter.convert(obj.getParent(),projection);
                //documentTypeTreeRest.setParent(childDocTypeTreeParentRest);
            }
            if (obj.getDocumentType() != null) {
                DocumentTypeRest documentTypeRest = documentTypeConverter.convert(obj.getDocumentType(), projection);
                documentTypeTreeRest.setDocumentType(documentTypeRest);
            }

            if (!obj.getIstemplet() && obj.getBitstream_id() != null) {
                BitstreamRest bitstreamRest = bitstreamConverter.convertId(obj.getBitstream_id());
                documentTypeTreeRest.setBitstream(bitstreamRest);
            }
            if (mergePdf) {
                try {
                    documentTypeTreeRest.addTitle(context,podocuments,bitstreamService);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (obj.getItem() != null) {
                ItemRest itemRest = itemConverter.convert(obj.getItem(), projection);
                documentTypeTreeRest.setItem(itemRest);
            }
            List<DocumentTypeTree> childDocumentTypeTrees = obj.getClonechildNode();
            if (childDocumentTypeTrees != null && childDocumentTypeTrees.size() != 0) {
                if (documentTypeTreeRest.getNonrepetitive()) {
                    DocumentTypeTree repetive = childDocumentTypeTrees.stream().filter(d -> !d.getIstemplet()).findFirst().get();
                    DocumentTypeTreeRest childDocTypeTreeRest = this.convertHasDisplay(context, repetive, projection, mergePdf, podocuments);
                    documentTypeTreeRest.setBitstream(childDocTypeTreeRest.getBitstream());
                    documentTypeTreeRest.setDesc(childDocTypeTreeRest.getDocumentType().getDocumenttypename());
                    documentTypeTreeRest.setIsTemplet(childDocTypeTreeRest.getIsTemplet());
                    documentTypeTreeRest.setIsSubchild(childDocTypeTreeRest.getIsSubchild());
                    documentTypeTreeRest.setDisplay(true);
                    if (mergePdf) {
                        try {
                            /*PDOutlineItem pdDestination = childDocTypeTreeRest.retuenChildbookmark();
                            if (childDocTypeTreeRest.getBitstream() != null) {
                                Bitstream bitstream = bitstreamService.find(context, UUID.fromString(childDocTypeTreeRest.getBitstream().getId()));
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
                                pdDestination.setDestination(dest);
                            }*/
                            documentTypeTreeRest.retuenChildbookmark().setDestination(childDocTypeTreeRest.retuenChildbookmark().getDestination());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    childDocumentTypeTrees = childDocumentTypeTrees.stream().filter(d -> d.getIstemplet()).collect(Collectors.toList());
                }
                if (obj.getAllLoaded()) {
                    childDocumentTypeTrees.forEach(documentTypeTree -> {
                        documentTypeTree.setAllLoaded(true);
                        DocumentTypeTreeRest childDocTypeTreeRest = this.convertHasDisplay(context, documentTypeTree, projection, mergePdf, podocuments);
                        if(mergePdf) {
                            documentTypeTreeRest.addtoChildBookmarkPDOutlineItem(childDocTypeTreeRest.retuenChildbookmark());
                        }
                        documentTypeTreeRest.setChildren(childDocTypeTreeRest);
                    });

                }
                documentTypeTreeRest.setHasChildren(true);
            }
            return documentTypeTreeRest;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public DocumentTypeTreeRest convertIteamDisplayPage(Context context, DocumentTypeTree obj, Projection projection, Boolean mergePdf, List<PDDocument> podocuments) {
        try {

            DocumentTypeTreeRest documentTypeTreeRest = new DocumentTypeTreeRest();
            documentTypeTreeRest.setId(obj.getID().toString());
            documentTypeTreeRest.setUuid(obj.getID().toString());
            documentTypeTreeRest.setTempletName(obj.getTempletName());
            documentTypeTreeRest.setIsTemplet(obj.getIstemplet());
            documentTypeTreeRest.setIsDate(obj.getDate());
            documentTypeTreeRest.setIsRemark(obj.getIsRemark());
            documentTypeTreeRest.setIsDescription(obj.getDescription());
            documentTypeTreeRest.setDoc_date(obj.getDoc_date());
            documentTypeTreeRest.setDesc(obj.getDesc());
            documentTypeTreeRest.setRootOfmaster(obj.getRootOfmaster());
            documentTypeTreeRest.setRemarkdesc(obj.getRemark());
            documentTypeTreeRest.setIndex(obj.getIndex());
            documentTypeTreeRest.setDisplay(true);
            documentTypeTreeRest.setIsSubchild(obj.getSubchild());
            documentTypeTreeRest.setHasSubChild(obj.getHasSubChild());
            documentTypeTreeRest.setNonrepetitive(obj.getNonrepetitive()== null?false:obj.getNonrepetitive());
        //    documentTypeTreeRest.setIsforScanning(obj.getIsforScanning());
//
           if (obj.getParent() != null) {
                documentTypeTreeRest.setParentuuid(obj.getParent().getID());
                //DocumentTypeTreeRest childDocTypeTreeParentRest=documentTypeTreeConverter.convert(obj.getParent(),projection);
                //documentTypeTreeRest.setParent(childDocTypeTreeParentRest);
            }
            if (obj.getDocumentType() != null) {
                DocumentTypeRest documentTypeRest = documentTypeConverter.convertWihoutMataData(obj.getDocumentType(), projection);
                documentTypeTreeRest.setDocumentType(documentTypeRest);
            }

            if (!obj.getIstemplet() && obj.getBitstream_id() != null) {
                BitstreamRest bitstreamRest = bitstreamConverter.convertId(obj.getBitstream_id());
                documentTypeTreeRest.setBitstream(bitstreamRest);
            }
            List<DocumentTypeTree> childDocumentTypeTrees = obj.getClonechildNode();
            if (childDocumentTypeTrees != null && childDocumentTypeTrees.size() != 0) {
                if (documentTypeTreeRest.getNonrepetitive() && childDocumentTypeTrees.size() == 1 ) {
                    Optional<DocumentTypeTree> repetiveOption = childDocumentTypeTrees.stream().filter(d -> !d.getIstemplet()).findFirst();
                    if(repetiveOption.isPresent()) {
                        DocumentTypeTree repetive=repetiveOption.get();
                        DocumentTypeTreeRest childDocTypeTreeRest = this.convertIteamDisplayPage(context, repetive, projection, mergePdf, podocuments);
                        documentTypeTreeRest.setBitstream(childDocTypeTreeRest.getBitstream());
                        documentTypeTreeRest.setDesc(childDocTypeTreeRest.getDocumentType().getDocumenttypename());
                        documentTypeTreeRest.setIsTemplet(childDocTypeTreeRest.getIsTemplet());
                        documentTypeTreeRest.setIsSubchild(childDocTypeTreeRest.getIsSubchild());
                        documentTypeTreeRest.setDisplay(true);
                        /*childDocumentTypeTrees.stream().filter(d->repetive.getID() != d.getID()).forEach(d->{
                            documentTypeTreeRest.setChildren(this.convertIteamDisplayPage(context, d, projection, mergePdf, podocuments));
                        });*/
                        childDocumentTypeTrees = childDocumentTypeTrees.stream().filter(d -> d.getIstemplet()).collect(Collectors.toList());
                    }
                }
                if (obj.getAllLoaded()) {
                    childDocumentTypeTrees.forEach(documentTypeTree -> {
                        documentTypeTree.setAllLoaded(true);
                        DocumentTypeTreeRest childDocTypeTreeRest = this.convertIteamDisplayPage(context, documentTypeTree, projection, mergePdf, podocuments);
                        if(mergePdf) {
                            documentTypeTreeRest.addtoChildBookmarkPDOutlineItem(childDocTypeTreeRest.retuenChildbookmark());
                        }
                        documentTypeTreeRest.setChildren(childDocTypeTreeRest);
                    });

                }
                documentTypeTreeRest.setHasChildren(true);
            }
            return documentTypeTreeRest;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static <T> Stream<T> getStreamFromIterator(Iterator<T> iterator) {
        // Convert the iterator to Spliterator
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
        // Get a Sequential Stream from spliterator
        return StreamSupport.stream(spliterator, false);
    }

    public DocumentTypeTreeRest converttoRestrecursion(Set<DocumentTypeTree> documentTypeTrees, Projection projection) {
        for (DocumentTypeTree documentTypeTree : documentTypeTrees) {
            Set<DocumentTypeTree> childDocumentTypeTrees = documentTypeTree.getChildren();
            if (childDocumentTypeTrees.size() == 0) {
                return this.convert(documentTypeTree, projection);
            }
            DocumentTypeTreeRest documentTypeTreeobj = converttoRestrecursion(childDocumentTypeTrees, projection);
            if (documentTypeTreeobj != null)
                return documentTypeTreeobj;

        }
        return null;
    }

    public List<PDDocument> returnPodocument() {
        return this.podocument;
    }

    @Override
    protected DocumentTypeTreeRest newInstance() {
        return new DocumentTypeTreeRest();
    }

    @Override
    public Class<DocumentTypeTree> getModelClass() {
        return DocumentTypeTree.class;
    }

    @Override
    public boolean supportsModel(IndexableObject idxo) {
        return idxo.getIndexedObject() instanceof DocumentTypeTree;
    }
}
