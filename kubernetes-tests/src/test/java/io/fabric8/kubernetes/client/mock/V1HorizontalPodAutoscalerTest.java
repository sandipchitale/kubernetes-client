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

package io.fabric8.kubernetes.client.mock;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscalerList;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscalerListBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.kubernetes.client.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableKubernetesMockClient
public class V1HorizontalPodAutoscalerTest {

  KubernetesMockServer server;
  KubernetesClient client;

  @Test
  void testLoadAsPlainList() {
    List<HasMetadata> itemsList = client.load(getClass().getResourceAsStream("/test-horizontalpodautoscaler.yml")).get();

    assertNotNull(itemsList);
    assertEquals(1, itemsList.size());
    HorizontalPodAutoscaler horizontalPodAutoscaler = (HorizontalPodAutoscaler) itemsList.get(0);
    assertEquals("php-apache", horizontalPodAutoscaler.getMetadata().getName());
    assertEquals(10, horizontalPodAutoscaler.getSpec().getMaxReplicas());
    assertEquals(1, horizontalPodAutoscaler.getSpec().getMinReplicas());
  }

  @Test
  void testList() {
    server.expect().withPath("/apis/autoscaling/v1/namespaces/test/horizontalpodautoscalers")
        .andReturn(200, new HorizontalPodAutoscalerListBuilder().build()).once();
    server.expect().withPath("/apis/autoscaling/v1/namespaces/ns1/horizontalpodautoscalers")
        .andReturn(200, new HorizontalPodAutoscalerListBuilder()
            .addNewItem().and()
            .addNewItem().and()
            .build())
        .once();

    HorizontalPodAutoscalerList horizontalPodAutoscalerList = client.autoscaling().v1().horizontalPodAutoscalers()
        .inNamespace("test").list();
    assertNotNull(horizontalPodAutoscalerList);
    assertEquals(0, horizontalPodAutoscalerList.getItems().size());

    horizontalPodAutoscalerList = client.autoscaling().v1().horizontalPodAutoscalers().inNamespace("ns1").list();
    assertNotNull(horizontalPodAutoscalerList);
    assertEquals(2, horizontalPodAutoscalerList.getItems().size());
  }

  @Test
  void testListWithLabels() {
    server.expect()
        .withPath("/apis/autoscaling/v1/namespaces/test/horizontalpodautoscalers?labelSelector="
            + Utils.toUrlEncoded("key1=value1,key2=value2,key3=value3"))
        .andReturn(200, new HorizontalPodAutoscalerListBuilder().build()).once();
    server.expect()
        .withPath("/apis/autoscaling/v1/namespaces/ns1/horizontalpodautoscalers?labelSelector="
            + Utils.toUrlEncoded("key1=value1,key2=value2"))
        .andReturn(200, new HorizontalPodAutoscalerListBuilder()
            .addNewItem().and()
            .addNewItem().and()
            .addNewItem().and()
            .build())
        .once();

    HorizontalPodAutoscalerList horizontalPodAutoscalerList = client.autoscaling().v1().horizontalPodAutoscalers()
        .withLabel("key1", "value1")
        .withLabel("key2", "value2")
        .withLabel("key3", "value3")
        .list();
    assertNotNull(horizontalPodAutoscalerList);
    assertEquals(0, horizontalPodAutoscalerList.getItems().size());

    horizontalPodAutoscalerList = client.autoscaling().v1().horizontalPodAutoscalers().inNamespace("ns1")
        .withLabel("key1", "value1")
        .withLabel("key2", "value2")
        .list();
    assertNotNull(horizontalPodAutoscalerList);
    assertEquals(3, horizontalPodAutoscalerList.getItems().size());
  }

  @Test
  void testGet() {
    server.expect().withPath("/apis/autoscaling/v1/namespaces/test/horizontalpodautoscalers/horizontalpodautoscaler1")
        .andReturn(200, new HorizontalPodAutoscalerBuilder().build()).once();
    server.expect().withPath("/apis/autoscaling/v1/namespaces/ns1/horizontalpodautoscalers/horizontalpodautoscaler2")
        .andReturn(200, new HorizontalPodAutoscalerBuilder().build()).once();

    HorizontalPodAutoscaler horizontalPodAutoscaler = client.autoscaling().v1().horizontalPodAutoscalers().inNamespace("test")
        .withName("horizontalpodautoscaler1").get();
    assertNotNull(horizontalPodAutoscaler);

    horizontalPodAutoscaler = client.autoscaling().v1().horizontalPodAutoscalers().withName("horizontalpodautoscaler2").get();
    assertNull(horizontalPodAutoscaler);

    horizontalPodAutoscaler = client.autoscaling().v1().horizontalPodAutoscalers().inNamespace("ns1")
        .withName("horizontalpodautoscaler2").get();
    assertNotNull(horizontalPodAutoscaler);
  }

