package A2;

import io.dropwizard.Configuration;

public class DistributedSystemConfiguration extends Configuration {
    // Enable debugging print statements
    public static final boolean VERBOSE = false;
    public static final int UNIQUE_ID_UDP_SIZE = 16;
}
