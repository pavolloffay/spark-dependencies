/**
 * Copyright 2017 The Jaeger Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jaegertracing.spark.dependencies.test;

import io.jaegertracing.spark.dependencies.test.rest.DependencyLink;
import io.jaegertracing.spark.dependencies.test.tree.Node;
import io.jaegertracing.spark.dependencies.test.tree.Traversals;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pavol Loffay
 */
public class DependencyLinkDerivator {

  public static Map<String, Map<String, Long>> serviceDependencies(Node root) {
    return serviceDependencies(root, new LinkedHashMap<>());
  }

  public static Map<String, Map<String, Long>> serviceDependencies(Node root,
      Map<String, Map<String, Long>> dependenciesMap) {
    Traversals.inorder(root, (child, parent) -> {
      if (parent == null) {
        return;
      }
      Map<String, Long> childMap = dependenciesMap.get(parent.getServiceName());
      if (childMap == null) {
        childMap = new LinkedHashMap<>();
        dependenciesMap.put(parent.getServiceName(), childMap);
      }

      Long callCount = childMap.get(child.getServiceName());
      if (callCount == null) {
        callCount = 0L;
      }
      childMap.put(child.getServiceName(), ++callCount);
    });
    return dependenciesMap;
  }

  public static Map<String, Map<String, Long>> serviceDependencies(List<DependencyLink> dependencyLinks) {
    Map<String, Map<String, Long>> parentDependencyMap = new LinkedHashMap<>();
    dependencyLinks.forEach(dependencyLink -> {
      Map<String, Long> childCallCountMap = parentDependencyMap.get(dependencyLink.getParent());
      if (childCallCountMap == null) {
        childCallCountMap = new LinkedHashMap<>();
        parentDependencyMap.put(dependencyLink.getParent(), childCallCountMap);
      }
      childCallCountMap.put(dependencyLink.getChild(), dependencyLink.getCallCount());
    });
    return parentDependencyMap;
  }
}
