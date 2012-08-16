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

import java.net.URI;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.service.cinderella.CinderellaConfig.Builder;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

public class CommonsConfigurationToCinderellaConfig implements Function<Configuration, CinderellaConfig> {
   private final String prefix;
   private final String user;

   public CommonsConfigurationToCinderellaConfig(String prefix, String user) {
      this.prefix = checkNotNull(prefix, "prefix");
      this.user = checkNotNull(user, "user");
   }

   @SuppressWarnings("unchecked")
   static String getPropertyOrThrowReasonableNPE(String propertyKey, Configuration config) {
      return checkNotNull(config.getString(propertyKey), "%s not in %s", propertyKey,
            ImmutableSet.copyOf(config.getKeys()));
   }

   @Override
   public CinderellaConfig apply(Configuration input) {
      Builder builder = CinderellaConfig.builder();
      builder.user(user);
      builder.home(getPropertyOrThrowReasonableNPE(prefix + ".home", input));
      builder.ec2Port(Integer.parseInt(getPropertyOrThrowReasonableNPE(prefix + ".ec2.port", input)));
      builder.ec2Version(getPropertyOrThrowReasonableNPE(prefix + ".ec2.version", input));
      builder.authorizedAccessKey(getPropertyOrThrowReasonableNPE(prefix + ".auth.access-key", input));
      builder.authorizedSecretKey(getPropertyOrThrowReasonableNPE(prefix + ".auth.secret-key", input));
      builder.vCloudEndpoint(URI.create(getPropertyOrThrowReasonableNPE(prefix + ".vcloud.endpoint", input)));
      builder.vCloudUserAtOrg(getPropertyOrThrowReasonableNPE(prefix + ".vcloud.useratorg", input));
      builder.vCloudPassword(getPropertyOrThrowReasonableNPE(prefix + ".vcloud.password", input));
      builder.tar(URI.create(getPropertyOrThrowReasonableNPE(prefix + ".tar.url", input)));
      builder.cloudStackTar(URI.create(getPropertyOrThrowReasonableNPE(prefix + ".cloudstack.tar.url", input)));
      builder.mavenTar(URI.create(getPropertyOrThrowReasonableNPE(prefix + ".maven.tar.url", input)));
      builder.jettyTar(URI.create(getPropertyOrThrowReasonableNPE(prefix + ".jetty.tar.url", input)));
      return builder.build();
   }
}

