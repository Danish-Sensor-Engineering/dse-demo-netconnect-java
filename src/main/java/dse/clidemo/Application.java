package dse.clidemo;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.concurrent.Callable;

@Command(name = "clidemo",
        mixinStandardHelpOptions = true,
        versionProvider = dse.clidemo.VersionProvider.class,
        defaultValueProvider = dse.clidemo.DefaultProvider.class)
public class Application implements Callable<Integer> {


    @Option(names = { "-d", "--debug" }, description = "Enable debugging [default: false].")
    private boolean[] enableDebug = new boolean[0];


    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }


    @Override
    public Integer call() {


        switch (enableDebug.length) {
            case 1:
                System.setProperty("org.slf4j.simpleLogger.defaultLogLevel" , "DEBUG");
                break;
            case 2:
                System.setProperty("org.slf4j.simpleLogger.defaultLogLevel ", "TRACE");
                break;
        }

        return 0;
    }

}
