/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.service;

import org.dspace.content.cis.*;
import org.dspace.content.cis.dto.FirNoAndFirYearAndDistrictAndPoliceNameDTO;
import org.dspace.core.ContextCIS;

import java.sql.SQLException;
import java.util.List;

/**
 * Service interface class for the Bitstream object.
 * The implementation of this class is responsible for all business logic calls for the Bitstream object and is
 * autowired by spring
 *
 * @author kevinvandevelde at atmire.com
 */
public interface OrgNametService {
    public OrgNamet findBYcivilt(ContextCIS context,Short orgid) throws SQLException;
    public List<JudgeNamet> findJudGeName(ContextCIS context,String[] jocode) throws SQLException;
    public DistrictT findDistByDistc0de(ContextCIS context, String distcode) throws SQLException;

    public JudgeNamet findJudGeNameByJudgCode(ContextCIS context, String judgecode) throws SQLException;
    public PoliceStnT findbypscode(ContextCIS context, Integer pscode) throws SQLException;
    public FirNoAndFirYearAndDistrictAndPoliceNameDTO findFirNoandYearandDisticandPolicenameByCino(ContextCIS context, String cino) throws SQLException;
    public  List<String> GetExtraPet(ContextCIS context, String cino) throws SQLException;
    public  List<String> GetExtraRes(ContextCIS context, String cino) throws SQLException;
    public DspaceToCisMapping update(ContextCIS context, DspaceToCisMapping dspaceToCisMapping) throws SQLException;
    public DspaceToCisMapping findByCino(ContextCIS context, String cino) throws SQLException;
    public Naturet findCaseTypeByNatureT(ContextCIS context, Short nature_cd)  throws SQLException;
    public SubnatureThreeT getSubnatureThreeT(ContextCIS context, int cs_subject, int subnature1_cd, int subnature2_cd, int subnature3_cd) throws SQLException;
    public SubnatureTwoT getSubnatureTwoT(ContextCIS context, int cs_subject, int subnature1_cd, int subnature2_cd) throws SQLException ;
    public SubnatureOnet getSubnatureOnet(ContextCIS context, int cs_subject, int subnature1_cd) throws SQLException;
    public SubjectMaster getSubjectMaster(ContextCIS context, int c_subject) throws SQLException;
    public DspaceToCisMapping save(ContextCIS context, DspaceToCisMapping dspaceToCisMapping) throws SQLException;
    public List<Civaddresst> findExtraPatandres(ContextCIS context, String cino) throws SQLException;

}
