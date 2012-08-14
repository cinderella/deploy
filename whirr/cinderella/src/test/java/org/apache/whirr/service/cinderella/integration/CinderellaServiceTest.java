/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.whirr.service.cinderella.integration;

import static org.jclouds.concurrent.MoreExecutors.sameThreadExecutor;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.TestConstants;
import org.apache.whirr.service.cinderella.CinderellaConfig;
import org.apache.whirr.service.cinderella.CommonsConfigurationToCinderellaConfig;
import org.jclouds.ContextBuilder;
import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.ec2.EC2ApiMetadata;
import org.jclouds.ec2.EC2AsyncClient;
import org.jclouds.ec2.EC2Client;
import org.jclouds.rest.RestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Install a cinderella service on the remote machine, and test its EC2 interface.
 */
public class CinderellaServiceTest {
   private ClusterSpec clusterSpec;
   private ClusterController controller;
   private Cluster cluster;
   private CinderellaConfig cinderellaConfig;

   @Before
   public void setUp() throws Exception {
      CompositeConfiguration config = new CompositeConfiguration();
      if (System.getProperty("config") != null) {
         config.addConfiguration(new PropertiesConfiguration(System.getProperty("config")));
      }
      config.addConfiguration(new PropertiesConfiguration("whirr-cinderella-test.properties"));
      config.addConfiguration(new PropertiesConfiguration("whirr-cinderella-default.properties"));
      
      clusterSpec = ClusterSpec.withTemporaryKeys(config);

      cinderellaConfig = new CommonsConfigurationToCinderellaConfig("cinderella", clusterSpec.getClusterUser()).apply(config);
      controller = new ClusterController();

      controller.destroyCluster(clusterSpec);
      
      cluster = controller.launchCluster(clusterSpec);
   }
   
   
   @Test(timeout = TestConstants.ITEST_TIMEOUT)
   public void testEC2Interface() throws Exception {
      for (Instance instance : cluster.getInstances()) {

         RestContext<? extends EC2Client, ? extends EC2AsyncClient> context = ContextBuilder.newBuilder(new EC2ApiMetadata())
                                             .endpoint("http://" + instance.getPublicAddress().getHostAddress() + ":" + cinderellaConfig.getEC2Port() + "/")
                                             .credentials(cinderellaConfig.getAuthorizedAccessKey(), cinderellaConfig.getAuthorizedSecretKey())
                                             .modules(ImmutableSet.<Module>of(new ExecutorServiceModule(sameThreadExecutor(), sameThreadExecutor())))
                                             .build(EC2ApiMetadata.CONTEXT_TOKEN);
         
         try {
            context.getApi().getAMIServices().describeImagesInRegion(null);
         } finally {
            context.close();
         }
      
      }

   }

   @After
   public void tearDown() throws Exception {
      if (controller != null) {
         controller.destroyCluster(clusterSpec);
      }
   }

}
