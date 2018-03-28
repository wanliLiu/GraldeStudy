import org.gradle.util.ConfigureUtil

class CloserExtension {
    String apiUrl
    String user
    PackageConfig pkg = new PackageConfig()

    def pkg(Closure closure) {
        ConfigureUtil.configure(closure, pkg)
    }

    @Override
    public String toString() {
        return "CloserExtension{" +
                "apiUrl='" + apiUrl + '\'' +
                ", user='" + user + '\'' +
                ", pkg=\n" + pkg.toString() +
                '}'
    }


    class PackageConfig {
        String repo
        //An alternative user for the package
        String userOrg

        VersionConfig version = new VersionConfig()

        def version(Closure closure) {
            ConfigureUtil.configure(closure, version)
        }


        @Override
        public String toString() {
            return "PackageConfig{" +
                    "repo='" + repo + '\'' +
                    ", userOrg='" + userOrg + '\'' +
                    ", version=\n" + version.toString() +
                    '}'
        }
    }

    class VersionConfig {
        String name
        String desc
        String released
        String vcsTag

        @Override
        public String toString() {
            return "VersionConfig{" +
                    "name='" + name + '\'' +
                    ", desc='" + desc + '\'' +
                    ", released='" + released + '\'' +
                    ", vcsTag='" + vcsTag + '\'' +
                    '}'
        }
    }


}