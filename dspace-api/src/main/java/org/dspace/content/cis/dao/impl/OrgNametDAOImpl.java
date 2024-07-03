/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dao.impl;

import org.dspace.content.cis.*;
import org.dspace.content.cis.dao.OrgNametDAO;
import org.dspace.core.AbstractHibernateDSOCISDAO;
import org.dspace.core.ContextCIS;
import org.hibernate.Session;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;

/**
 * Hibernate implementation of the Database Access Object interface class for the Bitstream object.
 * This class is responsible for all database calls for the Bitstream object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class OrgNametDAOImpl extends AbstractHibernateDSOCISDAO<OrgNamet> implements OrgNametDAO {

    protected OrgNametDAOImpl() {
        super();
    }

    @Override
    public OrgNamet findBYcivilt(ContextCIS context,Short orgid) throws SQLException {
        Query query = createQuery(context, "FROM OrgNamet WHERE orgid=:orgid");
        query.setParameter("orgid", orgid);
        return singleResult(query);
    }

    @Override
    public Naturet findCaseTypeByNatureT(ContextCIS context, Short nature_cd)  throws SQLException {
        Query query = createQuery(context, "FROM Naturet WHERE nature_cd=:nature_cd");
        query.setParameter("nature_cd", nature_cd);
        return (Naturet)query.getSingleResult();
    }
    @Override
    public List<Civaddresst>  findExtraPatandres(ContextCIS context, String cino) throws SQLException{
        //Query query = createNativeQuery(context, "select * from civ_address_t_a where cino='"+cino+"' and display='Y'",Civaddresst.class);
        Query query = createNativeQuery(context, "select * from civ_address_t where cino='"+cino+"' and display='Y'",Civaddresst.class);
        return (List<Civaddresst>)query.getResultList();
    }

    @Override
    public SubnatureOnet getSubnatureOnet(ContextCIS context,int cs_subject,int subnature1_cd) throws SQLException {
      try {
          Query query = createQuery(context, "FROM SubnatureOnet WHERE subnature1_cd=:subnature1_cd and nature_cd=:nature_cd and subnature1_desc like :subnature1_desc");
          query.setParameter("subnature1_cd", subnature1_cd);
          query.setParameter("nature_cd", cs_subject);
          query.setParameter("subnature1_desc", "%_%");
          return (SubnatureOnet) query.getSingleResult();
      }catch (Exception e){
          System.out.println("eror in getSubnatureOnet"+e.getMessage());
          return null;
      }
    }
    @Override
    public SubnatureTwoT getSubnatureTwoT(ContextCIS context,int cs_subject,int subnature1_cd,int subnature2_cd) throws SQLException {
       try {

           Query query = createQuery(context, "FROM SubnatureTwoT WHERE subnature1_cd=:subnature1_cd and subnature2_cd=:subnature2_cd and nature_cd=:nature_cd and  subnature2_desc like :subnature2_desc");
           query.setParameter("subnature1_cd", subnature1_cd);
           query.setParameter("subnature2_cd", subnature2_cd);
           query.setParameter("nature_cd", cs_subject);
           query.setParameter("subnature2_desc", "%_%");
           return (SubnatureTwoT) query.getSingleResult();
       }catch (Exception e){
           System.out.println("error in getSubnatureTwoT"+e.getMessage());
           return null;
       }
    }
    @Override
    public SubnatureThreeT getSubnatureThreeT(ContextCIS context, int cs_subject,int subnature1_cd,int subnature2_cd,int subnature3_cd) throws SQLException {

//        System.out.println("::::subnature1_cd::::::::::"+subnature1_cd);
//        System.out.println("::::subnature2_cd::::::::::"+subnature2_cd);
//        System.out.println("::::subnature3_cd::::::::::"+subnature3_cd);
//        System.out.println("::::cs_subject::::::::::"+cs_subject);

        try {
            Query query = createQuery(context, "FROM SubnatureThreeT  WHERE  subnature1_cd=:subnature1_cd and subnature2_cd=:subnature2_cd and  subnature3_cd=:subnature3_cd and nature_cd=:nature_cd");
            query.setParameter("subnature1_cd", subnature1_cd);
            query.setParameter("subnature2_cd", subnature2_cd);
            query.setParameter("subnature3_cd", subnature3_cd);
            query.setParameter("nature_cd", cs_subject);
            // query.setParameter("subnature3_desc", "%_%");
            return (SubnatureThreeT)query.getSingleResult();}
        catch (Exception e){
            System.out.println("error :::::::"+e.getMessage());
            return null;
        }
    }

    @Override
    public SubjectMaster getSubjectMaster(ContextCIS context, int c_subject) throws SQLException {
        Query query = createQuery(context, "FROM SubjectMaster WHERE subject_code=:subject_code ");
        query.setParameter("subject_code", c_subject);
        return (SubjectMaster)query.getSingleResult();
    }

    @Override
    public List<JudgeNamet> findJudGeName(ContextCIS context, String[] jocode) throws SQLException {
        Query query = createQuery(context, "FROM JudgeNamet WHERE jocode IN (:jocode)");
        query.setParameter("jocode", jocode);
        return query.getResultList();
    }

    @Override
    public JudgeNamet findJudGeNameByJudgCode(ContextCIS context, String judgecode) throws SQLException {
        Query query = createQuery(context, "FROM JudgeNamet WHERE judge_code =:judgecode");
        query.setParameter("judgecode", Short.valueOf(judgecode));
        return (JudgeNamet) query.getSingleResult();
    }

    @Override
    public PoliceStnT findbypscode(ContextCIS context, Integer pscode) throws SQLException {
        try {
        Query query = createQuery(context, "FROM PoliceStnT WHERE police_st_code =:pscode");
        query.setParameter("pscode",pscode);
        return (PoliceStnT) query.getSingleResult();}catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public DistrictT findDistByDistc0de(ContextCIS context, String distcode) throws SQLException {
        Query query = createQuery(context, "FROM DistrictT WHERE dist_code = :distcode");
        query.setParameter("distcode", Short.valueOf(distcode));
        return (DistrictT) query.getSingleResult();
    }



    @Override
    public List<Object[]> findFirNoandYearandDisticandPolicenameByCino(ContextCIS context, String cino) throws SQLException {
     try
     {
        String s="SELECT c.fir_no,c.fir_year,d.dist_name,p.police_st_name FROM criminal_t c \n" +
                "\t\t\tLEFT JOIN district_t d ON d.dist_code=c.police_dist_code AND d.state_id='5'\n" +
                "\t\t\tLEFT JOIN police_stn_t p ON p.police_st_code=c.police_st_code AND c.police_dist_code=p.dist_code AND c.police_state_id=p.state_id\n" +
                "\t\t\t  WHERE c.cino = '"+cino+" '\n" +
                "\n" +
                "\t\t\tUNION\n" +
                "\n" +
                "\t\t\tSELECT c.fir_no,c.fir_year,d.dist_name,p.police_st_name FROM criminal_t_a c \n" +
                "\t\t\tLEFT JOIN district_t d ON d.dist_code=c.police_dist_code AND d.state_id='5'\n" +
                "\t\t\tLEFT JOIN police_stn_t p ON p.police_st_code=c.police_st_code AND c.police_dist_code=p.dist_code AND c.police_state_id=p.state_id\n" +
                "\t\t\t  WHERE c.cino = '"+cino+" '";
        Query query=createNativeQuery(context,s,null);
        List<Object[]> results = query.getResultList();
        return results;
     }catch (Exception e){
         System.out.println(":::::::::Error  in findFirNoandYearandDisticandPolicenameByCino:::::::::::::::::::::::"+e.getMessage());
         e.printStackTrace();
         return null;
     }
    }

    @Override
    public List<Object[]> GetExtraPet(ContextCIS context, String cino) throws SQLException {
        try
        {
            String s="select pet_name as petitionername from civil_t where cino='"+cino+"' and  display='Y'\n" +
                    "UNION\n" +
                    "select name as petitionername  from civ_address_t where cino='"+cino+"' and type=1  and display='Y';";
            Query query=createNativeQuery(context,s,null);
            List<Object[]> results = query.getResultList();
            return results;
        }catch (Exception e){
            System.out.println(":::::::::Error  in GetExtraPet:::::::::::::::::::::::"+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Object[]> GetExtraRes(ContextCIS context, String cino) throws SQLException {
        try
        {
            String s="select res_name as respondentname from civil_t where cino='"+cino+"' and  display='Y'\n" +
                    "UNION\n" +
                    "select name as respondentname from civ_address_t where cino='"+cino+"' and type=2  and display='Y'";
            Query query=createNativeQuery(context,s,null);
            List<Object[]> results = query.getResultList();
            return results;
        }catch (Exception e){
            System.out.println(":::::::::Error  in GetExtraPet:::::::::::::::::::::::"+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
