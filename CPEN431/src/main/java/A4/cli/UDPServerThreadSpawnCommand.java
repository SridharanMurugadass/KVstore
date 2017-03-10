package A4.cli;

import static A4.DistributedSystemConfiguration.VERBOSE;

import A4.server.GossipReceiverThread;
import A4.server.GossipSenderThread;
import A4.server.KVOperationThread;
import A4.server.UDPServerThread;
import io.dropwizard.setup.Bootstrap;
import java.util.Random;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class UDPServerThreadSpawnCommand extends io.dropwizard.cli.Command {
    public UDPServerThreadSpawnCommand() {
        super("spawn",
            "Run server that listens, performs operation, and responds to KV requests");
    }

    @Override
    public void configure(Subparser subparser) {
        // Add command line option
        subparser.addArgument("-name")
            .dest("name")
            .type(String.class)
            .required(true)
            .help("Name of host server");

        subparser.addArgument("-port")
            .dest("port")
            .type(Integer.class)
            .required(true)
            .help("Port number to host server");
      
        subparser.addArgument("-nodes")
            .dest("nodes")
            .type(String.class)
            .required(true)
            .help("File containing list of nodes to connect with");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        int port = namespace.getInt("port");
        String name = namespace.getString("name");
        String nodes = namespace.getString("nodes");
        
        if (VERBOSE) {
            System.out.println("Name: " + name);
            System.out.println("Port: " + port);
            System.out.println("Nodes: " + nodes);
        }

        UDPServerThread server = new UDPServerThread(name + "-server-thread", port);
        new GossipReceiverThread(name + "-gossip-receiver-thread", port).start();
        new GossipSenderThread(name + "-gossip-sender-thread", nodes, port).start();
        server.receive();
    }
}
