package depgraph;

public class Dependency {

    final String name;
    final String version;
    final String framework;

    public Dependency(String name, String version, String framework) {
        this.name = name;
        this.version = version;
        this.framework = framework;
    }
}
