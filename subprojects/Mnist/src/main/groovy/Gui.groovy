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
import static GroovyFXUtil.*
import static MnistInfer.*
import static Util.displayResult
import static Util.maxIndex
import static groovyx.javafx.GroovyFX.start
import static javafx.scene.paint.Color.WHITE

def size = 280
def pen = 12
load(getClass())

start {
    stage(title: 'MNIST', visible: true) {
        scene(id: "scene", fill: WHITE) {
            borderPane {
                top {
                    canvas(id: "canvas", width: size, height: size)
                    def g = canvas.graphicsContext2D
                    clear(g, size)
                    canvas.onMouseDragged { e -> g.fillOval e.x - pen, e.y - pen, pen * 2, pen * 2 }
                }
                center {
                    hbox(alignment: 'Center') {
                        button('Clear', onAction: {
                            clear(canvas.graphicsContext2D, size)
                            out.text = displayResult([null] * 10, null)
                        })
                        button('Predict', onAction: {
                            def result = query(normalize(imageToArray(snapshot(canvas))))
                            def predictLabel = maxIndex(result)
                            out.text = displayResult(result.data.collect{ it[0] }, predictLabel)
                        })
                    }
                }
                bottom {
                    textArea(displayResult([null] * 10, null), id: 'out', editable: false, prefColumnCount: 16)
                }
            }
        }
    }
}
