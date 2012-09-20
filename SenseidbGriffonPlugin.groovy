/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
class SenseidbGriffonPlugin {
    // the plugin version
    String version = '0.2'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.1.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-senseidb-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Senseidb support'
    String description = '''
The Senseidb plugin enables lightweight access to [Senseidb][1] datastores.
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * SenseidbConfig.groovy - contains the datastore definitions.
 * BootstrapdbSenseidb.groovy - defines init/destroy hooks for data to be manipulated during app startup/shutdown.

A new dynamic method named `withSenseidb` will be injected into all controllers,
giving you access to an `com.senseidb.search.client.json.SenseiServiceProxy` object, with which you'll be able
to make calls to the datastore. Remember to make all datastore calls off the EDT
otherwise your application may appear unresponsive when doing long computations
inside the EDT.

This method is aware of multiple datastores. If no dsName is specified when calling
it then the default datastore will be selected. Here are two example usages, the first
queries against the default datastore while the second queries a datastore whose name has
been configured as 'internal'

    package sample
    class SampleController {
        def queryAllDataStores = {
            withSenseidb { dsName, proxy -> ... }
            withSenseidb('internal') { dsName, proxy -> ... }
        }
    }

This method is also accessible to any component through the singleton `griffon.plugins.senseidb.SenseidbConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`SenseidbEnhancer.enhance(metaClassInstance, senseidbProviderInstance)`.

Configuration
-------------
### Dynamic method injection

The `withSenseidb()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.senseidb.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * SenseidbConnectStart[config, dsName] - triggered before connecting to the datastore
 * SenseidbConnectEnd[dsName, datastore] - triggered after connecting to the datastore
 * SenseidbDisconnectStart[config, dsName, datastore] - triggered before disconnecting from the datastore
 * SenseidbDisconnectEnd[config, dsName] - triggered after disconnecting from the datastore

### Multiple Stores

The config file `SenseidbConfig.groovy` defines a default datastore block. As the name
implies this is the datastore used by default, however you can configure named datastores
by adding a new config block. For example connecting to a datastore whose name is 'internal'
can be done in this way

    datastores {
        internal {
            host = 'server.acme.com'
        }
    }

This block can be used inside the `environments()` block in the same way as the
default datastore block is used.

Testing
-------
The `withSenseidb()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `SenseidbEnhancer.enhance(metaClassInstance, senseiProviderInstance)` where 
`senseiProviderInstance` is of type `griffon.plugins.sensei.SenseidbProvider`. The contract for this interface looks like this

    public interface SenseidbProvider {
        Object withSenseidb(Closure closure);
        Object withSenseidb(String storeName, Closure closure);
        <T> T withSenseidb(CallableWithArgs<T> callable);
        <T> T withSenseidb(String storeName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MySenseidbProvider implements SenseidbProvider {
        Object withSenseidb(String storeName = 'default', Closure closure) { null }
        public <T> T withSenseidb(String storeName = 'default', CallableWithArgs<T> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            SenseidbEnhancer.enhance(service.metaClass, new MySenseidbProvider())
            // exercise service methods
        }
    }


[1]: http://senseidb.com/
'''
}
