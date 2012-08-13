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
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
 * Configuration required to install and configure vBlob properly
 * 
 * @author Adrian Cole
 */
public class VBlobConfig {

   public static Builder builder() {
      return new Builder();
   }

   public Builder toBuilder() {
      return new Builder().fromVBlobConfig(this);
   }

   public static class Builder {
      private String user = "vblob";
      private String home = "/usr/local/vblob";
      private int s3Port = 9981;
      private String authorizedAccessKey = "MvndHwA4e6dgaGV23L94";
      private String authorizedSecretKey = "A50GS9tj2DLXRln4rf1K+A/CSjmAbBGw0H5yul6s";
      private URI tar = URI.create("https://github.com/cloudfoundry/vblob/tarball/master");
      private ImmutableMap.Builder<String, URI> formatToNodejsPackage = ImmutableMap
            .<String, URI> builder()
            .put("deb", URI.create("https://raw.github.com/cinderella/deploy/master/debs/nodejs-0.6.10_amd64.deb"))
            .put("rpm",
                  URI.create("https://raw.github.com/cinderella/deploy/master/rpms/x86_64/nodejs-0.6.10-1.x86_64.rpm"));
      private String foreverVersion = "0.9.2";
      
      /**
       * @see VBlobConfig#getUser()
       */
      public Builder user(String user) {
         this.user = user;
         return this;
      }
      
      /**
       * @see VBlobConfig#getHome()
       */
      public Builder home(String home) {
         this.home = home;
         return this;
      }

      /**
       * @see VBlobConfig#getS3Port()
       */
      public Builder s3Port(int s3Port) {
         this.s3Port = s3Port;
         return this;
      }

      /**
       * @see VBlobConfig#getAuthorizedAccessKey()
       */
      public Builder authorizedAccessKey(String authorizedAccessKey) {
         this.authorizedAccessKey = authorizedAccessKey;
         return this;
      }

      /**
       * @see VBlobConfig#getAuthorizedSecretKey()
       */
      public Builder authorizedSecretKey(String authorizedSecretKey) {
         this.authorizedSecretKey = authorizedSecretKey;
         return this;
      }

      /**
       * @see VBlobConfig#getTar()
       */
      public Builder tar(URI tar) {
         this.tar = tar;
         return this;
      }

      /**
       * @see VBlobConfig#getFormatToNodejsPackage()
       */
      public Builder formatToNodejsPackage(Map<String, URI> formatToNodejsPackage) {
         this.formatToNodejsPackage = ImmutableMap.<String, URI> builder();
         this.formatToNodejsPackage.putAll(checkNotNull(formatToNodejsPackage, "formatToNodejsPackage"));
         return this;
      }

      /**
       * @see VBlobConfig#getFormatToNodejsPackage()
       */
      public Builder addFormatToNodejsPackage(String format, URI nodejsPackage) {
         this.formatToNodejsPackage.put(checkNotNull(format, "format"),
               checkNotNull(nodejsPackage, "nodejsPackage of format %s", format));
         return this;
      }

      /**
       * @see VBlobConfig#getForeverVersion()
       */
      public Builder foreverVersion(String foreverVersion) {
         this.foreverVersion = foreverVersion;
         return this;
      }

      public VBlobConfig build() {
         return new VBlobConfig(user, home, s3Port, authorizedAccessKey, authorizedSecretKey, tar, formatToNodejsPackage.build(), foreverVersion);
      }

      public Builder fromVBlobConfig(VBlobConfig in) {
         return this.user(in.user)
                    .home(in.home)
                    .s3Port(in.s3Port)
                    .authorizedAccessKey(in.authorizedAccessKey)
                    .authorizedSecretKey(in.authorizedSecretKey)
                    .tar(in.tar)
                    .formatToNodejsPackage(in.formatToNodejsPackage)
                    .foreverVersion(in.foreverVersion);
      }
   }

