import groovy.swing.SwingBuilder

import javax.swing.JComponent
import javax.swing.WindowConstants

class SwingUtil {
    private SwingUtil() {}

    static void show(JComponent component, JComponent... additionalComponents = []) {
        show([:], component, additionalComponents)
    }

    static void show(Map extraArgs, JComponent component, JComponent... additionalComponents = []) {
        showHV(extraArgs, "vbox", component, additionalComponents)
    }

    static void showH(Map extraArgs, JComponent component, JComponent... additionalComponents = []) {
        showHV(extraArgs, "hbox", component, additionalComponents)
    }

    private static void showHV(Map extraArgs, String direction, JComponent component, JComponent... additionalComponents = []) {
        Map frameArgs = [title: 'Frame', size: [800, 600], show: true, defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE]
        frameArgs.putAll(extraArgs)
        new SwingBuilder().edt {
            frame(*:frameArgs) {
                "$direction" {
                    widget(component)
                    additionalComponents.each {
                        widget(it)
                    }
                }
            }
        }
    }
}
