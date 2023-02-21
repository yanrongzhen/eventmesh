/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eventmesh.spi.loader;

import org.apache.eventmesh.spi.ExtensionException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


import lombok.extern.slf4j.Slf4j;

/**
 * Load extension from classpath
 */

@Slf4j
public class MetaInfExtensionClassLoader implements ExtensionClassLoader {

    private static final MetaInfExtensionClassLoader INSTANCE = new MetaInfExtensionClassLoader();

    private final ConcurrentHashMap<Class<?>, Map<String, Class<?>>> extensionClassCache = new ConcurrentHashMap<>(16);

    private MetaInfExtensionClassLoader() {

    }

    @Override
    public <T> Map<String, Class<?>> loadExtensionClass(Class<T> extensionType, String extensionInstanceName) {
        return extensionClassCache.computeIfAbsent(extensionType, this::doLoadExtensionClass);
    }

    public static MetaInfExtensionClassLoader getInstance() {
        return INSTANCE;
    }

    private <T> Map<String, Class<?>> doLoadExtensionClass(Class<T> extensionType) {
        Map<String, Class<?>> extensionMap = new HashMap<>();
        String extensionFileName = EventMeshExtensionConstant.EVENTMESH_EXTENSION_META_DIR + extensionType.getName();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> extensionUrls = classLoader.getResources(extensionFileName);
            if (extensionUrls != null) {
                while (extensionUrls.hasMoreElements()) {
                    URL url = extensionUrls.nextElement();
                    extensionMap.putAll(loadResources(url, extensionType));
                }
            }
        } catch (IOException e) {
            throw new ExtensionException("load extension class error", e);
        }
        return extensionMap;
    }

    private <T> Map<String, Class<?>> loadResources(URL url, Class<T> extensionType) throws IOException {
        Map<String, Class<?>> extensionMap = new HashMap<>();
        try (InputStream inputStream = url.openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((extensionName, extensionClass) -> {
                String extensionNameStr = (String) extensionName;
                String extensionClassStr = (String) extensionClass;
                try {
                    Class<?> targetClass = Class.forName(extensionClassStr);
                    log.info("load extension class success, extensionType: {}, extensionClass: {}", extensionType, targetClass);
                    if (!extensionType.isAssignableFrom(targetClass)) {
                        throw new ExtensionException(String.format("class: %s is not subClass of %s", targetClass, extensionType));
                    }
                    extensionMap.put(extensionNameStr, targetClass);
                } catch (ClassNotFoundException e) {
                    throw new ExtensionException("load extension class error", e);
                }
            });
        }
        return extensionMap;
    }
}