  @Test
  void testEditMissing() {

    Assertions.assertThrows(KubernetesClientException.class, () -> {
      server.expect().withPath("/apis/autoscaling/v1/namespaces/test/horizontalpodautoscalers/horizontalpodautoscaler")
          .andReturn(404, "error message from kubernetes").always();

      client.autoscaling().v1().horizontalPodAutoscalers().inNamespace("test").withName("horizontalpodautoscaler").edit(h -> h);
    });
  }

  @Test
  void testDelete() {
    server.expect().withPath("/apis/autoscaling/v1/namespaces/test/horizontalpodautoscalers/horizontalpodautoscaler1")
        .andReturn(200, new HorizontalPodAutoscalerBuilder().build()).once();
    server.expect().withPath("/apis/autoscaling/v1/namespaces/ns1/horizontalpodautoscalers/horizontalpodautoscaler2")
        .andReturn(200, new HorizontalPodAutoscalerBuilder().build()).once();

    Boolean deleted = client.autoscaling().v1().horizontalPodAutoscalers().inNamespace("test")
        .withName("horizontalpodautoscaler1").delete();
    assertTrue(deleted);

    deleted = client.autoscaling().v1().horizontalPodAutoscalers().withName("horizontalpodautoscaler2").delete();
    assertFalse(deleted);

    deleted = client.autoscaling().v1().horizontalPodAutoscalers().inNamespace("ns1").withName("horizontalpodautoscaler2")
        .delete();
    assertTrue(deleted);
  }

  @Test
  void testDeleteMulti() {
    HorizontalPodAutoscaler horizontalPodAutoscaler1 = new HorizontalPodAutoscalerBuilder().withNewMetadata()
        .withName("horizontalpodautoscaler1").withNamespace("test").endMetadata().build();
    HorizontalPodAutoscaler horizontalPodAutoscaler2 = new HorizontalPodAutoscalerBuilder().withNewMetadata()
        .withName("horizontalpodautoscaler2").withNamespace("ns1").endMetadata().build();
    HorizontalPodAutoscaler horizontalPodAutoscaler3 = new HorizontalPodAutoscalerBuilder().withNewMetadata()
        .withName("horizontalpodautoscaler3").withNamespace("any").endMetadata().build();

    server.expect().withPath("/apis/autoscaling/v1/namespaces/test/horizontalpodautoscalers/horizontalpodautoscaler1")
        .andReturn(200, horizontalPodAutoscaler1).once();
    server.expect().withPath("/apis/autoscaling/v1/namespaces/ns1/horizontalpodautoscalers/horizontalpodautoscaler2")
        .andReturn(200, horizontalPodAutoscaler2).once();

    Boolean deleted = client.autoscaling().v1().horizontalPodAutoscalers().inAnyNamespace().delete(horizontalPodAutoscaler1,
        horizontalPodAutoscaler2);
    assertTrue(deleted);

    deleted = client.autoscaling().v1().horizontalPodAutoscalers().inAnyNamespace().delete(horizontalPodAutoscaler3);
    assertFalse(deleted);
  }

  @Test
  void testCreateWithNameMismatch() {
    Assertions.assertThrows(KubernetesClientException.class, () -> {
      HorizontalPodAutoscaler horizontalPodAutoscaler1 = new HorizontalPodAutoscalerBuilder().withNewMetadata()
          .withName("horizontalpodautoscaler1").withNamespace("test").endMetadata().build();
      client.autoscaling().v1().horizontalPodAutoscalers().inNamespace("test").withName("horizontalpodautoscaler1")
          .create(horizontalPodAutoscaler1);
    });
  }

  @Test
  void testLoadFromFile() {
    HorizontalPodAutoscaler horizontalPodAutoscaler = client.autoscaling().v1().horizontalPodAutoscalers()
        .load(getClass().getResourceAsStream("/test-horizontalpodautoscaler.yml")).get();
    assertEquals("php-apache", horizontalPodAutoscaler.getMetadata().getName());
  }

  @Test
  void testBuild() {
    HorizontalPodAutoscaler horizontalPodAutoscaler = new HorizontalPodAutoscalerBuilder()
        .withNewMetadata().withName("test-hpa").withNamespace("test").endMetadata()
        .withNewSpec()
        .withNewScaleTargetRef()
        .withApiVersion("apps/v1")
        .withKind("Deployment")
        .withName("the-deployment")
        .endScaleTargetRef()
        .withMinReplicas(1)
        .withMaxReplicas(10)
        .endSpec()
        .build();

    server.expect().withPath("/apis/autoscaling/v1/namespaces/test/horizontalpodautoscalers/test-hpa")
        .andReturn(200, horizontalPodAutoscaler).once();

    horizontalPodAutoscaler = client.autoscaling().v1().horizontalPodAutoscalers().inNamespace("test").withName("test-hpa")
        .get();
    assertNotNull(horizontalPodAutoscaler);
  }

}
