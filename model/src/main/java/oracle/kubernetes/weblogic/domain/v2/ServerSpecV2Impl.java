// Copyright 2018, Oracle Corporation and/or its affiliates.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at
// http://oss.oracle.com/licenses/upl.

package oracle.kubernetes.weblogic.domain.v2;

import static oracle.kubernetes.operator.KubernetesConstants.DEFAULT_IMAGE;
import static oracle.kubernetes.weblogic.domain.v2.ConfigurationConstants.START_ALWAYS;
import static oracle.kubernetes.weblogic.domain.v2.ConfigurationConstants.START_IF_NEEDED;
import static oracle.kubernetes.weblogic.domain.v2.ConfigurationConstants.START_NEVER;

import io.kubernetes.client.models.V1EnvVar;
import io.kubernetes.client.models.V1LocalObjectReference;
import io.kubernetes.client.models.V1Probe;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import oracle.kubernetes.weblogic.domain.v1.ServerSpec;

/** The effective configuration for a server configured by the version 2 domain model. */
public class ServerSpecV2Impl extends ServerSpec {
  private final Server server;
  private Integer clusterLimit;

  /**
   * Constructs an object to return the effective configuration
   *
   * @param server the server whose configuration is to be returned
   * @param clusterLimit the number of servers desired for the cluster, or null if not a clustered
   *     server
   * @param configurations the additional configurations to search for values if the server lacks
   *     them
   */
  public ServerSpecV2Impl(
      Server server, Integer clusterLimit, BaseConfiguration... configurations) {
    this.server = getBaseConfiguration(server);
    this.clusterLimit = clusterLimit;
    for (BaseConfiguration configuration : configurations) this.server.fillInFrom(configuration);
  }

  private Server getBaseConfiguration(Server server) {
    return server != null ? server.getConfiguration() : new Server();
  }

  @Override
  public String getImage() {
    return Optional.ofNullable(getConfiguredImage()).orElse(DEFAULT_IMAGE);
  }

  @Override
  protected String getConfiguredImage() {
    return server.getImage();
  }

  @Override
  protected String getConfiguredImagePullPolicy() {
    return server.getImagePullPolicy();
  }

  @Override
  public V1LocalObjectReference getImagePullSecret() {
    return server.getImagePullSecret();
  }

  @Override
  public List<V1EnvVar> getEnvironmentVariables() {
    return withStateAdjustments(server.getEnv());
  }

  @Override
  public String getDesiredState() {
    return Optional.ofNullable(getConfiguredDesiredState()).orElse("RUNNING");
  }

  private String getConfiguredDesiredState() {
    return server.getServerStartState();
  }

  @Override
  public Integer getNodePort() {
    return server.getNodePort();
  }

  @Override
  public boolean shouldStart(int currentReplicas) {
    switch (server.getServerStartPolicy()) {
      case START_NEVER:
        return false;
      case START_ALWAYS:
        return true;
      case START_IF_NEEDED:
        return clusterLimit == null || currentReplicas < clusterLimit;
      default:
        return clusterLimit == null;
    }
  }

  @Nonnull
  @Override
  public V1Probe getLivenessProbe() {
    return server.getLivenessProbe();
  }

  @Nonnull
  @Override
  public V1Probe getReadinessProbe() {
    return server.getReadinessProbe();
  }
}