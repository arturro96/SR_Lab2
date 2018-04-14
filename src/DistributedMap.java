import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.STATE_TRANSFER;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class DistributedMap extends ReceiverAdapter implements SimpleStringMap {

    private Map<String, String> map;
    private JChannel channel;
    private ProtocolStack protocolStack;

    public DistributedMap() throws Exception {
        this.map = new HashMap<String, String>();
        this.channel = new JChannel(false);
        initProtocol();
        channel.setReceiver(this);
        channel.connect("MapCluster");
        channel.getState(null, 10000);

    }

    private void initProtocol() throws Exception{
        this.protocolStack = new ProtocolStack();
        this.channel.setProtocolStack(protocolStack);
        protocolStack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName("230.0.0.100")))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE_TRANSFER())
                .addProtocol(new SEQUENCER());

        protocolStack.init();
    }

    public void printMap(){
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

    private void updateDistributedMap(MapMessage mapmsg){
        Message msg = new Message(null, null, mapmsg);
        try {
            channel.send(msg);
        } catch (Exception e) {
            System.out.println("Update Map failed"  + e.getMessage() );
        }
    }

    public void receive(Message msg) {
        MapMessage mapmsg = (MapMessage) msg.getObject();
        if (mapmsg.getOperationType().equals(OperationType.PUT))
            this.map.put(mapmsg.getKey(), mapmsg.getValue());
        else
            this.map.remove(mapmsg.getKey());
    }

    public void setState(InputStream input) throws Exception {
        Map<String, String> mapToSet = (HashMap<String, String>) Util.objectFromStream(new DataInputStream(input));
        synchronized(map) {
            map.clear();
            map.putAll(mapToSet);
        }
    }

    public void getState(OutputStream output) throws Exception {
        synchronized(map) {
            Util.objectToStream(map, new DataOutputStream(output));
        }
    }

    @Override
    public boolean containsKey(String key) {
        return this.map.containsKey(key);
    }

    @Override
    public String get(String key) {
        return this.map.get(key);
    }

    @Override
    public String put(String key, String value) {
        if (!this.map.containsKey(key)) {
            this.map.put(key, value);
            updateDistributedMap(new MapMessage(key, value, OperationType.PUT));
            return key + ":" + value;
        }
        System.out.println("Key is already in map");
        return null;
    }

    @Override
    public String remove(String key) {
        if (this.map.containsKey(key)) {
            this.map.remove(key);
            updateDistributedMap(new MapMessage(key, OperationType.REMOVE));
            return key;
        }
        return null;
    }

    @Override
    public void viewAccepted(View view) {
        if(view instanceof MergeView) {
            ViewHandler viewHandler = new ViewHandler(channel, (MergeView) view);
            viewHandler.start();
        }
    }
}
