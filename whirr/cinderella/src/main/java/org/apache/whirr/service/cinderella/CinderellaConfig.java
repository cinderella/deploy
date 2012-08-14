/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Tar 2.0 (the
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

import com.google.common.base.Objects;

/**
 * Configuration required to install and configure cinderella properly
 * 
 * @author Adrian Cole
 */
public class CinderellaConfig {

   public static Builder builder() {
      return new Builder();
   }

   public Builder toBuilder() {
      return new Builder().fromCinderellaConfig(this);
   }

   public static class Builder {
      private String user = "cinderella";
      private String home = "/usr/local/cinderella";
      private int ec2Port = 8080;
      private String ec2Version = "2010-11-15";
      private String authorizedAccessKey = "MvndHwA4e6dgaGV23L94";
      private String authorizedSecretKey = "A50GS9tj2DLXRln4rf1K+A/CSjmAbBGw0H5yul6s";
      private URI vCloudEndpoint;
      private String vCloudUserAtOrg;
      private String vCloudPassword;
      private URI tar = URI.create("https://github.com/cinderella/cinderella/tarball/master");
      private URI cloudStackTar = URI.create("https://github.com/cinderella/incubator-cloudStack/tarball/master");
      private URI mavenTar = URI.create("http://www.us.apache.org/dist/maven/binaries/apache-maven-3.0.4-bin.tar.gz");
      private URI jettyTar = URI
            .create("http://download.eclipse.org/jetty/stable-8/dist/jetty-distribution-8.1.5.v20120716.tar.gz");

      /**
       * @see CinderellaConfig#getUser()
       */
      public Builder user(String user) {
         this.user = user;
         return this;
      }

      /**
       * @see CinderellaConfig#getHome()
       */
      public Builder home(String home) {
         this.home = home;
         return this;
      }

      /**
       * @see CinderellaConfig#getEC2Port()
       */
      public Builder ec2Port(int ec2Port) {
         this.ec2Port = ec2Port;
         return this;
      }

      /**
       * @see CinderellaConfig#getEC2Version()
       */
      public Builder ec2Version(String ec2Version) {
         this.ec2Version = ec2Version;
         return this;
      }

      /**
       * @see CinderellaConfig#getAuthorizedAccessKey()
       */
      public Builder authorizedAccessKey(String authorizedAccessKey) {
         this.authorizedAccessKey = authorizedAccessKey;
         return this;
      }

      /**
       * @see CinderellaConfig#getAuthorizedSecretKey()
       */
      public Builder authorizedSecretKey(String authorizedSecretKey) {
         this.authorizedSecretKey = authorizedSecretKey;
         return this;
      }

      /**
       * @see CinderellaConfig#getVCloudEndpoint()
       */
      public Builder vCloudEndpoint(URI vCloudEndpoint) {
         this.vCloudEndpoint = vCloudEndpoint;
         return this;
      }

      /**
       * @see CinderellaConfig#getVCloudUserAtOrg()
       */
      public Builder vCloudUserAtOrg(String vCloudUserAtOrg) {
         this.vCloudUserAtOrg = vCloudUserAtOrg;
         return this;
      }

      /**
       * @see CinderellaConfig#getVCloudPassword()
       */
      public Builder vCloudPassword(String vCloudPassword) {
         this.vCloudPassword = vCloudPassword;
         return this;
      }

      /**
       * @see CinderellaConfig#getTar()
       */
      public Builder tar(URI tar) {
         this.tar = tar;
         return this;
      }

      /**
       * @see CinderellaConfig#getCloudStackTar()
       */
      public Builder cloudStackTar(URI cloudStackTar) {
         this.cloudStackTar = cloudStackTar;
         return this;
      }

      /**
       * @see CinderellaConfig#getMavenTar()
       */
      public Builder mavenTar(URI mavenTar) {
         this.mavenTar = mavenTar;
         return this;
      }

      /**
       * @see CinderellaConfig#getJettyTar()
       */
      public Builder jettyTar(URI jettyTar) {
         this.jettyTar = jettyTar;
         return this;
      }

      public CinderellaConfig build() {
         return new CinderellaConfig(user, home, ec2Port, ec2Version, authorizedAccessKey, authorizedSecretKey,
               vCloudEndpoint, vCloudUserAtOrg, vCloudPassword, tar, cloudStackTar, mavenTar, jettyTar);
      }

      public Builder fromCinderellaConfig(CinderellaConfig in) {
         return this.user(in.user).home(in.home).ec2Port(in.ec2Port).ec2Version(in.ec2Version)
               .authorizedAccessKey(in.authorizedAccessKey).authorizedSecretKey(in.authorizedSecretKey)
               .vCloudEndpoint(in.vCloudEndpoint).vCloudUserAtOrg(in.vCloudUserAtOrg).vCloudPassword(in.vCloudPassword)
               .tar(in.tar).cloudStackTar(in.cloudStackTar).mavenTar(in.mavenTar).jettyTar(in.jettyTar);
      }
   }

   private final String user;
   private final String home;
   private final int ec2Port;
   private final String ec2Version;
   private final String authorizedAccessKey;
   private final String authorizedSecretKey;
   private final URI vCloudEndpoint;
   private final String vCloudUserAtOrg;
   private final String vCloudPassword;
   private final URI tar;
   private final URI cloudStackTar;
   private final URI mavenTar;
   private final URI jettyTar;

   protected CinderellaConfig(String user, String home, int ec2Port, String ec2Version, String authorizedAccessKey,
         String authorizedSecretKey, URI vCloudEndpoint, String vCloudUserAtOrg, String vCloudPassword, URI tar,
         URI cloudStackTar, URI mavenTar, URI jettyTar) {
      this.user = checkNotNull(user, "user");
      this.home = checkNotNull(home, "home");
      this.ec2Port = checkNotNull(ec2Port, "ec2Port");
      this.ec2Version = checkNotNull(ec2Version, "ec2Version");
      this.authorizedAccessKey = checkNotNull(authorizedAccessKey, "authorizedAccessKey");
      this.authorizedSecretKey = checkNotNull(authorizedSecretKey, "authorizedSecretKey");
      this.vCloudEndpoint = checkNotNull(vCloudEndpoint, "vCloudEndpoint");
      this.vCloudUserAtOrg = checkNotNull(vCloudUserAtOrg, "vCloudUserAtOrg");
      this.vCloudPassword = checkNotNull(vCloudPassword, "vCloudPassword");
      this.tar = checkNotNull(tar, "tar");
      this.cloudStackTar = checkNotNull(cloudStackTar, "cloudStackTar");
      this.mavenTar = checkNotNull(mavenTar, "mavenTar");
      this.jettyTar = checkNotNull(jettyTar, "jettyTar");
   }

   /**
    * User running the service
    */
   public String getUser() {
      return user;
   }

   /**
    * Fully qualified path under which we install cinderella
    */
   public String getHome() {
      return home;
   }

   /**
    * Port to listen to connections on (default 8080)
    */
   public int getEC2Port() {
      return ec2Port;
   }

   /**
    * Version of the EC2 Api. default {@code 2010-11-15}
    */
   public String getEC2Version() {
      return ec2Version;
   }

   /**
    * The Amazon EC2-compatible Access Key that this service will use when
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
    * The endpoint for the vCloud api. ex.
    * {@code https://lon01.ilandcloud.com/api}
    * 
    * @see #getVCloudUserAtOrg()
    * @see #getVCloudPassword()
    */
   public URI getVCloudEndpoint() {
      return vCloudEndpoint;
   }

   /**
    * The user and org to connect to {@link #getVCloudEndpoint() vCloud} with,
    * delimited by {@code @} ex. {@code cinderella@prodOrg }
    * 
    * @see #getVCloudEndpoint()
    * @see #getVCloudPassword()
    */
   public String getVCloudUserAtOrg() {
      return vCloudUserAtOrg;
   }

   /**
    * The password to the vCloud {@link #getVCloudUserAtOrg()}
    * 
    * @see #getVCloudUserAtOrg()
    * @see #getVCloudPassword()
    */
   public String getVCloudPassword() {
      return vCloudPassword;
   }

   /**
    * location to install cinderella from. default
    * {@code https://github.com/cinderella/cinderella/tarball/master}
    */
   public URI getTar() {
      return tar;
   }

   /**
    * CloudStack Cloud Bridge is used for EC2 Query Classes. default
    * {@code https://github.com/cinderella/incubator-cloudStack/tarball/master}
    * 
    */
   public URI getCloudStackTar() {
      return cloudStackTar;
   }

   /**
    * Maven is used to create the war file for cinderella. default
    * {@code http://www.us.apache.org/dist/maven/binaries/apache-maven-3.0.4-bin.tar.gz}
    * 
    */
   public URI getMavenTar() {
      return mavenTar;
   }

   /**
    * Jetty is used to create the war file for cinderella. default
    * {@code http://download.eclipse.org/jetty/stable-8/dist/jetty-distribution-8.1.5.v20120716.tar.gz}
    * 
    */
   public URI getJettyTar() {
      return jettyTar;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode() {
      return Objects.hashCode(user, home, ec2Port, ec2Version, authorizedAccessKey, vCloudEndpoint, vCloudUserAtOrg,
            tar, cloudStackTar, mavenTar, jettyTar);
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
      CinderellaConfig other = CinderellaConfig.class.cast(obj);
      return Objects.equal(this.user, other.user) && Objects.equal(this.home, other.home)
            && Objects.equal(this.ec2Port, other.ec2Port) && Objects.equal(this.ec2Version, other.ec2Version)
            && Objects.equal(this.authorizedAccessKey, other.authorizedAccessKey)
            && Objects.equal(this.vCloudEndpoint, other.vCloudEndpoint)
            && Objects.equal(this.vCloudUserAtOrg, other.vCloudUserAtOrg) && Objects.equal(this.tar, other.tar)
            && Objects.equal(this.cloudStackTar, other.cloudStackTar) && Objects.equal(this.mavenTar, other.mavenTar)
            && Objects.equal(this.jettyTar, other.jettyTar);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      return Objects.toStringHelper(this).omitNullValues().add("user", user).add("home", home).add("ec2Port", ec2Port)
            .add("ec2Version", ec2Version).add("authorizedAccessKey", authorizedAccessKey)
            .add("vCloudEndpoint", vCloudEndpoint).add("vCloudUserAtOrg", vCloudUserAtOrg).add("tar", tar)
            .add("cloudStackTar", cloudStackTar).add("mavenTar", mavenTar).add("jettyTar", jettyTar).toString();
   }

}
