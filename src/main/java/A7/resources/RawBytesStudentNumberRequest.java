package A7.resources;

import static A7.DistributedSystemConfiguration.VERBOSE;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;

public class RawBytesStudentNumberRequest {
    // request size is 20 bytes
    private static final int REQ_UDP_SIZE = 20;

    /*
    Request message format: the first 16 bytes are the request’s unique ID,
    the rest is the application level payload.
    */
    public static byte[] generateRequest(int snum, byte[] uniqueID)
        throws NoSuchAlgorithmException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(REQ_UDP_SIZE);
        byteBuffer.limit(REQ_UDP_SIZE);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(uniqueID);
        /*
        The client’s payload (student id) is an integer (4 bytes), in little-endian format.
        8 digit student number, each hex digit is 4 bits, 4*8 = 32 bits = 4 bytes
        */
        byteBuffer.putInt(snum); // snum entered in big-endian order
        if (VERBOSE > 0) {
            System.out.println("Position: " + byteBuffer.position());
            System.out.println("Remaining: " + byteBuffer.remaining());
        }
        return byteBuffer.array();
    }
}
