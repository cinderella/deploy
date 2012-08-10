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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.whirr.RolePredicates.role;
import static org.jclouds.compute.util.ComputeServiceUtils.extractTargzIntoDirectory;
import static org.jclouds.scriptbuilder.domain.Statements.call;
import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import org.apache.whirr.service.FirewallManager.Rule;
import org.jclouds.scriptbuilder.statements.java.InstallJDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;

public class CinderellaClusterActionHandler extends ClusterActionHandlerSupport {

   private static final Logger LOG = LoggerFactory.getLogger(CinderellaClusterActionHandler.class);

   public final static String CINDERELLA_ROLE = "cinderella";

   public final static String CINDERELLA_DEFAULT_CONFIG = "whirr-cinderella-default.properties";

   public final static String CINDERELLA_HOME = "/usr/local/cinderella";

   public final static String CINDERELLA_JETTY_TARBALL_URL = "whirr.cinderella.jetty.tarball";
   public final static String CINDERELLA_PORT = "whirr.cinderella.port";

   @Override
   public String getRole() {
      return CINDERELLA_ROLE;
   }

   @Override
   protected void beforeBootstrap(ClusterActionEvent event) throws IOException {
      Configuration config = getConfiguration(event.getClusterSpec(), CINDERELLA_DEFAULT_CONFIG);

      // Validate the config
      getPropertyOrThrowReasonableNPE(CinderellaClusterActionHandler.CINDERELLA_PORT, config);
      String cinderellaTarball = getPropertyOrThrowReasonableNPE(
            CinderellaClusterActionHandler.CINDERELLA_JETTY_TARBALL_URL, config);

      addStatement(event, InstallJDK.fromOpenJDK());
      addStatement(event, extractTargzIntoDirectory(URI.create(cinderellaTarball), "/tmp"));
      addStatement(event, exec("{md} " + CINDERELLA_HOME));
      addStatement(event, exec("mv /tmp/jetty-*/* " + CINDERELLA_HOME));
   }

   @Override
   protected void beforeConfigure(ClusterActionEvent event) throws IOException {
      LOG.info("Configure Cinderella");

      ClusterSpec clusterSpec = event.getClusterSpec();
      Configuration config = getConfiguration(clusterSpec, CINDERELLA_DEFAULT_CONFIG);

      int port = Ints.tryParse(getPropertyOrThrowReasonableNPE(CinderellaClusterActionHandler.CINDERELLA_PORT, config));

      // Open up Jetty port
      event.getFirewallManager().addRule(Rule.create().destination(role(CINDERELLA_ROLE)).port(port));
   }

   @Override
   protected void beforeStart(ClusterActionEvent event) throws IOException {
      ClusterSpec clusterSpec = event.getClusterSpec();
      Configuration config = getConfiguration(clusterSpec, CINDERELLA_DEFAULT_CONFIG);

      String port = getPropertyOrThrowReasonableNPE(CinderellaClusterActionHandler.CINDERELLA_PORT, config);

      LOG.info("Starting up Cinderella");

      addStatement(event, call("start_jetty", CINDERELLA_HOME, port));
   }

   @Override
   protected void beforeStop(ClusterActionEvent event) throws IOException {
      LOG.info("Stopping Cinderella");
      addStatement(event, call("stop_jetty", CINDERELLA_HOME));
   }

   @SuppressWarnings("unchecked")
   protected static String getPropertyOrThrowReasonableNPE(String propertyKey, Configuration config) {
      return checkNotNull(config.getString(propertyKey), "%s not in %s", propertyKey,
            ImmutableSet.copyOf(config.getKeys()));
   }
}
