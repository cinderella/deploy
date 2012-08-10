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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.whirr.RolePredicates.role;
import static org.jclouds.scriptbuilder.domain.Statements.call;
import static org.jclouds.scriptbuilder.domain.Statements.createOrOverwriteFile;
import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import org.apache.whirr.service.FirewallManager.Rule;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.statements.git.CloneGitRepo;
import org.jclouds.scriptbuilder.statements.git.InstallGit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.google.gson.GsonBuilder;

public class VBlobHandler extends ClusterActionHandlerSupport {

   public static final String ROLE = "vblob";

   public static final String VBLOB_DEFAULT_CONFIG = "whirr-vblob-default.properties";
   
   public static final String NODE_DEB = ROLE + ".node.deb.url";
   public static final String NODE_RPM = ROLE + ".node.rpm.url";
   public static final String FOREVER_VERSION = ROLE + ".forever.version";
   
   public static final String HOME = ROLE + ".home";
   public static final String REPO = ROLE + ".repo";
   public static final String BRANCH = ROLE + ".branch";
   public static final String TAG = ROLE + ".tag";
   
   public static final String KEY_ID = ROLE + ".auth.s3.keyID";
   public static final String SECRET_ID = ROLE + ".auth.s3.secretID";

   public static final String PORT =  ROLE + ".port";

   @Override
   public String getRole() {
      return ROLE;
   }

   @Override
   protected void beforeBootstrap(ClusterActionEvent event) throws IOException {
      Configuration config = getConfiguration(event.getClusterSpec(), VBLOB_DEFAULT_CONFIG);

      addStatement(event, call("setupPublicCurl"));
      addStatement(event, call("install_node", getPropertyOrThrowReasonableNPE(NODE_DEB, event)
                                             , getPropertyOrThrowReasonableNPE(NODE_RPM, event)));
      addStatement(event, call("install_forever", getPropertyOrThrowReasonableNPE(FOREVER_VERSION, event)));

      addStatement(event, new InstallGit());
      String vBlobHome = getPropertyOrThrowReasonableNPE(HOME, event);
      addStatement(event, CloneGitRepo.builder()
                                      .repository(getPropertyOrThrowReasonableNPE(REPO, event))
                                      .branch(config.getString(BRANCH, null))
                                      .tag(config.getString(TAG, null))
                                      .directory(vBlobHome).build());

   }

   @SuppressWarnings("unchecked")
   protected String getPropertyOrThrowReasonableNPE(String propertyKey, ClusterActionEvent event) throws IOException {
      Configuration config = getConfiguration(event.getClusterSpec(), VBLOB_DEFAULT_CONFIG);
      return checkNotNull(config.getString(propertyKey), "%s not in %s", propertyKey,
               ImmutableSet.copyOf(config.getKeys()));
   }

   @Override
   protected void beforeConfigure(ClusterActionEvent event) throws IOException {
      ClusterSpec spec = event.getClusterSpec();
      Integer port = Ints.tryParse(getPropertyOrThrowReasonableNPE(PORT, event));
      String vBlobHome = getPropertyOrThrowReasonableNPE(HOME, event);
      String keyId = getPropertyOrThrowReasonableNPE(KEY_ID, event);
      String secretID = getPropertyOrThrowReasonableNPE(SECRET_ID, event);

      Statement writeConfigJson = writeConfigJsonForPortAccessAndSecretToDirectory(port, keyId, secretID, vBlobHome);
      
      addStatement(event, writeConfigJson);
      addStatement(event, exec("chown -R " + spec.getClusterUser() + " " + vBlobHome));
      
      Cluster cluster = event.getCluster();
      event.getFirewallManager().addRule(
               Rule.create().destination(cluster.getInstancesMatching(role(ROLE))).port(port));
   }

   protected Statement writeConfigJsonForPortAccessAndSecretToDirectory(int port, String keyId, String secretID, String vBlobHome) {
      Map<Object, Object> config = ImmutableMap.builder()
               .put("drivers", ImmutableList.builder()
                                            .add(ImmutableMap.of("fs-1", ImmutableMap.builder()
                                                                                     .put("type", "fs")
                                                                                     .put("option", ImmutableMap.of())
                                                                                     .build()))
                                            .build())
               .put("port", port)
               .put("current_driver", "fs-1")
               .put("logtype", "winston")
               .put("logfile", vBlobHome + "/log.txt") 
               .put("auth", "s3")
               .put("debug", true)
               .put("account_api", false)
               .put("keyID", keyId)
               .put("secretID", secretID).build();
      String configJson = new GsonBuilder().setPrettyPrinting().create().toJson(config);
      String fileName = vBlobHome + "/config.json";
      return createOrOverwriteFile(fileName, Collections.singleton(configJson));
   }

   @Override
   protected void beforeStart(ClusterActionEvent event) throws IOException {
      String vBlobHome = getPropertyOrThrowReasonableNPE(HOME, event);
      addStatement(event, call("start_vblob", vBlobHome, event.getClusterSpec().getClusterUser()));
   }

   @Override
   protected void beforeStop(ClusterActionEvent event) throws IOException {
      String vBlobHome = getPropertyOrThrowReasonableNPE(HOME, event);
      addStatement(event, call("stop_vblob", vBlobHome, event.getClusterSpec().getClusterUser()));
   }

   @Override
   protected void beforeCleanup(ClusterActionEvent event) throws IOException {
      String vBlobHome = getPropertyOrThrowReasonableNPE(HOME, event);
      addStatement(event, call("stop_vblob", vBlobHome, event.getClusterSpec().getClusterUser()));
      addStatement(event, call("cleanup_vblob", vBlobHome, event.getClusterSpec().getClusterUser()));
   }
}
