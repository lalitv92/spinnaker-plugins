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

package com.opsmx.spinautodb.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TerraAppUtil {

	private static final Logger log = LoggerFactory.getLogger(TerraAppUtil.class);

	static final String separator = File.separator;

	public File createDirForPipelineId(String applicaton, String pipeline, String pipelineId) {

		String ApplicationName = applicaton;
		String PipelineName = pipeline;
		String PipelineId = pipelineId;

		String currentUserDir = System.getProperty("user.home");
		String opsmxdir = currentUserDir + separator + ".opsmx";
		log.debug(" opsmx directror :" + opsmxdir);
		File opsmxDirFile = new File(opsmxdir);
		if (!opsmxDirFile.exists())
			opsmxDirFile.mkdir();

		File spinnakerDirFile = new File(opsmxDirFile.getPath() + separator + "spinnaker");
		if (!spinnakerDirFile.exists())
			spinnakerDirFile.mkdir();

		File applicationDirFile = new File(spinnakerDirFile.getPath() + separator + ApplicationName);
		if (!applicationDirFile.exists())
			applicationDirFile.mkdir();

		File pipelineNameDirFile = new File(applicationDirFile.getPath() + separator + PipelineName);
		if (!pipelineNameDirFile.exists())
			pipelineNameDirFile.mkdir();

		File pipelineIdDirFile = new File(pipelineNameDirFile.getPath() + separator + PipelineId);
		if (!pipelineIdDirFile.exists())
			pipelineIdDirFile.mkdir();
		log.debug(" Succesfully created the pipeline directory :" + pipelineIdDirFile.getPath());
		return pipelineIdDirFile;
	}

	public String getDirPathOfPipelineId(String applicaton, String pipeline, String pipelineId) {

		String ApplicationName = "applicationName-" + applicaton;
		String PipelineName = "pipelineName-" + pipeline;
		String PipelineIdName = "pipelineId-" + pipelineId;
		String currentUserDir = System.getProperty("user.home");
		String pipelineIdDir = currentUserDir + separator + ".opsmx" + separator + "spinnaker" + separator
				+ ApplicationName + separator + PipelineName + separator + PipelineIdName;
		log.debug(" pipeline directory :" + pipelineIdDir);
		return pipelineIdDir;
	}

	public void writeStreamOnFile(File file, InputStream stream) {

		boolean append = true;
		boolean autoFlush = true;
		String charset = "UTF-8";

		FileOutputStream fos;
		OutputStreamWriter osw;
		try {
			fos = new FileOutputStream(file, append);
			osw = new OutputStreamWriter(fos, charset);
			BufferedWriter bw = new BufferedWriter(osw);
			PrintWriter pw = new PrintWriter(bw, autoFlush);
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				pw.write(inputLine);
				pw.write("\n");
			}

			in.close();
			pw.close();
			log.info("completed writing in to file :" + file.getPath());
		} catch (Exception e) {
			log.info("Error : resource stream writing ");
			throw new RuntimeException("Error : resource stream writing ", e);
		}
	}

	public void overWriteStreamOnFile(File file, InputStream stream) {

		boolean append = false;
		boolean autoFlush = true;
		String charset = "UTF-8";

		FileOutputStream fos;
		OutputStreamWriter osw;
		try {
			fos = new FileOutputStream(file, append);
			osw = new OutputStreamWriter(fos, charset);
			BufferedWriter bw = new BufferedWriter(osw);
			PrintWriter pw = new PrintWriter(bw, autoFlush);
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				pw.write(inputLine);
				pw.write("\n");
			}
			in.close();
			pw.close();

			log.info("completed writing in to file :" + file.getPath());
		} catch (Exception e) {
			log.info("Error : resource stream over writing ");
			throw new RuntimeException("Error : resource stream over writing ", e);
		}
	}


	public static void main(String... args) {}
}
