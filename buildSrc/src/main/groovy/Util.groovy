class Util {
    private Util() {}
    static List<String> baseNames(File subdir, List<String> exclusions = []) {
        subdir.listFiles()*.name
                .findAll{it.endsWith('.groovy')}
                .collect{it - '.groovy'}
                .findAll{ !(it in exclusions) }
    }
}