/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.whirr.service.cinderella;

import static org.jclouds.scriptbuilder.domain.Statements.call;
import static org.jclouds.scriptbuilder.domain.Statements.createOrOverwriteFile;
import static org.jclouds.scriptbuilder.domain.Statements.exec;
import static org.jclouds.scriptbuilder.domain.Statements.extractTargzAndFlattenIntoDirectory;

import java.util.Map;

import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.domain.StatementList;
import org.jclouds.scriptbuilder.statements.java.InstallJDK;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class CinderellaStatements {

   public static Statement install(CinderellaConfig config) {
      return new StatementList(ImmutableSet
            .<Statement> builder()
            .add(InstallJDK.fromOpenJDK())
            .add(extractTargzAndFlattenIntoDirectory(config.getJettyTar(), config.getHome()))
            .addAll(buildCinderellaWarFromSource(config, config.getHome() + "/webapps/root.war"))
            .add(exec("chown -R " + config.getUser() + " " + config.getHome()))
            .add(exec("mkdir ${HOME}/.cinderella"))
            .add(writeEC2ServiceProperties(config))
            .add(exec("chown -R " + config.getUser() + " ${HOME}/.cinderella")).build());
   }
   
   private static Iterable<Statement> buildCinderellaWarFromSource(CinderellaConfig config, String dest) {
      return ImmutableSet.<Statement> builder()
            .add(extractTargzAndFlattenIntoDirectory(config.getMavenTar(), "/tmp/maven"))
            .add(extractTargzAndFlattenIntoDirectory(config.getTar(), "/tmp/cinderella"))
            .add(extractTargzAndFlattenIntoDirectory(config.getCloudStackTar(), "/tmp/cinderella/cloudbridge"))
            .add(call("install_cinderella", "/tmp/cinderella", "/tmp/maven", dest))
            .add(exec("rm -rf /tmp/cinderella /tmp/maven")).build();
   }
   
   private static Statement writeEC2ServiceProperties(CinderellaConfig config) {
      Map<String, String> configFile = ImmutableMap.<String, String>builder()
            .put("endpoint", config.getVCloudEndpoint().toASCIIString())
            .put("useratorg", config.getVCloudUserAtOrg())
            .put("password", config.getVCloudPassword())
            .put("key", config.getAuthorizedAccessKey() + "=" + config.getAuthorizedSecretKey())
            .build();
            
      return createOrOverwriteFile("${HOME}/.cinderella/ec2-service.properties",
                  ImmutableSet.of(Joiner.on('\n').withKeyValueSeparator("=").join(configFile)));
   }

   public static Statement start(CinderellaConfig config) {
      return call("start_jetty", config.getHome(),  config.getEC2Port() + "", config.getUser());
   }

   public static Statement stop(CinderellaConfig config) {
      return call("stop_jetty", config.getHome(),  config.getEC2Port() + "", config.getUser());
   }

   public static Statement cleanup(CinderellaConfig config) {
      return new StatementList(ImmutableSet.<Statement> builder()
            .add(call("stop_jetty", config.getHome(),  config.getEC2Port() + "", config.getUser()))
            .add(exec("rm -rf ${HOME}/.cinderella " + config.getHome())).build());
   }
}
