/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dao.impl;

import org.dspace.content.cis.*;
import org.dspace.content.cis.dao.CiviltDAO;
import org.dspace.core.AbstractHibernateDSOCISDAO;
import org.dspace.core.ContextCIS;

import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hibernate implementation of the Database Access Object interface class for the Bitstream object.
 * This class is responsible for all database calls for the Bitstream object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class CiviltDAOImpl extends AbstractHibernateDSOCISDAO<Civilt> implements CiviltDAO {

    protected CiviltDAOImpl() {
        super();
    }

    @Override
    public List<Civilt> findAll(ContextCIS context) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        Query query = createQuery(context, "select b from Civilt b ");
        return query.getResultList();
    }
    @Override
    public List<Civilt> findAllConnectedFromDspaceMapping(ContextCIS context, int maxResults) throws SQLException{
        Query query = createQuery(context, "select b from Civilt b  join b.dspaceToCisMapping as d where d.isrelationshipdone is null");
        query.setMaxResults(maxResults);
        return query.getResultList();
    }

    @Override
    public List<Civilt> findAllnotMigratedToDMS(ContextCIS context,int maxResults) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery<Civilt> criteriaQuery = criteriaBuilder.createQuery(Civilt.class);
        Root<Civilt> rootCivil = criteriaQuery.from(Civilt.class);
        Join<Civilt, CaseType> caseTypeJoin = rootCivil.join("caseType", JoinType.LEFT);
        Join<Civilt, Courtt> courtJoin = rootCivil.join("courtt", JoinType.LEFT);

        Join<Civilt, CriminalT> criminalT = rootCivil.join("criminalT", JoinType.LEFT);
      //  Join<Civilt, ExtraActT> extraactt = rootCivil.join("extraactt", JoinType.LEFT);
        Join<Civilt, DspaceToCisMapping> dspaceToCisMappingJoin = rootCivil.join("dspaceToCisMapping", JoinType.LEFT);
        //Join<Civilt, ExtraActT> extraactt = rootCivil.join("extraactt", JoinType.LEFT);
      // Join<Civilt, JudgeNamet> judgenamet = rootCivil.join("JudgeNamet", JoinType.LEFT);
        //Join<Civilt, ExtraActT> extraActT = rootCivil.join("extraActT", JoinType.LEFT);
        //Join<Civilt, Connectedt> connectedJoin = rootCivil.join("civilt2connected", JoinType.LEFT);
        //Join<Connectedt, DspaceToCisMapping> connectedDspaceJoin = connectedJoin.join("jkdmsDspaceCisMapping", JoinType.INNER);
        //Join<Civilt, SubjectMaster> subjectMasterJoin = rootCivil.join("subjectMaster", JoinType.LEFT);
        //subjectMasterJoin.on(criteriaBuilder.like(subjectMasterJoin.get(SubjectMaster_.SUBJECT_NAME),"%\\_%"));
        //Join<Civilt, SubnatureOnet> subnatureOneJoin = rootCivil.join("subnatureOnets", JoinType.LEFT);
        //subnatureOneJoin.on(criteriaBuilder.like(subnatureOneJoin.get(SubnatureOnet_.SUBNATURE1_DESC),"%\\_%"));
        //Join<Civilt, SubnatureTwoT> subnatureTwoJoin = rootCivil.join("subnatureTwoTS", JoinType.LEFT);
        //subnatureTwoJoin.on(criteriaBuilder.like(subnatureTwoJoin.get(SubnatureTwoT_.SUBNATURE2_DESC),"%\\_%"));
        //Join<Civilt, SubnatureThreeT> subnatureThreeJoin = rootCivil.join("subnatureThreeTS", JoinType.LEFT);
        //subnatureThreeJoin.on(criteriaBuilder.like(subnatureThreeJoin.get(SubnatureThreeT_.SUBNATURE3_DESC),"%\\_%"));
        /*Join<Civilt, OrgNamet> orgnameMasterJoin = rootCivil.join("civilt2orgnamemaster", JoinType.LEFT);
        //Join<Civilt, OrgNamet> orgnameMasterJoinres = rootCivil.join("civilt2orgnamemasterresorgid", JoinType.LEFT);
        orgnameMasterJoin.on(
                //criteriaBuilder.notEqual(rootCivil.get(Civilt_.ORGID),-99),
                criteriaBuilder.like(orgnameMasterJoin.get(OrgNamet_.ORGNAME),"%\\_%"),
                criteriaBuilder.lessThan(orgnameMasterJoin.get(OrgNamet_.ORGID),100)
        );
        orgnameMasterJoinres.on(
                //criteriaBuilder.notEqual(rootCivil.get(Civilt_.RESORGID),-99),
                criteriaBuilder.like(orgnameMasterJoinres.get(OrgNamet_.ORGNAME),"%\\_%"),
                criteriaBuilder.lessThan(orgnameMasterJoinres.get(OrgNamet_.ORGID),100)
        );*/

      /*  Join<Civilt, DspaceToCisMapping> dspaceCisMappingJoin = rootCivil.join("dspaceToCisMapping");
        Predicate customJoinCondition = criteriaBuilder.equal(
                rootCivil.get("cino"),
                dspaceCisMappingJoin.get("cino")
        );
        Predicate dspaceCisMappingIsNull = criteriaBuilder.isNull(dspaceCisMappingJoin.get("cino"));*/
//        Subquery<String> subquery = criteriaQuery.subquery(String.class);
  //      Root<DspaceToCisMapping> subqueryRoot = subquery.from(DspaceToCisMapping.class);
    //    subquery.select(subqueryRoot.get("cino"));
        Predicate regYearIsNotNull = criteriaBuilder.isNotNull(rootCivil.get("reg_year"));
//        Predicate fixcino = criteriaBuilder.equal(rootCivil.get("cino"),"JKJM030038282021");
        criteriaQuery.select(rootCivil)
                .distinct(true)
                .where(criteriaBuilder.and(criteriaBuilder.isNull(dspaceToCisMappingJoin.get("cino")),regYearIsNotNull));
                //.where(criteriaBuilder.and(criteriaBuilder.isNull(dspaceToCisMappingJoin.get("cino")),regYearIsNotNull,fixcino)); //dspaceCisMappingIsNull
        criteriaQuery.orderBy(criteriaBuilder.asc(rootCivil.get(Civilt_.reg_year)));
        return list(context, criteriaQuery, false, Civilt.class, maxResults, -1);
    }
}
