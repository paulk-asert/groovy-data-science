import weka.core.WekaPackageManager

def pkgMgr = new WekaPackageManager()
def packages = ['LVQ', 'cascadeKMeans', 'GenClustPlusPlus', 'XMeans', 'SelfOrganizingMap']
packages.each { pkgName ->
    def pkg = pkgMgr.getRepositoryPackageInfo(pkgName)
    if (pkg.installed) println "$pkgName already installed"
    else {
        pkg.install()
        println "Installing $pkgName ($pkg.packageMetaData.Category):"
        println "Title: $pkg.packageMetaData.Title"
        println "Description: $pkg.packageMetaData.Description"
    }
}
