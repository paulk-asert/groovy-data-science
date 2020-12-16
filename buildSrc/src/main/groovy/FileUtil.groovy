class FileUtil {
    private FileUtil() {}

    static List<String> baseNames(File subdir, List<String> exclusions = [], String ext = '.groovy') {
        subdir.listFiles()*.name
                .findAll { it.endsWith(ext) }
                .collect { it - ext }
                .findAll { !(it in exclusions) && !it.endsWith('Util') }
    }
}
