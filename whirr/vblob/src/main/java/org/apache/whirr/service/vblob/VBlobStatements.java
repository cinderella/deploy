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

package org.apache.whirr.service.vblob;

import static org.jclouds.scriptbuilder.domain.Statements.call;
import static org.jclouds.scriptbuilder.domain.Statements.createOrOverwriteFile;
import static org.jclouds.scriptbuilder.domain.Statements.exec;
import static org.jclouds.scriptbuilder.domain.Statements.extractTargzAndFlattenIntoDirectory;

import java.util.Collections;
import java.util.Map;

import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.domain.StatementList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.GsonBuilder;

public class VBlobStatements {

   public static Statement install(VBlobConfig config) {
      return new StatementList(ImmutableSet
            .<Statement> builder()
            .add(call("setupPublicCurl"))
            .add(call("install_node", config.getFormatToNodejsPackage().get("deb").toASCIIString(), config
                  .getFormatToNodejsPackage().get("rpm").toASCIIString()))
            .add(call("install_forever", config.getForeverVersion()))
            .add(extractTargzAndFlattenIntoDirectory(config.getTar(), config.getHome()))
            .add(writeConfigJson(config))
            .add(exec("chown -R " + config.getUser() + " " + config.getHome())).build());
   }
   
   private static Statement writeConfigJson(VBlobConfig config) {
      Map<Object, Object> configJ = ImmutableMap
            .builder()
            .put("drivers",
                  ImmutableList
                        .builder()
                        .add(ImmutableMap.of("fs-1",
                              ImmutableMap.builder().put("type", "fs").put("option", ImmutableMap.of()).build()))
                        .build()).put("port", config.getS3Port()).put("current_driver", "fs-1").put("logtype", "winston")
            .put("logfile", config.getHome() + "/log.txt").put("auth", "s3").put("debug", true).put("account_api", false)
            .put("keyID", config.getAuthorizedAccessKey()).put("secretID",  config.getAuthorizedSecretKey()).build();
      String configJson = new GsonBuilder().setPrettyPrinting().create().toJson(configJ);
      String fileName = config.getHome() + "/config.json";
      return createOrOverwriteFile(fileName, Collections.singleton(configJson));
   }

   public static Statement start(VBlobConfig config) {
      return call("start_vblob", config.getHome(), config.getUser());
   }

   public static Statement stop(VBlobConfig config) {
      return call("stop_vblob", config.getHome(), config.getUser());
   }

   public static Statement cleanup(VBlobConfig config) {
      return new StatementList(ImmutableSet.<Statement> builder()
            .add(call("stop_vblob", config.getHome(), config.getUser()))
            .add(exec("rm -rf " + config.getHome())).build());
   }
}
