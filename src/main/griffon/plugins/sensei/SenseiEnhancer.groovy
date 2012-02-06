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

import griffon.util.CallableWithArgs

/**
 * @author Andres Almiray
 */
final class SenseiEnhancer {
    private SenseiEnhancer() {}
    
    static void enhance(MetaClass mc, SenseiProvider provider = SenseiServerHolder.instance) {
        mc.withSensei = {Closure closure ->
            provider.withSensei('default', closure)
        }
        mc.withSensei << {String storeName, Closure closure ->
            provider.withSensei(storeName, closure)
        }
        mc.withSensei << {CallableWithArgs callable ->
            provider.withSensei('default', callable)
        }
        mc.withSensei << {String storeName, CallableWithArgs callable ->
            provider.withSensei(storeName, callable)
        }
    }
}
