package uk.gov.ons.ctp.response.collection.exercise.lib.common.distributed;

import java.util.UUID;

/** Base for Distributed Manager */
public abstract class DistributedManagerBase {
  protected String keyRoot;
  protected String uuid;

  /**
   * Distributed Manager Base constructor
   *
   * @param keyRoot root key
   */
  public DistributedManagerBase(String keyRoot) {
    this.uuid = UUID.randomUUID().toString();
    this.keyRoot = keyRoot;
  }

  /**
   * Creates Key
   *
   * @param key key string
   * @return String key
   */
  protected String createKey(String key) {
    return String.format("%s:%s:%s", keyRoot, uuid, key);
  }

  /**
   * Creates global Key
   *
   * @param key key string
   * @return String key
   */
  protected String createGlobalKey(String key) {
    return String.format("%s:global:%s", keyRoot, key);
  }

  /**
   * Creates Key for all instances
   *
   * @param key key string
   * @return String key
   */
  protected String createAllInstancesKey(String key) {
    return String.format("%s:*:%s", keyRoot, key);
  }
}
