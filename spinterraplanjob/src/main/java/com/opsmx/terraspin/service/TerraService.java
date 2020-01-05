/*
 * Copyright 2019 OpsMX, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opsmx.terraspin.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.opsmx.terraspin.component.ApplicationStartup;
import com.opsmx.terraspin.util.ProcessUtil;
import com.opsmx.terraspin.util.TerraAppUtil;

@Component
public class TerraService {

	private static final Logger log = LoggerFactory.getLogger(TerraService.class);
	
	ApplicationStartup ApplicationStartup = new ApplicationStartup();
	JSONParser parser = new JSONParser();
	/*
	 * @Autowired TerraAppUtil terraAppUtil1;
	 */
	TerraAppUtil terraAppUtil = new TerraAppUtil();
	ProcessUtil processutil = new ProcessUtil();
	
	static String userHomeDir = System.getProperty("user.home");
	static String DEMO_HTML = "<!DOCTYPE html> <html> <head> <meta charset=\"UTF-8\"> <title>Opsmx TerraApp</title> </head> <body bgcolor='#000000'> <pre style=\"color:white;\"> \"OPTION_SCPACE\" </pre> </body> </html>";
	String spinApplicationName = "spinApp";
	String spinPipelineName = "spinPipe";
	String spinpiPelineId = "spinPipeId";
	String spinPlan = System.getenv("plan");
	String spinArtifactAccount = System.getenv("artifactAccount");
	String applicationName = "applicationName-" + spinApplicationName;
	String pipelineName = "pipelineName-" + spinPipelineName;
	String pipelineId = "pipelineId-" + spinpiPelineId;
	
	
	
	@SuppressWarnings("unchecked")
	public void planStart(JSONObject artifactconfigaccount, String variableOverrideFile) {
		
		
		log.info("plan starting ::");
		log.info("applicationName:" + applicationName);
		log.info("pipelineName:" + pipelineName);
		log.info("pipelineId:" + pipelineId);
		
		File currentTerraformInfraCodeDir = terraAppUtil.createDirForPipelineId(applicationName, pipelineName,
				pipelineId);

		String statusFilePath = currentTerraformInfraCodeDir + "/planStatus";
		File statusFile = new File(statusFilePath);
		statusFile.delete();
		JSONObject status = new JSONObject();
		status.put("status", "RUNNING");
		InputStream statusInputStream = new ByteArrayInputStream(status.toString().getBytes(StandardCharsets.UTF_8));
		terraAppUtil.writeStreamOnFile(statusFile, statusInputStream);

		terraServicePlanSetting(artifactconfigaccount, spinArtifactAccount, spinPlan, currentTerraformInfraCodeDir);

		TerraformIntialInitThread terraInitialInitOperationCall = new TerraformIntialInitThread(currentTerraformInfraCodeDir);
		Thread trigger = new Thread(terraInitialInitOperationCall);
		trigger.start();
		try {
			trigger.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		String tfModulejsonpath = currentTerraformInfraCodeDir + "/.terraform/modules/modules.json";
		String tfModulejson = terraAppUtil.getStrJson(tfModulejsonpath);
		
		
		JSONObject moduleConfigObject = null;
		try {
			moduleConfigObject = (JSONObject) parser.parse(tfModulejson);
		} catch (ParseException pe) {
			log.info("Exception while parsing  tf module json :: " + tfModulejson);
			throw new RuntimeException("config Parse error:", pe);
		}
		
		JSONObject correcttModule = null; 
		JSONArray Modules = (JSONArray) moduleConfigObject.get("Modules");
		for(int i=0; i<Modules.size(); i++) {
			JSONObject currentModule = (JSONObject) Modules.get(i);
			String currentKey = (String) currentModule.get("Key");
			if(StringUtils.equalsAnyIgnoreCase("terraModule", currentKey)) {
				correcttModule = currentModule;
				break;
			}
		}
		
		String tfModuledir = (String) correcttModule.get("Dir");
		
		String exacttfRootModuleFilePathinStr = currentTerraformInfraCodeDir + "/" + tfModuledir;
		File exacttfRootModuleFilePathdir = new File(exacttfRootModuleFilePathinStr);
		
		
		TerraformInitThread terraInitOperationCall = new TerraformInitThread(exacttfRootModuleFilePathdir);
		Thread trigger1 = new Thread(terraInitOperationCall);
		trigger1.start();
		try {
			trigger1.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean ischangemod = processutil.runcommand("chmod 777 -R " + exacttfRootModuleFilePathdir);
		log.info("changing mod of file status :: " + ischangemod + "current dir :: " + exacttfRootModuleFilePathdir);

		
		TerraformPlanThread terraOperationCall = new TerraformPlanThread(exacttfRootModuleFilePathdir, currentTerraformInfraCodeDir, variableOverrideFile);
		Thread trigger2 = new Thread(terraOperationCall);
		trigger2.start();
		try {
			trigger2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@SuppressWarnings("unchecked")
	public JSONObject planStatus(String baseURL) {
		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/planStatus";
		String planOutputURL = baseURL + "/api/v1/terraform/planOutput/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId;

		JSONObject jsonObj = new JSONObject();
		JSONParser parser = new JSONParser();
		String statusStr = null;
		JSONObject outputJsonObj = new JSONObject();

		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));
			statusStr = (String) jsonObj.get("status");
			if (statusStr.equalsIgnoreCase("RUNNING")) {
				outputJsonObj.put("status", "RUNNING");
			} else {
				outputJsonObj.put("status", statusStr);
				outputJsonObj.put("planOutputURL", planOutputURL);
				log.info("terrafor plan output json :"+outputJsonObj);
			}

		} catch (Exception e) {
			log.info("Error : parse plan status");
			throw new RuntimeException("parse plan status error ",e);
		}
		
		return outputJsonObj;
	}

	public String planOutput(String applicationName, String pipelineName, String pipelineId, String baseURL) {
		String currentSatusDir = userHomeDir + "/.opsmx/spinnaker/" + applicationName + "/" + pipelineName + "/"
				+ pipelineId + "/planStatus";

		JSONObject jsonObj = new JSONObject();
		String statusStr = null;
		JSONParser parser = new JSONParser();
		try {
			jsonObj = (JSONObject) parser.parse(new FileReader(currentSatusDir));

			statusStr = (String) jsonObj.get("output");

		} catch (Exception e) {
			log.info("Error : parse plan out put");
			throw new RuntimeException("parse plan output error ",e);
		}
		String strToR = DEMO_HTML.replace("OPTION_SCPACE", statusStr);
		log.debug("terraform plan out put :"+strToR);
		return strToR;
	}
	
	
	public void terraServicePlanSetting(JSONObject artifactconfigaccount, String artifactAccount, String spinPlan, File currentTerraformInfraCodeDir) {
		String terraformInfraCode = null;

		if (StringUtils.isNoneEmpty(artifactAccount)) {
			String planConfig = new String( "module \"terraModule\"{source = \"git::https://GITUSER:GITPASS@github.com/GITUSER/GITPLANURL\"}");
			// String gitPlanUrl = spinPlan.split("https://")[1];
			String gitPlanUrl = spinPlan;
			// JSONObject artifacts = (JSONObject) halConfigObject.get("artifacts");
			JSONObject githubArtifactAccount = artifactconfigaccount;

			String gitUser = (String) githubArtifactAccount.get("username");
			String gittoken = (String) githubArtifactAccount.get("token");
			String gitPass = (String) githubArtifactAccount.get("password");
			
			if(StringUtils.isNoneEmpty(gitPass)) {
				terraformInfraCode = planConfig.replaceAll("GITUSER", gitUser).replaceAll("GITPASS", gitPass)
						.replaceAll("GITPLANURL", gitPlanUrl);
			}else {
				terraformInfraCode = planConfig.replaceAll("GITUSER", gitUser).replaceAll("GITPASS", gittoken)
						.replaceAll("GITPLANURL", gitPlanUrl);
			}
			

		} else {
			terraformInfraCode = spinPlan;
		}

		String infraCodePath = currentTerraformInfraCodeDir.getPath() + "/infraCode.tf";
		File infraCodfile = new File(infraCodePath);
		if (!infraCodfile.exists()) {
			try {
				infraCodfile.createNewFile();
			} catch (IOException e) {
				log.info("Error : terraform InfrCodfile Creation");
				throw new RuntimeException("Error : terraform InfrCodfile Creation ",e);

			}
		}

		InputStream infraCodeInputStream = new ByteArrayInputStream(
				terraformInfraCode.getBytes(StandardCharsets.UTF_8));
		terraAppUtil.overWriteStreamOnFile(infraCodfile, infraCodeInputStream);
	}	
}
