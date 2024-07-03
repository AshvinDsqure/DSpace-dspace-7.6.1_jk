/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.model.PdfAnnotationRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.PdfAnnotation;
import org.dspace.content.service.DocumentTypeService;
import org.dspace.discovery.IndexableObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is the converter from/to the Item in the DSpace API data model and the
 * REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class PdfAnnotationConverter
        extends DSpaceObjectConverter<PdfAnnotation, PdfAnnotationRest>
        implements IndexableObjectConverter<PdfAnnotation, PdfAnnotationRest> {

    @Autowired
    private DocumentTypeService documentTypeService;
    @Autowired
    BitstreamConverter bitstreamConverter;
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(PdfAnnotationConverter.class);

    @Override
    public PdfAnnotationRest convert(PdfAnnotation obj, Projection projection) {
        PdfAnnotationRest pdfAnnotationRest = new PdfAnnotationRest();
        if (obj.getBitstream() != null){
            pdfAnnotationRest.setBitstreamRest(bitstreamConverter.convert(obj.getBitstream(), projection));
        }
        if(obj.getNoteannotatiostr()!=null){
            pdfAnnotationRest.setNoteannotatiostr(obj.getNoteannotatiostr());
        }
        pdfAnnotationRest.setId(obj.getID().toString());
        pdfAnnotationRest.setUuid(obj.getID().toString());
        pdfAnnotationRest.setPdfannotationStr(obj.getAnnotationStr());
        return pdfAnnotationRest;
    }


    @Override
    protected PdfAnnotationRest newInstance() {
        return new PdfAnnotationRest();
    }
    @Override
    public Class<PdfAnnotation> getModelClass() {
        return PdfAnnotation.class;
    }

    @Override
    public boolean supportsModel(IndexableObject idxo) {
        return idxo.getIndexedObject() instanceof PdfAnnotation;
    }
}
