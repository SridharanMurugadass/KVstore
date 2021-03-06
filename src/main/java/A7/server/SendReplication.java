package A7.server;

import static A7.DistributedSystemConfiguration.MAX_REP_PAYLOAD_SIZE;
import static A7.utils.UniqueIdentifier.generateUniqueID;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import com.google.protobuf.ByteString;

import A7.client.UDPClient;
import A7.core.KeyValueStoreSingleton;
import A7.core.VersionedValue;
import A7.proto.Message.Msg;
import A7.resources.ProtocolBufferKeyValueStoreRequest;
import A7.utils.MsgWrapper;

public class SendReplication implements Runnable {    
	MsgWrapper sendLocation;
	ConcurrentHashMap<ByteString, VersionedValue> copyMap;
	public SendReplication(MsgWrapper received) {
		this.sendLocation = received;
		copyMap = new ConcurrentHashMap<ByteString, VersionedValue>();
		copyMap.putAll(KeyValueStoreSingleton.getInstance().getMap());
	}

	// create submap from index "from" to index "to" (exclusive)
	protected ByteString createSubMap(int from, int to) throws IOException {
		// Populate new hashMap from ranges provided
		ConcurrentHashMap<ByteString, VersionedValue> newMap = new ConcurrentHashMap<>();
		Object[] keySet = copyMap.keySet().toArray();

		// Map has changed since beginning of recursion, abort
		for (int i = from; i < to; i++) {
			newMap.put((ByteString) keySet[i],
					copyMap.get(keySet[i]));
		}
		// Nothing in range to serialize; return null
		// (ie. do not serialize the HashMap object itself if there are no keys inside)
		if (newMap.size() == 0) {
			return null;
		}
		// Write object to ByteString
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(newMap);
        out.flush();
        return ByteString.copyFrom(bos.toByteArray());
	}

	protected void sendDupeRequestMsg(ByteString value) {
		byte[] messageID = new byte[0];

		try {
			messageID = generateUniqueID();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		Msg dupeMsg = ProtocolBufferKeyValueStoreRequest.generateDupesRequest(
		    value, ByteString.copyFrom(messageID));

		// send duplication request optimistically, doesn't wait for response
		try {
			UDPClient.sendReplicaRequest(
                dupeMsg.toByteArray(),
                sendLocation.getAddress().getHostAddress(),
                sendLocation.getPort());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void serveReplication(int from, int mid, int to) {
		try {
			ByteString headPayload = createSubMap(from, mid);
			
			if (headPayload != null) {
				if (headPayload.size() > MAX_REP_PAYLOAD_SIZE) {
					int midKey = from + (mid - from) / 2;
					serveReplication(from, midKey, mid);
				} else if (headPayload.size() != 0) {
					sendDupeRequestMsg(headPayload);
				}
			}
			
			ByteString tailPayload = createSubMap(mid, to);
			
			if (tailPayload != null) {
				if (tailPayload.size() > MAX_REP_PAYLOAD_SIZE) {
					int midKey = mid + (to - mid) / 2 ;
					serveReplication(mid, midKey, to);
				} else if (headPayload.size() != 0) {
					sendDupeRequestMsg(tailPayload);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    @Override
    public void run() {
    	Object[] keySet = copyMap.keySet().toArray();
    	int from = 0;
    	int mid = keySet.length;
    	int to = keySet.length;
    	serveReplication(from, mid, to);
    }	
}
