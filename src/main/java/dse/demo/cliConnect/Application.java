package dse.demo.cliConnect;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.Callable;

@Command(name = "dse-cli-connect", mixinStandardHelpOptions = true, versionProvider = dse.demo.cliConnect.VersionProvider.class)
public class Application implements Callable<Integer> {


    @CommandLine.Parameters(index = "0") InetAddress host;
    @CommandLine.Parameters(index = "1", defaultValue = "2730") int port;

    @CommandLine.Parameters(hidden = true)  // "hidden": don't show this parameter in usage help message
    ArrayList<String> allParameters; // no "index" attribute: captures _all_ arguments


    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }


    @Override
    public Integer call() {

        return 0;
    }

}
