package A4;

import static A4.resources.ProtocolBufferKeyValueStoreResponse.generateGetResponse;
import static A4.resources.ProtocolBufferKeyValueStoreResponse.generatePutResponse;

import A4.proto.KeyValueResponse.KVResponse;
import A4.proto.Message.Msg;
import A4.server.UDPServerThread;
import A4.utils.UniqueIdentifier;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import org.junit.Test;

public class KeyValueStoreIntegrationTest {

    private void spawnUDPServerThread() throws IOException {
        new UDPServerThread("Integration test", 10129).run();
    }

    @Test
    public void testPutGet() throws Exception {
        byte[] messageID = UniqueIdentifier.generateUniqueID();
        byte[] key = new byte[32];
        byte[] value = new byte[32];

        SecureRandom.getInstanceStrong().nextBytes(key);
        SecureRandom.getInstanceStrong().nextBytes(value);

        Msg res = generatePutResponse(ByteString.copyFrom(key), ByteString.copyFrom(value),
            ByteString.copyFrom(messageID));

        byte[] getReqMessageID = UniqueIdentifier.generateUniqueID();
        Msg msg = generateGetResponse(ByteString.copyFrom(key), ByteString.copyFrom(messageID));

        KVResponse resp = KVResponse.parseFrom(msg.getPayload());

        ByteString retrieved = resp.getValue();
        Arrays.equals(value, retrieved.toByteArray());
    }
}