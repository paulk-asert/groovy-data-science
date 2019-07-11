import groovy.swing.SwingBuilder

import javax.swing.JComponent
import javax.swing.WindowConstants

class SwingUtil {
    private SwingUtil() {}

    static void show(JComponent component) {
        show([:], component)
    }

    static void show(Map extraArgs, JComponent component) {
        Map frameArgs = [title: 'Frame', size: [800, 600], show: true, defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE]
        frameArgs.putAll(extraArgs)
        new SwingBuilder().edt {
            frame(*:frameArgs) {
                widget(component)
            }
        }
    }
}
