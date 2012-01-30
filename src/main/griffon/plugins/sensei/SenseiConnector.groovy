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
package griffon.plugins.sensei

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
final class SenseiConnector {
    private bootstrap

    private static final Logger LOG = LoggerFactory.getLogger(SenseiConnector)

    static void enhance(MetaClass mc) {
        mc.withSensei = {Closure closure ->
            SenseiStoreHolder.instance.withSensei('default', closure)
        }
        mc.withSensei << {String storeName, Closure closure ->
            SenseiStoreHolder.instance.withSensei(storeName, closure)
        }
        mc.withSensei << {CallableWithArgs callable ->
            SenseiStoreHolder.instance.withSensei('default', callable)
        }
        mc.withSensei << {String storeName, CallableWithArgs callable ->
            SenseiStoreHolder.instance.withSensei(storeName, callable)
        }
    }

    Object withSensei(String storeName = 'default', Closure closure) {
        SenseiStoreHolder.instance.withSensei(storeName, closure)
    }

    public <T> T withSensei(String storeName = 'default', CallableWithArgs<T> callable) {
        return SenseiStoreHolder.instance.withSensei(storeName, callable)
    }

    // ======================================================

    ConfigObject createConfig(GriffonApplication app) {
        def storeClass = app.class.classLoader.loadClass('SenseiConfig')
        new ConfigSlurper(Environment.current.name).parse(storeClass)
    }

    private ConfigObject narrowConfig(ConfigObject config, String storeName) {
        return storeName == 'default' ? config.store : config.stores[storeName]
    }

    SenseiServiceProxy connect(GriffonApplication app, ConfigObject config, String storeName = 'default') {
        if (SenseiStoreHolder.instance.isStoreConnected(storeName)) {
            return SenseiStoreHolder.instance.getStore(storeName)
        }

        config = narrowConfig(config, storeName)
        app.event('SenseiConnectStart', [config, storeName])
        SenseiServiceProxy store = startSensei(config)
        SenseiStoreHolder.instance.setStore(storeName, store)
        bootstrap = app.class.classLoader.loadClass('BootstrapSensei').newInstance()
        bootstrap.metaClass.app = app
        bootstrap.init(storeName, store)
        app.event('SenseiConnectEnd', [storeName, store])
        store
    }

    void disconnect(GriffonApplication app, ConfigObject config, String storeName = 'default') {
        if (SenseiStoreHolder.instance.isStoreConnected(storeName)) {
            config = narrowConfig(config, storeName)
            SenseiServiceProxy store = SenseiStoreHolder.instance.getStore(storeName)
            app.event('SenseiDisconnectStart', [config, storeName, store])
            bootstrap.destroy(storeName, store)
            stopSensei(config, store)
            app.event('SenseiDisconnectEnd', [config, storeName])
            SenseiStoreHolder.instance.disconnectStore(storeName)
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
