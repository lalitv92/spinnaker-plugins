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

package com.opsmx.spinautodb.component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.opsmx.spinautodb.artifact.interfaces.Githubprovider;
import com.opsmx.spinautodb.interfaces.Provider;
import com.opsmx.spinautodb.util.TerraAppUtil;


@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

	/**
	 * This method is called during Spring's startup.
	 * 
	 * @param event Event raised when an ApplicationContext gets initialized or
	 *              refreshed.
	 */
	private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);
	static final String separator = File.separator;

	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {

		TerraAppUtil tu = new TerraAppUtil();

		String currentUserDir = System.getProperty("user.home");
		String dbaccount = System.getenv("dbaccount");
		String artifactaccount = System.getenv("artifactaccount");
		String dbscript = System.getenv("dbscript");

		log.info("currentUserDir:" + currentUserDir);
		log.info("dbaccount:" + dbaccount);
		log.info("artifactaccount:" + artifactaccount);
		log.info("dbscript:" + dbscript);
		
		/*
		 * log.info(""); String opsmxdir = currentUserDir + separator + ".opsmx"; File
		 * opsmxDirFile = new File(opsmxdir); if (!opsmxDirFile.exists())
		 * opsmxDirFile.mkdir();
		 * 
		 * File scriptDirFile = new File(opsmxDirFile.getPath() + separator +
		 * "dbscript"); if (!scriptDirFile.exists()) scriptDirFile.mkdir();
		 

		File migrationDBSourceFile = new File(scriptDirFile.getPath() + separator + "migrationdb.sql");
		tu.overWriteStreamOnFile(migrationDBSourceFile,
				new ByteArrayInputStream(dbscript.getBytes(StandardCharsets.UTF_8)));

		log.info("");
		*/
		
		JSONObject currentartifactConfigObj = getArtifactAccountConfig(artifactaccount);
		String artifactaccountName = (String) currentartifactConfigObj.get("accountname");
		String artifactType = (String) currentartifactConfigObj.get("artifacttype");
		String artifactuserName = (String) currentartifactConfigObj.get("username");
		String artifactPassword = (String) currentartifactConfigObj.get("password");
		String artifactToken = (String) currentartifactConfigObj.get("token");
		
		log.info("artifactaccountName::" + artifactaccountName);
		log.info("artifactType::" + artifactType);
		log.info("artifactuserName::" + artifactuserName);
		log.info("artifactPassword::" + artifactPassword);
		log.info("artifactToken::" + artifactToken);
	
		
		File migrationDBSourceFile = null;
		if(StringUtils.isEmpty(artifactToken)) {
			log.info("cloning github repo by pass ::" + artifactPassword);
			migrationDBSourceFile = Githubprovider.getdbscriptpath(artifactuserName,artifactPassword,dbscript);
		}else {
			log.info("cloning github repo by Token ::" + artifactToken);
			migrationDBSourceFile = Githubprovider.getdbscriptpath(artifactuserName,artifactToken,dbscript);
		}
		
		
		
		JSONObject currentdbConfigObj = getDBAccountConfig(dbaccount);
		String accountName = (String) currentdbConfigObj.get("accountname");
		String dbType = (String) currentdbConfigObj.get("dbtype");
		String userName = (String) currentdbConfigObj.get("username");
		String password = (String) currentdbConfigObj.get("password");
		String dbEndpoint = (String) currentdbConfigObj.get("dbendpoint");
		
		log.info("accountName::" + accountName);
		log.info("dbType::" + dbType);
		log.info("userName::" + userName);
		log.info("password::" + password);
		log.info("dbEndpoint::" + dbEndpoint);
				
		String fullPathOfProviderImplClass = "com.opsmx.spinautodb.interfaces.Provider" + dbType.trim() + "Impl";

		try {

			Provider currentProvideObj = (Provider) Class.forName(fullPathOfProviderImplClass).newInstance();
			currentProvideObj.serviceProviderSetting(migrationDBSourceFile, userName.trim(), password.trim(), dbEndpoint.trim());

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.info("");
			throw new RuntimeException("error ",e);
		} 
		

	}

	public JSONObject getDBAccountConfig(String dbAccountName) {

		log.info("");

		JSONParser parser = new JSONParser();
		JSONObject dbConfigRootObj = new JSONObject();
		JSONObject dbConfigActualObj = new JSONObject();

		StringBuilder contentBuilder = new StringBuilder();
		//try (Stream<String> stream = Files.lines(Paths.get("/home/opsmx/lalit/work/opsmx/spinautodb/test/dbaccounts"),
		try (Stream<String> stream = Files.lines(Paths.get("/home/spinautodb/opsmx/db/config"),
				StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			dbConfigRootObj = (JSONObject) parser.parse(contentBuilder.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		log.info("");

		JSONArray dbaccounts = (JSONArray) dbConfigRootObj.get("dbaccounts");

		for (int i = 0; i < dbaccounts.size(); i++) {

			dbConfigActualObj = (JSONObject) dbaccounts.get(i);
			String accountName = (String) dbConfigActualObj.get("accountname");

			if (StringUtils.equalsAnyIgnoreCase(accountName.trim(), dbAccountName.trim()))
				break;

		}
		return dbConfigActualObj;
	}
	
	public JSONObject getArtifactAccountConfig(String artifactAccountName) {

		log.info("");

		JSONParser parser = new JSONParser();
		JSONObject artifactConfigRootObj = new JSONObject();
		JSONObject artifactConfigActualObj = new JSONObject();

		StringBuilder contentBuilder = new StringBuilder();
		//try (Stream<String> stream = Files.lines(Paths.get("/home/opsmx/lalit/work/opsmx/spinautodb/test/dbaccounts"),
		try (Stream<String> stream = Files.lines(Paths.get("/home/spinautodb/opsmx/artifact/config"),
				StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			artifactConfigRootObj = (JSONObject) parser.parse(contentBuilder.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		log.info("");

		JSONArray artifactaccounts = (JSONArray) artifactConfigRootObj.get("artifactaccounts");

		for (int i = 0; i < artifactaccounts.size(); i++) {

			artifactConfigActualObj = (JSONObject) artifactaccounts.get(i);
			String accountName = (String) artifactConfigActualObj.get("accountname");

			if (StringUtils.equalsAnyIgnoreCase(accountName.trim(), artifactAccountName.trim()))
				break;

		}
		return artifactConfigActualObj;
	}
}
