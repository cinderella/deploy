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

import java.net.URI;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.service.vblob.VBlobConfig.Builder;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class CommonsConfigurationToVBlobConfig implements Function<Configuration, VBlobConfig> {
   private final String prefix;
   private final String user;

   public CommonsConfigurationToVBlobConfig(String prefix, String user) {
      this.prefix = checkNotNull(prefix, "prefix");
      this.user = checkNotNull(user, "user");
   }

   @SuppressWarnings("unchecked")
   static String getPropertyOrThrowReasonableNPE(String propertyKey, Configuration config) {
      return checkNotNull(config.getString(propertyKey), "%s not in %s", propertyKey,
            ImmutableSet.copyOf(config.getKeys()));
   }

   @Override
   public VBlobConfig apply(Configuration input) {
      Builder builder = VBlobConfig.builder();
      builder.user(user);
      builder.home(getPropertyOrThrowReasonableNPE(prefix + ".home", input));
      builder.s3Port(Integer.parseInt(getPropertyOrThrowReasonableNPE(prefix + ".s3port", input)));
      builder.authorizedAccessKey(getPropertyOrThrowReasonableNPE(prefix + ".auth.access-key", input));
      builder.authorizedSecretKey(getPropertyOrThrowReasonableNPE(prefix + ".auth.secret-key", input));
      builder.tar(URI.create(getPropertyOrThrowReasonableNPE(prefix + ".tar.url", input)));
      builder.formatToNodejsPackage(ImmutableMap.<String, URI> builder()
            .put("deb", URI.create(getPropertyOrThrowReasonableNPE(prefix + ".node.deb.url", input)))
            .put("rpm", URI.create(getPropertyOrThrowReasonableNPE(prefix + ".node.rpm.url", input))).build());
      builder.foreverVersion(getPropertyOrThrowReasonableNPE(prefix + ".forever.version", input));
      return builder.build();
   }
}

