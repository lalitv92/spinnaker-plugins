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

package com.opsmx.spinautodb.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProvidermysqlImpl implements Provider {

	private static final Logger log = LoggerFactory.getLogger(ProviderpostgresImpl.class);

	@Override
	public void serviceProviderSetting(File sqlscriptpath, String username, String password, String dbendpoint) {

		log.info("::::  In serviceProviderSetting method of ProvidermysqlImpl class :::: \n");

		// Registering the Driver
		try {
			DriverManager.registerDriver(new org.postgresql.Driver());

			// Getting the connection
			// String mysqlUrl = "jdbc:postgresql://3654.8563.52125.40:545445432/try1" + dbendpoint;
			String mysqlUrl = "jdbc:postgresql://" + dbendpoint;
			Connection con = DriverManager.getConnection(mysqlUrl, username, password);
			System.out.println("Connection established......");
			// Initialize the script runner
			ScriptRunner sr = new ScriptRunner(con);
			// Creating a reader object
			Reader reader = new BufferedReader(new FileReader(sqlscriptpath));
			// Running the script
			sr.runScript(reader);

			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		ProviderpostgresImpl iml = new ProviderpostgresImpl();
	}
}
