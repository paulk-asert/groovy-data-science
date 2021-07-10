/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

class ResourceHelper {
    private File parent // place to store file
    private String urlPrefix

    /**
     * Helper class to save downloading bin models over the net each time.
     *
     * @param urlPrefix Where to download resource from
     */
    ResourceHelper(String urlPrefix) {
        this.urlPrefix = urlPrefix
        // place them alongside wherever build tool/IDE placed dummy resource
        parent = new File(getClass().classLoader.getResource('dummy.txt').file).parentFile
    }

    File load(String suffix) {
        def file = new File(parent, suffix + '.bin')
        if (!file.exists()) {
            URL url = new URL("$urlPrefix/${suffix}.bin")
            println "Downloading $suffix"
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream())
            FileOutputStream fileOutputStream = new FileOutputStream(file)
            fileOutputStream.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
        }
        file
    }
}
