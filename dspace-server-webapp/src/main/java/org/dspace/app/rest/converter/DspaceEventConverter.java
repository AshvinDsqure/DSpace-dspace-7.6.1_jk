/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Enum.DmsAction;
import org.dspace.app.rest.Enum.DmsObject;
import org.dspace.app.rest.model.DspaceEventRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.DmsEvent;
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
public class DspaceEventConverter
        extends DSpaceObjectConverter<DmsEvent, DspaceEventRest>
        implements IndexableObjectConverter<DmsEvent, DspaceEventRest> {


    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(DspaceEventConverter.class);
    @Autowired
    ItemConverter itemConverter;
    @Autowired
    DocumentTypeConverter documentTypeConverter;
    @Autowired
    DocumentTypeTreeConverter documentTypeTreeConverter;
    @Autowired
    EPersonConverter ePersonConverter;
    @Override
    public DspaceEventRest convert(DmsEvent obj, Projection projection) {
        DspaceEventRest dspaceEventRest = new DspaceEventRest();
        try {
            dspaceEventRest.setId(obj.getID().toString());
            dspaceEventRest.setUuid(obj.getID().toString());
            dspaceEventRest.setAction(DmsAction.find(obj.getAction()).getAction());
            dspaceEventRest.setActionDate(obj.getActionDate());
            if(obj.getePerson() != null) {
                dspaceEventRest.setePersonRest(ePersonConverter.convertCompressDetail(obj.getePerson(),projection));
            }
            if(obj.getItem() != null){
                dspaceEventRest.setItem(itemConverter.convertWihoutMataData(obj.getItem(),projection));
            }
            /*if(obj.getDocumentTypeTree() != null){
                dspaceEventRest.setDocumenttypenameRest(documentTypeTreeConverter.convertTODocumentType(obj.getDocumentTypeTree(),projection));
            }*/
            if(obj.getDescription() != null){
                dspaceEventRest.setDescription(obj.getDescription());
            }
            if(obj.getTitle() != null) {
                dspaceEventRest.setDescription(obj.getTitle());
                dspaceEventRest.setTitle(obj.getTitle());
            }
            if(obj.getObjectType() != null){
                dspaceEventRest.setDspaceObject(DmsObject.find(obj.getObjectType()).getAction());
            }
        }catch (Exception e){
        e.printStackTrace();
        }
        return dspaceEventRest;
    }
    @Override
    protected DspaceEventRest newInstance() {
        return new DspaceEventRest();
    }

    @Override
    public Class<DmsEvent> getModelClass() {
        return DmsEvent.class;
    }

    @Override
    public boolean supportsModel(IndexableObject idxo) {
        return idxo.getIndexedObject() instanceof DmsEvent;
    }
}
