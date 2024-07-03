/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.service.impl;

import org.dspace.content.cis.*;
import org.dspace.content.cis.dao.DspaceToCisMappingDAO;
import org.dspace.content.cis.dao.OrgNametDAO;
import org.dspace.content.cis.dto.FirNoAndFirYearAndDistrictAndPoliceNameDTO;
import org.dspace.content.cis.service.OrgNametService;
import org.dspace.core.ContextCIS;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service interface class for the Bitstream object.
 * The implementation of this class is responsible for all business logic calls for the Bitstream object and is
 * autowired by spring
 *
 * @author kevinvandevelde at atmire.com
 */
public class OrgNametServiceImpl implements OrgNametService {
    @Autowired
    OrgNametDAO orgNametDAO;
    @Autowired
    DspaceToCisMappingDAO dspaceToCisMappingDAO;

    @Override
    public OrgNamet findBYcivilt(ContextCIS context, Short orgid) throws SQLException{
        return orgNametDAO.findBYcivilt(context,orgid);
    }

    @Override
    public List<JudgeNamet> findJudGeName(ContextCIS context, String[] jocode) throws SQLException {
        return orgNametDAO.findJudGeName(context,jocode);
    }

    @Override
    public DistrictT findDistByDistc0de(ContextCIS context, String distcode) throws SQLException {
        return orgNametDAO.findDistByDistc0de(context,distcode);
    }

    @Override
    public JudgeNamet findJudGeNameByJudgCode(ContextCIS context, String judgecode) throws SQLException {
        return orgNametDAO.findJudGeNameByJudgCode(context,judgecode);
    }

    @Override
    public PoliceStnT findbypscode(ContextCIS context, Integer pscode) throws SQLException {
        return orgNametDAO.findbypscode(context,pscode);
    }

    @Override
    public FirNoAndFirYearAndDistrictAndPoliceNameDTO findFirNoandYearandDisticandPolicenameByCino(ContextCIS context, String cino) throws SQLException {
        FirNoAndFirYearAndDistrictAndPoliceNameDTO dto=  new FirNoAndFirYearAndDistrictAndPoliceNameDTO();
        List<Object[]>objects= orgNametDAO.findFirNoandYearandDisticandPolicenameByCino(context,cino);
       if(objects!=null&&objects.size()!=0){
           for (Object[] result : objects) {
            String firno = (String) result[0];
            Short firyear = (Short) result[1];
            String dist_name = (String) result[2];
            String police_st_name = (String) result[3];
               System.out.println("::::::::::firno::::::::"+firno);
               System.out.println("::::::::::firyear::::::::"+firyear);
               System.out.println("::::::::::dist_name::::::::"+dist_name);
               System.out.println("::::::::::police_st_name::::::::"+police_st_name);

               if(firno!=null){
                dto.setFir_no(firno);
               }
            if(firyear!=null&&firyear!=0){
                dto.setFir_year(firyear.toString());
            }if(dist_name!=null){
                dto.setDist_name(dist_name);
            }if(police_st_name!=null){
                dto.setPolice_st_name(police_st_name);
            }
         }
       }
        return dto;
    }

    @Override
    public List<String> GetExtraPet(ContextCIS context, String cino) throws SQLException {
        List<String>petList=new ArrayList<>();
        try {
            List<Object[]> objects = orgNametDAO.GetExtraPet(context, cino);
            if (objects != null && objects.size() != 0) {
                for (Object[] result : objects) {

                    Object name= (Object) result[0];
                    if (name != null) {
                        Object element = result[0]; // Accessing the first element of resul
                        // Checking the datatype of the first element
                        if (element instanceof String) {
                            System.out.println("The datatype of result[0] is String");
                        } else if (element instanceof Integer) {
                            System.out.println("The datatype of result[0] is Integer");
                        }
                    } else {
                        System.out.println("The result array or collection is empty");
                    }
                }
            }
            return petList;
        }catch (Exception e){
            System.out.println("Error "+e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> GetExtraRes(ContextCIS context, String cino) throws SQLException {
        List<String>petList=new ArrayList<>();
        try {
            List<Object[]> objects = orgNametDAO.GetExtraRes(context, cino);
            if (objects != null && objects.size() != 0) {
                for (Object[] result : objects) {
                    List<Object> name= (List<Object>) result[0];
                    if(name!=null) {
                        for (Object o : name) {
                            String name1 = (String) o;
                            System.out.println("in name1 :>>>>" + name1);
                        }
                    }
                }
            }
            return petList;
        }catch (Exception e){
            System.out.println("Error"+e.getMessage());
        return null;
        }
    }


    @Override
    public Naturet findCaseTypeByNatureT(ContextCIS context, Short nature_cd)  throws SQLException {
        return orgNametDAO.findCaseTypeByNatureT(context,nature_cd);
    }

    @Override
    public SubnatureThreeT getSubnatureThreeT(ContextCIS context, int cs_subject, int subnature1_cd, int subnature2_cd, int subnature3_cd) throws SQLException {
        return orgNametDAO.getSubnatureThreeT(context,cs_subject,subnature1_cd,subnature2_cd,subnature3_cd);
    }

    @Override
    public SubnatureTwoT getSubnatureTwoT(ContextCIS context, int cs_subject, int subnature1_cd, int subnature2_cd) throws SQLException {
        return orgNametDAO.getSubnatureTwoT(context,cs_subject,subnature1_cd,subnature2_cd);
    }

    @Override
    public SubnatureOnet getSubnatureOnet(ContextCIS context, int cs_subject, int subnature1_cd) throws SQLException {
        return orgNametDAO.getSubnatureOnet(context,cs_subject,subnature1_cd);
    }

    @Override
    public SubjectMaster getSubjectMaster(ContextCIS context, int c_subject) throws SQLException {
        return orgNametDAO.getSubjectMaster(context,c_subject);
    }

    @Override
    public DspaceToCisMapping save(ContextCIS context, DspaceToCisMapping dspaceToCisMapping) throws SQLException {
        return dspaceToCisMappingDAO.saveObject(context,dspaceToCisMapping);
    }
    @Override
    public List<Civaddresst> findExtraPatandres(ContextCIS context, String cino) throws SQLException{
        return orgNametDAO.findExtraPatandres(context,cino);
    }
    @Override
    public DspaceToCisMapping update(ContextCIS context, DspaceToCisMapping dspaceToCisMapping) throws SQLException{
        return dspaceToCisMappingDAO.update(context,dspaceToCisMapping);
    }
    @Override
    public DspaceToCisMapping findByCino(ContextCIS context, String cino) throws SQLException{
        return dspaceToCisMappingDAO.findByCino(context,cino);
    }

}
