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
import griffon.util.Environment
import griffon.util.Metadata
import griffon.util.CallableWithArgs

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
final class SenseidbConnector implements SenseidbProvider {
    private bootstrap

    private static final Logger LOG = LoggerFactory.getLogger(SenseidbConnector)

    Object withSenseidb(String storeName = 'default', Closure closure) {
        SenseidbStoreHolder.instance.withSenseidb(storeName, closure)
    }

    public <T> T withSenseidb(String storeName = 'default', CallableWithArgs<T> callable) {
        return SenseidbStoreHolder.instance.withSenseidb(storeName, callable)
    }

    // ======================================================

    ConfigObject createConfig(GriffonApplication app) {
        def storeClass = app.class.classLoader.loadClass('SenseidbConfig')
        new ConfigSlurper(Environment.current.name).parse(storeClass)
    }

    private ConfigObject narrowConfig(ConfigObject config, String storeName) {
        return storeName == 'default' ? config.store : config.stores[storeName]
    }

    SenseiServiceProxy connect(GriffonApplication app, ConfigObject config, String storeName = 'default') {
        if (SenseidbStoreHolder.instance.isStoreConnected(storeName)) {
            return SenseidbStoreHolder.instance.getStore(storeName)
        }

        config = narrowConfig(config, storeName)
        app.event('SenseidbConnectStart', [config, storeName])
        SenseiServiceProxy store = startSensei(config)
        SenseidbStoreHolder.instance.setStore(storeName, store)
        bootstrap = app.class.classLoader.loadClass('BootstrapSenseidb').newInstance()
        bootstrap.metaClass.app = app
        bootstrap.init(storeName, store)
        app.event('SenseidbConnectEnd', [storeName, store])
        store
    }

    void disconnect(GriffonApplication app, ConfigObject config, String storeName = 'default') {
        if (SenseidbStoreHolder.instance.isStoreConnected(storeName)) {
            config = narrowConfig(config, storeName)
            SenseiServiceProxy store = SenseidbStoreHolder.instance.getStore(storeName)
            app.event('SenseidbDisconnectStart', [config, storeName, store])
            bootstrap.destroy(storeName, store)
            stopSensei(config, store)
            app.event('SenseidbDisconnectEnd', [config, storeName])
            SenseidbStoreHolder.instance.disconnectStore(storeName)
        }
    }

    private SenseiServiceProxy startSensei(ConfigObject config) {
        String host  = config.host ?: 'localhost'
        int port     = config.port ?: 1234i
        
        new SenseiServiceProxy(host, port)
    }

    private void stopSensei(ConfigObject config, SenseiServiceProxy store) {
        // empty
    }
}
