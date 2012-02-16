/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package griffon.plugins.senseidb

import com.senseidb.search.client.json.SenseiServiceProxy

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import griffon.util.CallableWithArgs
import static griffon.util.GriffonNameUtils.isBlank

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
class SenseidbStoreHolder implements SenseidbProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SenseidbStoreHolder)
    private static final Object[] LOCK = new Object[0]
    private final Map<String, SenseiServiceProxy> stores = [:]
  
    String[] getStoreNames() {
        List<String> storeNames = new ArrayList().addAll(stores.keySet())
        storeNames.toArray(new String[storeNames.size()])
    }

    SenseiServiceProxy getStore(String storeName = 'default') {
        if(isBlank(storeName)) storeName = 'default'
        retrieveStore(storeName)
    }

    void setStore(String storeName = 'default', SenseiServiceProxy store) {
        if(isBlank(storeName)) storeName = 'default'
        storeStore(storeName, store)       
    }

    Object withSenseidb(String storeName = 'default', Closure closure) {
        SenseiServiceProxy store = fetchStore(storeName)
        if(LOG.debugEnabled) LOG.debug("Executing statement on store '$storeName'")
        return closure(storeName, store)
    }

    public <T> T withSenseidb(String storeName = 'default', CallableWithArgs<T> callable) {
        SenseiServiceProxy store = fetchStore(storeName)
        if(LOG.debugEnabled) LOG.debug("Executing statement on store '$storeName'")
        callable.args = [storeName, store] as Object[]
        return callable.call()
    }
    
    boolean isStoreConnected(String storeName) {
        if(isBlank(storeName)) storeName = 'default'
        retrieveStore(storeName) != null
    }
    
    void disconnectStore(String storeName) {
        if(isBlank(storeName)) storeName = 'default'
        storeStore(storeName, null)        
    }

    private SenseiServiceProxy fetchStore(String storeName) {
        if(isBlank(storeName)) storeName = 'default'
        SenseiServiceProxy store = retrieveStore(storeName)
        if(store == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = SenseidbConnector.instance.createConfig(app)
            store = SenseidbConnector.instance.connect(app, config, storeName)
        }
        
        if(store == null) {
            throw new IllegalArgumentException("No such SenseiServiceProxy configuration for name $storeName")
        }
        store
    }

    private SenseiServiceProxy retrieveStore(String storeName) {
        synchronized(LOCK) {
            stores[storeName]
        }
    }

    private void storeStore(String storeName, SenseiServiceProxy store) {
        synchronized(LOCK) {
            stores[storeName] = store
        }
    }
}
