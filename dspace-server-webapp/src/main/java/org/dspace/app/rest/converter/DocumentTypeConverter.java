/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.model.DocumentTypeRest;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.MetadataValueList;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.DocumentType;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.DocumentTypeService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.discovery.IndexableObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * This is the converter from/to the Item in the DSpace API data model and the
 * REST data model
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class DocumentTypeConverter
        extends DSpaceObjectConverter<DocumentType, DocumentTypeRest>
        implements IndexableObjectConverter<DocumentType, DocumentTypeRest> {

    @Autowired
    private DocumentTypeService documentTypeService;

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(DocumentTypeConverter.class);

    @Override
    public DocumentTypeRest convert(DocumentType obj, Projection projection) {
        DocumentTypeRest documentTypeRest = super.convert(obj, projection);
        documentTypeRest.setDocumenttypename(obj.getDocumenttypename());
        return documentTypeRest;
    }

    public DocumentTypeRest convertWihoutMataData(DocumentType obj, Projection projection) {
        DocumentTypeRest documentTypeRest = new DocumentTypeRest();
        documentTypeRest.setId(obj.getID().toString());
        documentTypeRest.setDocumenttypename(obj.getDocumenttypename());
        return documentTypeRest;
    }
    @Override
    protected DocumentTypeRest newInstance() {
        return new DocumentTypeRest();
    }
    @Override
    public Class<DocumentType> getModelClass() {
        return DocumentType.class;
    }

    @Override
    public boolean supportsModel(IndexableObject idxo) {
        return idxo.getIndexedObject() instanceof DocumentType;
    }
}
