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

import static org.apache.whirr.RolePredicates.role;
import static org.apache.whirr.service.vblob.VBlobStatements.cleanup;
import static org.apache.whirr.service.vblob.VBlobStatements.install;
import static org.apache.whirr.service.vblob.VBlobStatements.start;
import static org.apache.whirr.service.vblob.VBlobStatements.stop;

import java.io.IOException;

import org.apache.whirr.Cluster;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import org.apache.whirr.service.FirewallManager.Rule;

public class VBlobHandler extends ClusterActionHandlerSupport {

   @Override
   public String getRole() {
      return "vblob";
   }

   @Override
   protected void beforeBootstrap(ClusterActionEvent event) throws IOException {
      event.getStatementBuilder().addStatement(install(toConfig(event)));
   }

   protected VBlobConfig toConfig(ClusterActionEvent event) throws IOException {
      return new CommonsConfigurationToVBlobConfig(getRole(), event.getClusterSpec().getClusterUser())
            .apply(getConfiguration(event.getClusterSpec(), "whirr-" + getRole() + "-default.properties"));
   }

   @Override
   protected void beforeConfigure(ClusterActionEvent event) throws IOException {
      VBlobConfig config = toConfig(event);
      Cluster cluster = event.getCluster();
      event.getFirewallManager().addRule(
            Rule.create().destination(cluster.getInstancesMatching(role(getRole()))).port(config.getS3Port()));
   }

   @Override
   protected void beforeStart(ClusterActionEvent event) throws IOException {
      event.getStatementBuilder().addStatement(start(toConfig(event)));
   }

   @Override
   protected void beforeStop(ClusterActionEvent event) throws IOException {
      event.getStatementBuilder().addStatement(stop(toConfig(event)));
   }

   @Override
   protected void beforeCleanup(ClusterActionEvent event) throws IOException {
      event.getStatementBuilder().addStatement(cleanup(toConfig(event)));
   }

}
