// Copyright 2018, Oracle Corporation and/or its affiliates.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at
// http://oss.oracle.com/licenses/upl.

package oracle.kubernetes.weblogic.domain;

import io.kubernetes.client.models.V1LocalObjectReference;
import java.util.Arrays;
import javax.annotation.Nonnull;
import oracle.kubernetes.weblogic.domain.v1.Domain;
import oracle.kubernetes.weblogic.domain.v1.DomainSpec;
import oracle.kubernetes.weblogic.domain.v1.DomainStorage;

/**
 * Configures a domain, adding settings independently of the version of the domain representation.
 * Note that the configurator uses a predefined domain schema, and should only be used for testing.
 * Using it in the runtime runs the risk of corrupting the domain.
 */
@SuppressWarnings("UnusedReturnValue")
public abstract class DomainConfigurator {

  private Domain domain;

  protected DomainConfigurator(Domain domain) {
    this.domain = domain;
  }

  public abstract DomainConfigurator createFor(Domain domain);

  /**
   * Defines a name for the domain's admin server.
   *
   * @param adminServerName the name of the admin server
   */
  public abstract AdminServerConfigurator configureAdminServer(String adminServerName);

  /**
   * Sets the default number of replicas to be run in a cluster.
   *
   * @param replicas a non-negative number
   */
  public abstract void withDefaultReplicaCount(int replicas);

  /**
   * Sets the default image for the domain.
   *
   * @param image the name of the image
   * @return this object
   */
  public DomainConfigurator withDefaultImage(String image) {
    getDomainSpec().setImage(image);
    return this;
  }

  /**
   * Sets the default image pull policy for the domain.
   *
   * @param imagepullpolicy the new policy
   * @return this object
   */
  public DomainConfigurator withDefaultImagePullPolicy(String imagepullpolicy) {
    getDomainSpec().setImagePullPolicy(imagepullpolicy);
    return this;
  }

  /**
   * Sets the default image pull secret for the domain
   *
   * @param secretReference the object referring to the secret
   * @return this object
   */
  public DomainConfigurator withDefaultImagePullSecret(V1LocalObjectReference secretReference) {
    getDomainSpec().setImagePullSecret(secretReference);
    return this;
  }

  /**
   * Sets the image pull secrets for the domain
   *
   * @param secretReferences a list of objects referring to secrets
   * @return this object
   */
  public DomainConfigurator withDefaultImagePullSecrets(V1LocalObjectReference... secretReferences) {
    getDomainSpec().setImagePullSecrets(Arrays.asList(secretReferences));
    return this;
  }

  /**
   * Configures the domain to use a persistent volume claim defined before the domain is created.
   *
   * @param claimName the name of the persistent volume claim
   * @return this object
   */
  public DomainConfigurator withPredefinedClaim(String claimName) {
    getDomainSpec().setStorage(DomainStorage.createPredefinedClaim(claimName));
    return this;
  }

  /**
   * Configures the domain to use storage in the local node.
   *
   * @param path the path to the storage
   * @return this object
   */
  public DomainConfigurator withHostPathStorage(String path) {
    getDomainSpec().setStorage(DomainStorage.createHostPathStorage(path));
    return this;
  }

  /**
   * Configures the domain to use storage on a remote server.
   *
   * @param server the server hosting the storage
   * @param path the path to the storage
   * @return this object
   */
  public DomainConfigurator withNfsStorage(String server, String path) {
    getDomainSpec().setStorage(DomainStorage.createNfsStorage(server, path));
    return this;
  }

  /**
   * Defines the amount of storage to allocate for the domain.
   *
   * @param size the size to allocate
   * @return this object
   */
  public DomainConfigurator withStorageSize(String size) {
    getDomainSpec().getStorage().setStorageSize(size);
    return this;
  }

  /**
   * Defines the amount of storage to allocate for the domain.
   *
   * @param policy the size to allocate
   * @return this object
   */
  public DomainConfigurator withStorageReclaimPolicy(String policy) {
    getDomainSpec().getStorage().setStorageReclaimPolicy(policy);
    return this;
  }

  /**
   * Sets the default settings for the readiness probe. Any settings left null will default to the
   * tuning parameters.
   *
   * @param initialDelay the default initial delay, in seconds.
   * @param timeout the default timeout, in seconds.
   * @param period the default probe period, in seconds.
   */
  public abstract void withDefaultReadinessProbeSettings(
      Integer initialDelay, Integer timeout, Integer period);

  /**
   * Sets the default settings for the liveness probe. Any settings left null will default to the
   * tuning parameters.
   *
   * @param initialDelay the default initial delay, in seconds.
   * @param timeout the default timeout, in seconds.
   * @param period the default probe period, in seconds.
   */
  public abstract void withDefaultLivenessProbeSettings(
      Integer initialDelay, Integer timeout, Integer period);

  /**
   * Sets the default server start policy ("ALWAYS", "NEVER" or "IF_NEEDED") for the domain.
   *
   * @param startPolicy the new default policy
   */
  public abstract DomainConfigurator withDefaultServerStartPolicy(String startPolicy);

  /**
   * Defines the startup control mechanism for a version 1 domain. Must be one of:
   *
   * <ul>
   *   <li>NONE indicates that no servers, including the administration server, will be started.
   *   <li>ADMIN indicates that only the administration server will be started.
   *   <li>ALL indicates that all servers in the domain will be started.
   *   <li>SPECIFIED indicates that the administration server will be started and then additionally
   *       only those servers listed under serverStartup or managed servers belonging to cluster
   *       listed under clusterStartup up to the cluster's replicas field will be started.
   *   <li>AUTO indicates that servers will be started exactly as with SPECIFIED, but then managed
   *       servers belonging to clusters not listed under clusterStartup will be started up to the
   *       replicas field.
   * </ul>
   *
   * <p>Defaults to AUTO.
   *
   * @param startupControl the new value
   * @return this object
   */
  public abstract DomainConfigurator withStartupControl(String startupControl);

  /**
   * Add an environment variable to the domain
   *
   * @param name variable name
   * @param value value
   * @return this object
   */
  public abstract DomainConfigurator withEnvironmentVariable(String name, String value);

  protected DomainSpec getDomainSpec() {
    return domain.getSpec();
  }

  protected String getAsName() {
    return domain.getAsName();
  }

  /**
   * Adds a default server configuration to the domain, if not already present.
   *
   * @param serverName the name of the server to add
   * @return an object to add additional configurations
   */
  public abstract ServerConfigurator configureServer(@Nonnull String serverName);

  /**
   * Adds a default cluster configuration to the domain, if not already present.
   *
   * @param clusterName the name of the server to add
   * @return an object to add additional configurations
   */
  public abstract ClusterConfigurator configureCluster(@Nonnull String clusterName);

  public abstract void setShuttingDown(boolean start);

  /**
   * Returns true if this configurator supports use of the startup control flag.
   *
   * @return true or false
   */
  public abstract boolean useDomainV1();
}
