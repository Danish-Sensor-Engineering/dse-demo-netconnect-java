package dse.cli.connect;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

@Command(name = "dse-cli-connect", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class Application implements Callable<Integer> {


    @CommandLine.Parameters(index = "0", defaultValue = "localhost") InetAddress host;
    @CommandLine.Parameters(index = "1", defaultValue = "2730") int port;

    @CommandLine.Parameters(hidden = true)  // "hidden": don't show this parameter in usage help message
    ArrayList<String> allParameters; // no "index" attribute: captures _all_ arguments


    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }


    @Override
    public Integer call() throws IOException, InterruptedException {

        // Setup network and data processing
        ConnectClient connectClient;
        try {
            connectClient = new ConnectClient(host.getHostAddress(), port);
        } catch (ConnectException e) {
            System.err.printf("Could not connect to %s:%d%n", host.getHostAddress(), port);
            return 1;
        }

        // Run data processing in a thread
        new Thread(connectClient).start();

        // Run for some time
        sleep( 1000);

        // Stop the data processor and disconnect
        connectClient.stop();


        return 0;
    }

}
