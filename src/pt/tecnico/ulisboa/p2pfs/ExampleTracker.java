package pt.tecnico.ulisboa.p2pfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.tomp2p.futures.FutureTracker;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.TrackerData;
import net.tomp2p.utils.Utils;

/**
* Example of storing friends in a Tracker.
*
* @author Thomas Bocek
*
*/
public final class ExampleTracker {
    
    /**
* Empty constructor.
*/
    private ExampleTracker() {
    }
    
    /**
* Start the examples.
*
* @param args
* Empty
* @throws Exception .
*/
    public static void main(final String[] args) throws Exception {
        Peer[] peers = null;
        try {
            final int peerNr = 100;
            final int port = 5501;
            peers = ExampleUtils.createAndAttachNodes(peerNr, port);
            ExampleUtils.bootstrap(peers);
            MyPeer[] myPeers = wrap(peers);
            example(myPeers);
        } finally {
            // 0 is the master
            if (peers != null && peers[0] != null) {
                peers[0].shutdown();
            }
        }
    }

    /**
* Create MyPeer based on Peer.
*
* @param peers
* All the peers
* @return The converted MyPeers
*/
    private static MyPeer[] wrap(final Peer[] peers) {
        MyPeer[] retVal = new MyPeer[peers.length];
        for (int i = 0; i < peers.length; i++) {
            retVal[i] = new MyPeer(peers[i]);
        }
        return retVal;
    }

    /**
* Starts the example.
*
* @param peers
* All the peers
* @throws IOException .
* @throws ClassNotFoundException .
*/
    private static void example(final MyPeer[] peers) throws IOException, ClassNotFoundException {
        // 3 peers have files
        System.out.println("Setup: we have " + peers.length
                + " peers; peers[12] (Leo) knows Jan, peers[24] (Tim) knows Urs, peers[42] (Pat) knows Tom");
        
        final int peer12 = 12;
        final int peer24 = 24;
        final int peer42 = 42;
        
        peers[peer12].announce("Urs", "Jan");
        peers[peer24].announce("Tom", "Urs");
        peers[peer42].announce("Urs", "Tom");
        // peer 12 now searches for Song B
        System.out.println("peers[24] (Tom) wants to know the friends of Urs");
        peers[peer24].list("Urs");
    }

    /**
* Peer class that deals with friends.
*
* @author Thomas Bocek
*
*/
    private static class MyPeer {
        private final Peer peer;

        private final Map<Number160, String> friends = new HashMap<Number160, String>();

        /**
* @param peer
* The peer that backs this class
*/
        public MyPeer(final Peer peer) {
            this.peer = peer;
            setReplyHandler(peer);
        }

        /**
* Announce my friend list on the DHT and store it in my local map.
*
* @param nickName
* My nickname
* @param friendName
* The name of the friend
* @throws IOException .
*/
        public void announce(final String nickName, final String friendName) throws IOException {
            friends.put(Number160.createHash(nickName), friendName);
            announce();
        }

        /**
* Announce my friend list on the DHT.
*
* @throws IOException .
*/
        public void announce() throws IOException {
            for (Map.Entry<Number160, String> entry : friends.entrySet()) {
                Collection<String> tmp = new ArrayList<String>(friends.values());
                peer.addTracker(entry.getKey()).setAttachement(Utils.encodeJavaObject(tmp.toArray(new String[0])))
                        .start().awaitUninterruptibly();
            }
        }

        /**
* Lists friends that are stored in the DHT.
*
* @param nickName
* The nickname as the location key, where the data is stored
* @throws IOException .
* @throws ClassNotFoundException .
*/
        public void list(final String nickName) throws IOException, ClassNotFoundException {
            Number160 key = Number160.createHash(nickName);
            FutureTracker futureTracker = peer.getTracker(key).start();
            // now we know which peer has this data, and we also know what other things this peer has
            futureTracker.awaitUninterruptibly();
            Collection<TrackerData> trackerDatas = futureTracker.getTrackers();
            for (TrackerData trackerData : trackerDatas) {
                String[] attachement = (String[]) Utils.decodeJavaObject(trackerData.getAttachement(), 0,
                        trackerData.getAttachement().length);
                for (String s1 : attachement) {
                    System.out.println("this peers' (" + nickName + ") friend:" + s1);
                }
            }
            System.out.println("Tracker reports that " + trackerDatas.size() + " peer(s) are his friends");
        }

        /**
* @param peer Set reply handler for peer.
*/
        private void setReplyHandler(final Peer peer) {
            peer.setObjectDataReply(new ObjectDataReply() {
                @Override
                public Object reply(final PeerAddress sender, final Object request) throws Exception {
                    if (request != null && request instanceof Number160) {
                        return friends.get((Number160) request);
                    } else {
                        return null;
                    }
                }
            });
        }
    }
}