class JavaFXUtil {
    private JavaFXUtil() {}

// this should really be checking for jfxrt.jar in sourceSets.main.runtimeClasspath
// but we can't do that prior to configuration and we don't do anything fancy like
// using toolchains so it will do here
    static boolean checkForJavaFX() {
        try {
            Class.forName('javafx.beans.DefaultProperty')
            return true
        } catch (Exception ignore) {
            return false
        }
    }
}
