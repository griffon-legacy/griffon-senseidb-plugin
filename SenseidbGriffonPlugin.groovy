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
    String version = '0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '0.9.5-SNAPSHOT > *'
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
    String source = 'https://github.com/aalmiray/griffon-senseidb-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Senseidb support'
    String description = '''
The Sensei plugin enables lightweight access to [Senseidb][1] datastores.
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * SenseiConfig.groovy - contains the datastore definitions.
 * BootstrapSensei.groovy - defines init/destroy hooks for data to be manipulated during app startup/shutdown.

A new dynamic method named `withSensei` will be injected into all controllers,
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
	        withSensei { dsName, proxy -> ... }
	        withSensei('internal') { dsName, proxy -> ... }
	    }
	}
	
This method is also accessible to any component through the singleton `griffon.plugins.sensei.SenseiConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`SenseiConnector.enhance(metaClassInstance)`.

Configuration
-------------
### Dynamic method injection

The `withSensei()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.sensei.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * SenseiConnectStart[config, dsName] - triggered before connecting to the datastore
 * SenseiConnectEnd[dsName, datastore] - triggered after connecting to the datastore
 * SenseiDisconnectStart[config, dsName, datastore] - triggered before disconnecting from the datastore
 * SenseiDisconnectEnd[config, dsName] - triggered after disconnecting from the datastore

### Multiple Stores

The config file `SenseiConfig.groovy` defines a default datastore block. As the name
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
The `withSensei()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `SenseiEnhancer.enhance(metaClassInstance, senseiProviderInstance)` where 
`senseiProviderInstance` is of type `griffon.plugins.sensei.SenseiProvider`. The contract for this interface looks like this

    public interface SenseiProvider {
        Object withSensei(Closure closure);
        Object withSensei(String storeName, Closure closure);
        <T> T withSensei(CallableWithArgs<T> callable);
        <T> T withSensei(String storeName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MySenseiProvider implements SenseiProvider {
        Object withSensei(String storeName = 'default', Closure closure) {
            // empty
        }

        public <T> T withSensei(String storeName = 'default', CallableWithArgs<T> callable) {
            // empty
        }       
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            SenseiEnhancer.enhance(service.metaClass, new MySenseiProvider())
            // exercise service methods
        }
    }


[1]: http://senseidb.com/
'''
}
