package pt.tecnico.ulisboa.p2pfs;


import java.io.IOException;

import net.tomp2p.futures.FutureCreate;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Timings;

/**
* Example of direct replication with put and remove.
*
* @author Thomas Bocek
*
*/
public final class ExampleDirectReplication {

    private static final int NINE_SECONDS = 9 * 1000;

    /**
* Empty constructor.
*/
    private ExampleDirectReplication() {
    }

    /**
* Create 100 peers and start the example.
*
* @param args
* Empty
* @throws Exception .
*/
    public static void main(final String[] args) throws Exception {
        Peer[] peers = null;
        try {
            final int nrPeers = 100;
            final int port = 4301;
            peers = ExampleUtils.createAndAttachNodes(nrPeers, port);
            ExampleUtils.bootstrap(peers);
            exmpleDirectReplication(peers);
        } finally {
            if (peers != null && peers[0] != null) {
                peers[0].shutdown();
            }
        }
    }

    /**
* The example first stores data and pushed it a couple of times using direct replication. After, it removes the
* content, calling remove twice.
*
* @param peers
* The peers in this P2P network
* @throws IOException .
*/
    private static void exmpleDirectReplication(final Peer[] peers) throws IOException {
        FutureCreate<FutureDHT> futureCreate1 = new FutureCreate<FutureDHT>() {
            @Override
            public void repeated(final FutureDHT future) {
                System.out.println("put again...");
            }
        };
        FutureDHT futureDHT = peers[1].put(Number160.ONE).setData(new Data("test")).setFutureCreate(futureCreate1)
                .setRefreshSeconds(2).start();
        Timings.sleepUninterruptibly(NINE_SECONDS);
        System.out.println("stop replication");
        futureDHT.shutdown();
        Timings.sleepUninterruptibly(NINE_SECONDS);
        FutureCreate<FutureDHT> futureCreate2 = new FutureCreate<FutureDHT>() {
            @Override
            public void repeated(final FutureDHT future) {
                System.out.println("remove again...");
            }
        };
        
        futureDHT = peers[1].get(Number160.ONE).setDigest().start();
        futureDHT.awaitUninterruptibly();
        System.out.println("we found the data on " + futureDHT.getRawDigest().size() + " peers");
        
        futureDHT = peers[1].remove(Number160.ONE).setFutureCreate(futureCreate2).setRefreshSeconds(2)
                .setRepetitions(2).setDirectReplication().start();
        Timings.sleepUninterruptibly(NINE_SECONDS);
        
        futureDHT = peers[1].get(Number160.ONE).setDigest().start();
        futureDHT.awaitUninterruptibly();
        System.out.println("we found the data on " + futureDHT.getRawDigest().size() + " peers");
        
    }

}