class FileUtil {
    private FileUtil() {}

    static List<String> baseNames(File subdir, List<String> exclusions = [], String ext = '.groovy') {
        baseNames(subdir.listFiles().toList(), exclusions, ext)
    }

    static List<String> baseNames(Collection<File> files, List<String> exclusions = [], String ext = '.groovy') {
        files*.name
                .findAll { it.endsWith(ext) }
                .collect { it - ext }
                .findAll { !(it in exclusions) && !it.endsWith('Util') }
    }
}
