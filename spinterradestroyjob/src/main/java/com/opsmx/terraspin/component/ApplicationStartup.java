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

package com.opsmx.terraspin.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.opsmx.terraspin.service.TerraService;
import com.opsmx.terraspin.util.HalConfigUtil;
import com.opsmx.terraspin.util.ProcessUtil;
import com.opsmx.terraspin.util.TerraAppUtil;
import com.opsmx.terraspin.util.ZipUtil;

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
	JSONParser parser = new JSONParser();

	@Value("${application.iscontainer.env}")
	public boolean isContainer;

	public boolean isContainer() {
		return isContainer;
	}

	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {

		String currentUserDir = System.getProperty("user.home");
		String spinPlan = System.getenv("plan");
		String spinGitAccount = System.getenv("gitAccount");
		String spincloudAccount = System.getenv("cloudAccount");
		String spinStateRepo = System.getenv("stateRepo");

		log.info("current user :: " + System.getProperty("user.name") + " and user dir :: "
				+ System.getProperty("user.home"));

		log.info("spinPlan:" + spinPlan);
		log.info("spinGitAccount:" + spinGitAccount);
		log.info("spincloudAccount:" + spincloudAccount);
		log.info("spinStateRepo:" + spinStateRepo);

		String spinStateRepoName = spinStateRepo.trim().split(".git")[0];
		String staterepodir = currentUserDir + "/" + spinStateRepoName;

		TerraAppUtil terraapputil = new TerraAppUtil();
		TerraService terraservice = new TerraService();
		ProcessUtil processutil = new ProcessUtil();
		ZipUtil ziputil = new ZipUtil();

		String opsmxdir = currentUserDir + separator + ".opsmx";
		File opsmxDirFile = new File(opsmxdir);
		if (!opsmxDirFile.exists())
			opsmxDirFile.mkdir();
		 

		File scriptDirFile = new File(opsmxDirFile.getPath() + separator + "script");
		if (!scriptDirFile.exists())
			scriptDirFile.mkdir();

		File terraformApplySource = new File(scriptDirFile.getPath() + separator + "exeTerraformApply.sh");
		terraapputil.overWriteStreamOnFile(terraformApplySource, getClass().getClassLoader()
				.getResourceAsStream(separator + "script" + separator + "exeTerraformApply.sh"));

		File terraformPlanSource = new File(scriptDirFile.getPath() + separator + "exeTerraformPlan.sh");
		terraapputil.overWriteStreamOnFile(terraformPlanSource, getClass().getClassLoader()
				.getResourceAsStream(separator + "script" + separator + "exeTerraformPlan.sh"));

		File terraformOutputSource = new File(scriptDirFile.getPath() + separator + "exeTerraformOutput.sh");
		terraapputil.overWriteStreamOnFile(terraformOutputSource, getClass().getClassLoader()
				.getResourceAsStream(separator + "script" + separator + "exeTerraformOutput.sh"));

		File terraformGitOutputSource = new File(scriptDirFile.getPath() + separator + "exeTerraformGitOutput.sh");
		terraapputil.overWriteStreamOnFile(terraformGitOutputSource, getClass().getClassLoader()
				.getResourceAsStream(separator + "script" + separator + "exeTerraformGitOutput.sh"));

		File terraformDestroySource = new File(scriptDirFile.getPath() + separator + "exeTerraformDestroy.sh");
		terraapputil.overWriteStreamOnFile(terraformDestroySource, getClass().getClassLoader()
				.getResourceAsStream(separator + "script" + separator + "exeTerraformDestroy.sh"));

		File halConfigSource = new File(scriptDirFile.getPath() + separator + "exeHalConfig.sh");
		terraapputil.overWriteStreamOnFile(halConfigSource,
				getClass().getClassLoader().getResourceAsStream(separator + "script" + separator + "exeHalConfig.sh"));

		log.info("In hal config is container env: " + isContainer);
		HalConfigUtil.setHalConfig(halConfig(halConfigSource, isContainer));

		String halConfigString = HalConfigUtil.getHalConfig();
		JSONObject halConfigObject = null;
		try {
			halConfigObject = (JSONObject) parser.parse(halConfigString);
		} catch (ParseException pe) {
			log.info(":: Exception while parsing halconfig object ::" + halConfigString);
			throw new RuntimeException("Hal config Parse error:", pe);
		}

		JSONArray githubArtifactAccounts = (JSONArray) ((JSONObject) ((JSONObject) halConfigObject.get("artifacts"))
				.get("github")).get("accounts");
		JSONObject githubArtifactAccount = null;

		for (int i = 0; i < githubArtifactAccounts.size(); i++) {
			githubArtifactAccount = (JSONObject) githubArtifactAccounts.get(i);
			String githubArtifactaccountName = (String) githubArtifactAccount.get("name");
			if (StringUtils.equalsIgnoreCase(githubArtifactaccountName.trim(), spinGitAccount.trim()))
				break;
		}
		String gitUser = (String) githubArtifactAccount.get("username");
		String gittoken = (String) githubArtifactAccount.get("token");
		String gitPass = (String) githubArtifactAccount.get("password");

		String checkrepopresentcommand = "curl -u GITUSER:GITPASS https://api.github.com/GITUSER/REPONAME";
		String gitclonecommand = "git clone https://GITUSER:GITPASS@github.com/GITUSER/REPONAME";

		if (StringUtils.isNoneEmpty(gitPass)) {
			checkrepopresentcommand = checkrepopresentcommand.replaceAll("GITUSER", gitUser)
					.replaceAll("GITPASS", gitPass).replaceAll("REPONAME", spinStateRepo);
			gitclonecommand = gitclonecommand.replaceAll("GITUSER", gitUser).replaceAll("GITPASS", gitPass)
					.replaceAll("REPONAME", spinStateRepo);
		} else {
			checkrepopresentcommand = checkrepopresentcommand.replaceAll("GITUSER", gitUser)
					.replaceAll("GITPASS", gittoken).replaceAll("REPONAME", spinStateRepo);
			gitclonecommand = gitclonecommand.replaceAll("GITUSER", gitUser).replaceAll("GITPASS", gittoken)
					.replaceAll("REPONAME", spinStateRepo);
		}

		boolean isrepopresent = processutil.runcommand(checkrepopresentcommand);
		log.info("isrepopresent -- " + isrepopresent);
		if (isrepopresent) {
			boolean isgitcloned = processutil.runcommandwithindir(gitclonecommand, currentUserDir);
			log.info("isgitcloned -- " + isgitcloned);
			if (isgitcloned) {

				String source1 = "/home/terraspin/" + spinStateRepoName + "/.git";
				File srcDir1 = new File(source1);
				String destination1 = "/home/terraspin/extra";
				File destDir1 = new File(destination1);

				try {
					FileUtils.copyDirectoryToDirectory(srcDir1, destDir1);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				/*
				 * String source4 = staterepodir + "/.terraform.d"; File srcDir4 = new
				 * File(source4); String destination4 = "/home/terraspin"; File destDir4 = new
				 * File(destination4);
				 * 
				 * try { FileUtils.copyDirectoryToDirectory(srcDir4, destDir4);
				 * System.out.println("done!!"); } catch (IOException e) { e.printStackTrace();
				 * }
				 */

				//String clonerepopipelinedir = "/home/terraspin/" + spinStateRepoName + "/pipelineId-spinPipeId";
				String zipfilesrc = "/home/terraspin/" + spinStateRepoName + "/pipelineId-spinPipeId.zip";
				
				String extrapipelineidsrc =  "/home/terraspin/extra/pipelineId-spinPipeId";
				File extrapipelineidsrcdir = new File(extrapipelineidsrc);
				if (!extrapipelineidsrcdir.exists())
					extrapipelineidsrcdir.mkdir();

				
				
				try {
					ziputil.unzip(zipfilesrc, extrapipelineidsrc);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				boolean ischangemod = processutil.runcommand("chmod 777 -R ~/extra");
				log.info("changing mod of file status :: " + ischangemod);
				
				terraservice.destroyStart(extrapipelineidsrc, "");

				JSONObject destroystatusobj = terraservice.destroyStatus("", "", "", "");
				log.info("current destroystatusobj status :: " + destroystatusobj);
				String applystatusstr = (String) destroystatusobj.get("status");

				if (StringUtils.equalsIgnoreCase("SUCCESS", applystatusstr)) {
					
					File staterepoDir = new File(staterepodir);

					try {
						FileUtils.cleanDirectory(staterepoDir);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String source2 = "/home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId";
					File srcDir2 = new File(source2);
					String destination2 = staterepodir;
					File destDir2 = new File(destination2);

					try {
						FileUtils.copyDirectoryToDirectory(srcDir2, destDir2);
						System.out.println("done!!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					String source3 = "/home/terraspin/extra/.git";
					File srcDir3 = new File(source3);
					String destination3 = staterepodir;
					File destDir3 = new File(destination3);

					try {
						FileUtils.copyDirectoryToDirectory(srcDir3, destDir3);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					
					String zippath = staterepodir + "/pipelineId-spinPipeId.zip";
					String srczippath = staterepodir + "/pipelineId-spinPipeId";
					try {
						ziputil.zipDirectory(srczippath, zippath);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					////////////////////
					
					String gitconfigusernamecommand = "git config --global user.name \"OpsMx\"";
					boolean isgitconfigusernamecommandsuccess = processutil
							.runcommandwithindir(gitconfigusernamecommand, staterepodir);
					log.info("isgitconfigusernamecommandsuccess : " + isgitconfigusernamecommandsuccess);

					String gitconfiguseremailcommand = "git config --global user.email \"Team@OpsMx.com\"";
					boolean isconfiguseremailcommandsuccess = processutil.runcommandwithindir(gitconfiguseremailcommand,
							staterepodir);
					log.info("isconfiguseremailcommandsuccess : " + isconfiguseremailcommandsuccess);

					String gitaddcommand = "git add .";
					boolean isgitaddcommandsuccess = processutil.runcommandwithindir(gitaddcommand, staterepodir);

					if (isgitaddcommandsuccess) {
						String gitcommitcommand = "git commit -m \"adding spinterra apply state\"";
						// String gitcommitcommand = "git commit";
						boolean isgitcommitcommandsuccess = processutil.runcommandwithindir(gitcommitcommand,
								staterepodir);

						if (isgitcommitcommandsuccess) {
							String gitpushcommand = "git push -u origin master";
							boolean isgitpushcommandsuccess = processutil.runcommandwithindir(gitpushcommand,
									staterepodir);

							if (isgitpushcommandsuccess) {
								log.info("gitpushcommand got success : ");
							} else {
								log.info("isgitpushcommandnotsuccess : ");
								log.info("error : " + processutil.getStatusRootObj());
							}
						} else {
							log.info("isgitcommitcommandnotsuccess : ");
							log.info("error : " + processutil.getStatusRootObj());
						}
					} else {
						log.info("isgitaddcommandnotsuccess : ");
						log.info("error : " + processutil.getStatusRootObj());
					}
					
					
					
					//////////////////
					

				} else {
					log.info("----- error while executing spinterra apply ------");
					log.info("error : " + processutil.getStatusRootObj());
				}

			} else {
				log.info("isgitcloned not hapenning : ");
				log.info("error : " + processutil.getStatusRootObj());
			}

		} else {
			log.info("on github account repo is not present from where will pulling terraform plan state");
			log.info("error : " + processutil.getStatusRootObj());
		}

		// plan status
		/*
		 * JSONObject planstatusobj = new JSONObject(); String planstatusstr = new
		 * String();
		 * 
		 * do { planstatusobj = terraservice.planStatus(""); planstatusstr = (String)
		 * planstatusobj.get("status");
		 * 
		 * } while (StringUtils.equalsIgnoreCase("TERMINAL", planstatusstr) ||
		 * StringUtils.equalsIgnoreCase("SUCCESS", planstatusstr));
		 */

	}

	@SuppressWarnings("unchecked")
	public String halConfig(File file, boolean isContainerEnv) {
		log.info("Hal config script path : " + file.getPath());
		JSONParser parser = new JSONParser();
		JSONObject halConfigRootObj = new JSONObject();
		Process exec;
		if (isContainerEnv) {

			StringBuilder contentBuilder = new StringBuilder();
			try (Stream<String> stream = Files.lines(Paths.get("/home/terraspin/opsmx/hal/halconfig"),
					StandardCharsets.UTF_8))
			// try (Stream<String> stream = Files.lines(
			// Paths.get("/home/opsmx/lalit/work/opsmx/Terraform-spinnaker/TerraSpin/container/halconfig"),
			// StandardCharsets.UTF_8))
			{
				stream.forEach(s -> contentBuilder.append(s).append("\n"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				halConfigRootObj = (JSONObject) parser.parse(contentBuilder.toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			try {
				exec = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "sh " + file.getPath() });
				exec.waitFor();

				BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
				String line = "";
				String tempLine = "";
				while ((tempLine = reader.readLine()) != null) {
					line = line + tempLine.trim() + System.lineSeparator();
				}

				BufferedReader errorReader = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
				String line2 = "";
				String tempLine2 = "";
				while ((tempLine2 = errorReader.readLine()) != null) {
					line2 = line2 + tempLine2.trim() + System.lineSeparator();
				}

				reader.close();
				errorReader.close();

				if (exec.exitValue() == 0) {
					int startIndex = line.indexOf('{');
					String halConfigString = line.substring(startIndex);
					halConfigRootObj = (JSONObject) parser.parse(halConfigString);
					log.info("Successfully parsed hal config ");

				} else {
					halConfigRootObj.put("error", line2);
					log.info("Error while fetching hal config please make sure you hal daemaon is running");
				}

			} catch (IOException | InterruptedException | ParseException e) {
				log.info("Malformed Hal config Error :" + e.getMessage());
				throw new RuntimeException("Malformed Hal config data", e);
			}

		}
		log.info("hal config Object :::  " + halConfigRootObj);
		return halConfigRootObj.toJSONString();
	}

}