/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.DspaceEventConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.DocumentTypeRest;
import org.dspace.app.rest.model.DocumentTypeTreeRest;
import org.dspace.app.rest.model.DspaceEventRest;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DmsEvent;
import org.dspace.content.DocumentType;
import org.dspace.content.DocumentTypeTree;
import org.dspace.content.service.DmsEventService;
import org.dspace.content.service.DocumentTypeService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.RegistrationDataService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This is the repository responsible to manage Item Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(DspaceEventRest.CATEGORY + "." + DspaceEventRest.NAME)
public class DspaceEventRestRepository extends DSpaceObjectRestRepository<DmsEvent, DspaceEventRest> implements InitializingBean {
    @Autowired
    private DmsEventService dmsEventService;
    @Autowired
    private EPersonService ePersonService;
    @Autowired
    private DspaceEventConverter dspaceEventConverter;
    @Autowired
    private RegistrationDataService registrationDataService;

    public DspaceEventRestRepository(DmsEventService dmsEventService) {
        super(dmsEventService);
    }
    @Override
    public DspaceEventRest findOne(Context context, UUID uuid) {
        return null;
    }
    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<DspaceEventRest> findAll(Context context, Pageable pageable) {
        return  null;
    }
    @Override
    public Class<DspaceEventRest> getDomainClass() {
        return null;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
    }
    //@PreAuthorize("hasPermission(#id, 'ITEM', 'ADD')")
    @SearchRestMethod(name = "getCurrentDateEvent")
    public Page<DspaceEventRest> getCurrentDateEvent(Pageable pageable, @RequestParam(value = "userID") UUID userID, @RequestParam(value = "stdate") String stdateStr, @RequestParam(value = "enddate") String enddateStr,@RequestParam(value = "action") Integer action ) { //DspaceEventRest dspaceEventRest
        try {
            Context context = obtainContext();
            Date startDate=null;
            Date endDate=null;
            EPerson ePerson = null;
            if(userID != null){
                ePerson=ePersonService.find(context,userID);
            }
            if (stdateStr != null && enddateStr != null){
                DateTimeFormatter formatter_1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDate local_date_1 = LocalDate.parse(stdateStr, formatter_1);
                LocalDate local_date_2 = LocalDate.parse(enddateStr, formatter_1);
                startDate = java.sql.Date.valueOf(local_date_1);
                endDate = java.sql.Date.valueOf(local_date_2);
                System.out.println(stdateStr);
                System.out.println(enddateStr);

            }
            System.out.println("action:::::"+action);
            //int totalEvents= dmsEventService.countfindAllByCurrentDate(context,pageable.getPageSize(), Math.toIntExact(pageable.getOffset()),startDate,endDate,ePerson,action,dspaceEventRest.getCaseType(),dspaceEventRest.getCaseYear(),dspaceEventRest.getCaseNumber());
            int totalEvents= dmsEventService.countfindAllByCurrentDate(context,pageable.getPageSize(), Math.toIntExact(pageable.getOffset()),startDate,endDate,ePerson,action,null,null,null);
            //List<DmsEvent> dspDmsEvents= dmsEventService.findAllByCurrentDate(context,pageable.getPageSize(), Math.toIntExact(pageable.getOffset()),startDate,endDate,ePerson,action,dspaceEventRest.getCaseType(),dspaceEventRest.getCaseYear(),dspaceEventRest.getCaseNumber());
            List<DmsEvent> dspDmsEvents= dmsEventService.findAllByCurrentDate(context,pageable.getPageSize(), Math.toIntExact(pageable.getOffset()),startDate,endDate,ePerson,action,null,null,null);
            List<DspaceEventRest> dspaceEventRests= dspDmsEvents.stream().map(d -> {
                try {
                    return dspaceEventConverter.convert(d, utils.obtainProjection());
                }catch (Exception e){
                    e.printStackTrace();
                    return  null;
                }
            }).collect(toList());
            return new PageImpl(dspaceEventRests, pageable,totalEvents);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
