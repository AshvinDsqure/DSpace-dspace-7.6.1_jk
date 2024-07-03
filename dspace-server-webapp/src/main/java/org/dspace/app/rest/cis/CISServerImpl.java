/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.cis;

import org.dspace.app.rest.cis.constant.CIS;
import org.dspace.app.rest.cis.models.CISAuth;
import org.dspace.app.rest.cis.models.CISProcess;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Component
public class CISServerImpl {

    @Autowired
    public RestTemplate restTemplate;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private CacheManager cacheManager;

    public String startProcess(CISProcess cisProcess){
        try{
            String baseurl=configurationService.getProperty("cis.server");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<CISProcess> entity = new HttpEntity<CISProcess>(cisProcess,headers);
            System.out.println("Cause List Date:::"+cisProcess.getCauselist_date());
            System.out.println("JOCODE:::"+cisProcess.getJOCODE());
            String url =baseurl+ CIS.CAUSELIST+"?causelist_date="+cisProcess.getCauselist_date() +"&jocode="+cisProcess.getJOCODE()+"&secret_key="+cisProcess.getSecret_key();
            System.out.println("URL:::"+url);
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        }catch (Exception e){
            e.printStackTrace();
             throw  new RuntimeException("somethisng went with JBPM",e);
        }

    }
    public  String logintocisResapi(CISProcess cisProcess){
        try{
            String baseurl=configurationService.getProperty("cis.server");
            Cache cache = cacheManager.getCache("captchaCache");
            HttpHeaders headers = new HttpHeaders();
            CISAuth cisAuth=new CISAuth("jhhc","Nic@1234");
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<CISAuth> entity = new HttpEntity<CISAuth>(cisAuth,headers);
            if(cache.get("cisauthorizationtoken") == null){
                System.out.println("url::"+baseurl + CIS.AUTH + "/" + cisProcess.getJOCODE() + "/" + cisProcess.getDate());
                HttpHeaders rsponceheaders = restTemplate.postForEntity(baseurl + CIS.AUTH + "/" + cisProcess.getJOCODE() + "/" + cisProcess.getDate(), entity, String.class).getHeaders();
                System.out.println("rsponceheaders.get(\"Authorization\");:::" + rsponceheaders.get("Authorization"));
            }else{
                String cisauthorizationtoken = cache.get("cisauthorizationtoken").get().toString();
                System.out.println("cisauthorizationtoken:::"+cisauthorizationtoken);

            }
            return "";
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException("somethisng went with JBPM",e);
        }
    }
    public String searchCase(CISProcess cisProcess){
        try{
            String baseurl=configurationService.getProperty("cis.server");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<CISProcess> entity = new HttpEntity<CISProcess>(cisProcess,headers);
            return restTemplate.exchange(baseurl+ CIS.SEARCHCASE, HttpMethod.POST, entity, String.class).getBody();
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException("somethisng went with JBPM",e);
        }

    }
    public String migrate(CISProcess cisProcess){
        try{
            String baseurl=configurationService.getProperty("cis.server");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<CISProcess> entity = new HttpEntity<CISProcess>(cisProcess,headers);
            return restTemplate.exchange(baseurl+ CIS.MIGRATECASEFILE, HttpMethod.POST, entity, String.class).getBody();
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException("somethisng went with JBPM",e);
        }

    }
}
