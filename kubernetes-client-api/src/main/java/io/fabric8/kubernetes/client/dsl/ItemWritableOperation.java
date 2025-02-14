/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fabric8.kubernetes.client.dsl;

public interface ItemWritableOperation<T> extends DeletableWithOptions, ItemReplacable<T> {

  /**
   * Creates a provided resource in a Kubernetes Cluster. If creation
   * fails with a HTTP_CONFLICT, it tries to replace resource.
   *
   * @param item to create or replace
   * @return created or replaced item returned in kubernetes api response
   */
  T createOrReplace(T item);

  T create(T item);

  boolean delete(T item);

  /**
   * When the status subresource is enabled, the /status subresource for the custom resource is exposed.
   * It does a PUT requests to the /status subresource take a resource object and ignore changes
   * to anything except the status stanza.
   *
   * @param item kubernetes object
   * @return updated object
   * @deprecated please use one of patchStatus, editStatus, or replaceStatus, or a locked replace
   *             {@link Lockable#lockResourceVersion(String)}
   */
  @Deprecated
  T updateStatus(T item);

}
