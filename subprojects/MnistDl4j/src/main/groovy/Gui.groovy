import static Util.*
import static groovyx.javafx.GroovyFX.start
import static javafx.scene.paint.Color.WHITE

static displayResult(List items, Integer predict) {
    items.indexed().collect{ idx, val ->
        def marker = (idx == predict && items[predict] > 0.5) ? ' **' : ''
        "$idx ${val ? sprintf('%.4f', val) + marker : '?'}"
    }.join('\n')
}

def size = 280
def pen = 12
Mnist.load(getClass())

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
                    buttonBar {
                        button('Clear', onAction: {
                            clear(canvas.graphicsContext2D, size)
                            out.text = displayResult([null] * 10, null)
                        })
                        button('Predict', onAction: {
                            def result = Mnist.query(Mnist.scale(imageToArray(snapshot(canvas))))
                            def predictLabel = Mnist.maxIndex(result)
                            out.text = displayResult(result.data.collect{ it[0] }, predictLabel)
                        })
                    }
                }
                bottom {
                    textArea(displayResult([null] * 10, null), id: 'out', editable: false, prefColumnCount: 20)
                }
            }
        }
    }
}
