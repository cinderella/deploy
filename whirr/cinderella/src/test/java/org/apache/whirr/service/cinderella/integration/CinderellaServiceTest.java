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

package org.apache.whirr.service.cinderella.integration;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.service.cinderella.CinderellaClusterActionHandler;
import org.apache.whirr.util.BlobCache;
import org.jclouds.predicates.InetSocketAddressConnect;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.HostAndPort;
import com.google.common.primitives.Ints;

/**
 * TODO:
 */
public class CinderellaServiceTest {

   private static ClusterSpec clusterSpec;
   private static ClusterController controller;
   private static Cluster cluster;

   private static int port;
   
   @Before
   public void setUp() throws Exception {
      CompositeConfiguration config = new CompositeConfiguration();
      if (System.getProperty("conf") != null) {
         config.addConfiguration(new PropertiesConfiguration(System.getProperty("conf")));
      }
      config.addConfiguration(new PropertiesConfiguration("whirr-cinderella-test.properties"));
      config.addConfiguration(new PropertiesConfiguration("whirr-cinderella-default.properties"));

      port = Ints
            .tryParse(getPropertyOrThrowReasonableNPE(CinderellaClusterActionHandler.CINDERELLA_PORT, config));

      clusterSpec = ClusterSpec.withTemporaryKeys(config);
      controller = new ClusterController();

      cluster = controller.launchCluster(clusterSpec);
   }

   @SuppressWarnings("unchecked")
   protected static String getPropertyOrThrowReasonableNPE(String propertyKey, Configuration config) {
      return checkNotNull(config.getString(propertyKey), "%s not in %s", propertyKey,
            ImmutableSet.copyOf(config.getKeys()));
   }

   @Test
   public void testCinderella() {
      for (Instance instance : cluster.getInstances()) {
         Assert.assertTrue(new InetSocketAddressConnect().apply(HostAndPort.fromParts(instance.getPublicIp(), port)));
      }
   }

   @AfterClass
   public static void after() throws IOException, InterruptedException {
      if (controller != null) {
         controller.destroyCluster(clusterSpec);
      }

      BlobCache.dropAndCloseAll();
   }
}
