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
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPInputStream

static int[] getLabels(Path labelsFile) {
    ByteBuffer bb = ByteBuffer.wrap(decompress(labelsFile.bytes))
    if (bb.int != 2049) throw new IOException("not a labels file")
    int numLabels = bb.int
    (0..<numLabels).collect{ bb.get() & 0xFF } as int[]
}

static List<int[][]> getImages(Path imagesFile) {
    ByteBuffer bb = ByteBuffer.wrap(decompress(Files.readAllBytes(imagesFile)))
    if (bb.int != 2051) throw new IOException("not an images file")

    int numImages = bb.int
    int numRows = bb.int
    int numColumns = bb.int
    List<int[][]> images = (0..<numImages).collect {
        int[][] image = new int[numRows][]
        for (row in 0..<numRows) {
            image[row] = new int[numColumns]
            for (col in 0..<numColumns) {
                image[row][col] = bb.get() & 0xFF
            }
        }
        image
    }
    return images
}

private static byte[] decompress(final byte[] input) {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(input)
         GZIPInputStream gis = new GZIPInputStream(bais)
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        byte[] buf = new byte[8192]
        int n
        while ((n = gis.read(buf)) > 0) {
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }
}
