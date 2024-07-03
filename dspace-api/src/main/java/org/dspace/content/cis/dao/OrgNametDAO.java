/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dao;

import org.dspace.content.cis.*;
import org.dspace.core.ContextCIS;

import java.sql.SQLException;
import java.util.List;

/**
 * Database Access Object interface class for the Bitstream object.
 * The implementation of this class is responsible for all database calls for the Bitstream object and is autowired
 * by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 * @author kevinvandevelde at atmire.com
 */
public interface OrgNametDAO {
    public SubjectMaster getSubjectMaster(ContextCIS context, int c_subject) throws SQLException;
    public List<JudgeNamet> findJudGeName(ContextCIS context, String[] jocode) throws SQLException;
    public JudgeNamet findJudGeNameByJudgCode(ContextCIS context, String judgecode) throws SQLException;
    public PoliceStnT findbypscode(ContextCIS context, Integer pscode) throws SQLException;

    public DistrictT findDistByDistc0de(ContextCIS context, String distcode) throws SQLException;

    public SubnatureThreeT getSubnatureThreeT(ContextCIS context, int cs_subject,int subnature1_cd,int subnature2_cd,int subnature3_cd) throws SQLException;
    public  List<Object[]> findFirNoandYearandDisticandPolicenameByCino(ContextCIS context, String cino) throws SQLException;
    public  List<Object[]> GetExtraPet(ContextCIS context, String cino) throws SQLException;
    public  List<Object[]> GetExtraRes(ContextCIS context, String cino) throws SQLException;

    public SubnatureTwoT getSubnatureTwoT(ContextCIS context,int cs_subject,int subnature1_cd,int subnature2_cd) throws SQLException ;
    public SubnatureOnet getSubnatureOnet(ContextCIS context,int cs_subject,int subnature1_cd) throws SQLException;
    public OrgNamet findBYcivilt(ContextCIS context,Short orgid) throws SQLException;
    public Naturet findCaseTypeByNatureT(ContextCIS context, Short nature_cd)  throws SQLException;
    public List<Civaddresst>  findExtraPatandres(ContextCIS context, String cino) throws SQLException;

}
