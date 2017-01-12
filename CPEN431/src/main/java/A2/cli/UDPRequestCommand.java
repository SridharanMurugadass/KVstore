package A2.cli;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import A2.client.UDPClient;
import A2.resources.StudentNumberResponse;
import io.dropwizard.setup.Bootstrap;

import static A2.DistributedSystemConfiguration.VERBOSE;
import static A2.resources.StudentNumberRequest.generateRequest;
import static A2.resources.StudentNumberRequest.generateUniqueID;
import static A2.utils.ByteRepresentation.bytesToHex;

public class UDPRequestCommand extends io.dropwizard.cli.Command {
    public UDPRequestCommand() {
        super("request", "Send student number UDP request");
    }

    @Override
    public void configure(Subparser subparser) {
        // Add command line option
        subparser.addArgument("-ip")
                .dest("ip")
                .type(String.class)
                .required(true)
                .help("IP address to send request");

        subparser.addArgument("-port")
                .dest("port")
                .type(Integer.class)
                .required(true)
                .help("Port number of request");

        subparser.addArgument("-snum")
                .dest("snum")
                .type(Integer.class)
                .required(true)
                .help("Student number to send");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        String ip = namespace.getString("ip");
        int port = namespace.getInt("port");
        int snum = namespace.getInt("snum");

        if (VERBOSE) {
            System.out.println("IP Address: " + ip);
            System.out.println("Port: " + port);
            System.out.println("Student Number: " + snum);
        }

        byte[] uniqueID = generateUniqueID();
        byte[] req = generateRequest(snum, uniqueID);

        if (VERBOSE) {
            System.out.println("Request HEX String: " + bytesToHex(req));
        }

        System.out.println("Sending ID: " + snum);

        byte[] res = UDPClient.sendRequest(req, ip, port, uniqueID);
        StudentNumberResponse.parseResponse(res, uniqueID);
    }
}