   private final String user;
   private final String home;
   private final int s3Port;
   private final String authorizedAccessKey;
   private final String authorizedSecretKey;
   private final URI tar;
   private final Map<String, URI> formatToNodejsPackage;
   private final String foreverVersion;

   protected VBlobConfig(String user, String home, int s3Port, String authorizedAccessKey, String authorizedSecretKey, URI tar,
         Map<String, URI> formatToNodejsPackage, String foreverVersion) {
      this.user = checkNotNull(user, "user");
      this.home = checkNotNull(home, "home");
      this.s3Port = checkNotNull(s3Port, "s3Port");
      this.authorizedAccessKey = checkNotNull(authorizedAccessKey, "authorizedAccessKey");
      this.authorizedSecretKey = checkNotNull(authorizedSecretKey, "authorizedSecretKey");
      this.tar = checkNotNull(tar, "tar");
      this.formatToNodejsPackage = checkNotNull(formatToNodejsPackage, "formatToNodejsPackage");
      this.foreverVersion = checkNotNull(foreverVersion, "foreverVersion");
   }
   
   /**
    * User running the service
    */
   public String getUser() {
      return user;
   }
   
   /**
    * Fully qualified path under which we install vBlob
    */
   public String getHome() {
      return home;
   }

   /**
    * Port to listen to connections on (default 9981)
    */
   public int getS3Port() {
      return s3Port;
   }

   /**
    * The Amazon S3-compatible Access Key that this service will use when
    * authenticating requests.
    */
   public String getAuthorizedAccessKey() {
      return authorizedAccessKey;
   }

   /**
    * The secret key corresponding to {@link #getAuthorizedAccessKey()}
    */
   public String getAuthorizedSecretKey() {
      return authorizedSecretKey;
   }

   /**
    * location to install vBlob from.
    */
   public URI getTar() {
      return tar;
   }

   /**
    * vBlob is a nodejs application. This includes a mapping of package manager
    * formats to a remote location of the nodejs package which can support the
    * current version of vBlob.
    * 
    * ex.
    * 
    * <pre>
    *       deb -> https://raw.github.com/cinderella/deploy/master/debs/nodejs-0.6.10_amd64.deb
    *       rpm -> https://raw.github.com/cinderella/deploy/master/rpms/x86_64/nodejs-0.6.10-1.x86_64.rpm
    * </pre>
    */
   public Map<String, URI> getFormatToNodejsPackage() {
      return formatToNodejsPackage;
   }

   /**
    * The vBlob process is managed by <a
    * href="https://github.com/nodejitsu/forever/">forever</a>. This determines
    * the version of forever that will be installed via <a
    * href="https://npmjs.org/">npm</a>
    * 
    * <h4>Note</h4>
    * 
    * This version is sensitive to the version of node. For example, version
    * {@code 0.9.2} is compatible with node {@code 0.6.10}
    */
   public String getForeverVersion() {
      return foreverVersion;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode() {
      return Objects.hashCode(user, home, s3Port, authorizedAccessKey, tar, formatToNodejsPackage,
            foreverVersion);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      VBlobConfig other = VBlobConfig.class.cast(obj);
      return Objects.equal(this.user, other.user) && Objects.equal(this.home, other.home)
            && Objects.equal(this.s3Port, other.s3Port)
            && Objects.equal(this.authorizedAccessKey, other.authorizedAccessKey)
            && Objects.equal(this.tar, other.tar)
            && Objects.equal(this.formatToNodejsPackage, other.formatToNodejsPackage)
            && Objects.equal(this.foreverVersion, other.foreverVersion);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      return Objects.toStringHelper(this).omitNullValues().add("user", user).add("home", home).add("s3Port", s3Port)
            .add("authorizedAccessKey", authorizedAccessKey).add("tar", tar)
            .add("formatToNodejsPackage", formatToNodejsPackage).add("foreverVersion", foreverVersion).toString();
   }

}
