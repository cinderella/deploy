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
package org.apache.whirr.service.vblob.integration;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.concurrent.MoreExecutors.sameThreadExecutor;
import static org.jclouds.s3.reference.S3Constants.PROPERTY_S3_VIRTUAL_HOST_BUCKETS;
import static org.jclouds.util.Strings2.toStringAndClose;

import java.util.Properties;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.TestConstants;
import org.apache.whirr.service.vblob.VBlobHandler;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.domain.Credentials;
import org.jclouds.s3.S3ApiMetadata;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.google.inject.Module;

/**
 * Install a vBlob service on the remote machine, and test its S3 interface.
 */
public class VBlobServiceTest {
   private ClusterSpec clusterSpec;
   private ClusterController controller;
   private Cluster cluster;
   private Credentials s3Credentials;
   private int s3Port;

   @Before
   public void setUp() throws Exception {
      CompositeConfiguration config = new CompositeConfiguration();
      if (System.getProperty("config") != null) {
         config.addConfiguration(new PropertiesConfiguration(System.getProperty("config")));
      }
      config.addConfiguration(new PropertiesConfiguration("whirr-vblob-test.properties"));
      config.addConfiguration(new PropertiesConfiguration("whirr-vblob-default.properties"));
      
      s3Credentials = new Credentials(getPropertyOrThrowReasonableNPE(VBlobHandler.KEY_ID, config),
                                      getPropertyOrThrowReasonableNPE(VBlobHandler.SECRET_ID, config));

      s3Port = Ints.tryParse(getPropertyOrThrowReasonableNPE(VBlobHandler.PORT, config));

      clusterSpec = ClusterSpec.withTemporaryKeys(config);
      controller = new ClusterController();
      cluster = controller.launchCluster(clusterSpec);
   }
   
   @Test(timeout = TestConstants.ITEST_TIMEOUT)
   public void testS3Interface() throws Exception {
      for (Instance instance : cluster.getInstances()) {

         Properties overrides = new Properties();
         overrides.setProperty(PROPERTY_S3_VIRTUAL_HOST_BUCKETS, "false");
         
         BlobStore blobstore = ContextBuilder.newBuilder(new S3ApiMetadata())
                                             .endpoint("http://" + instance.getPublicAddress().getHostAddress() + ":" + s3Port)
                                             .credentials(s3Credentials.identity, s3Credentials.credential)
                                             .overrides(overrides)
                                             .modules(ImmutableSet.<Module>of(new ExecutorServiceModule(sameThreadExecutor(), sameThreadExecutor())))
                                             .buildView(BlobStoreContext.class).getBlobStore();
         try {
            blobstore.createContainerInLocation(null, "bucket");
            blobstore.putBlob("bucket", blobstore.blobBuilder("blob").payload("hello world").build());
            Blob blob = blobstore.getBlob("bucket", "blob");
            Assert.assertEquals("hello world", toStringAndClose(blob.getPayload().getInput()));
         } finally {
            blobstore.getContext().close();
         }
      
      }

   }

   @After
   public void tearDown() throws Exception {
      if (controller != null) {
         controller.destroyCluster(clusterSpec);
      }
   }

   @SuppressWarnings("unchecked")
   protected String getPropertyOrThrowReasonableNPE(String propertyKey, Configuration config ) {
      return checkNotNull(config.getString(propertyKey), "%s not in %s", propertyKey,
               ImmutableSet.copyOf(config.getKeys()));
   }

}
