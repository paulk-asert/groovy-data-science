import static Util.*
import static MnistInfer.*
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
