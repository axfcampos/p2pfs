package pt.tecnico.ulisboa.p2pfs;

/*
* Copyright 2012 Thomas Bocek
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

import java.io.IOException;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

/**
* Example of indirect replication with put.
*
* @author Thomas Bocek
*
*/
public final class ExampleIndirectReplication {

    private static final int ONE_SECOND = 1000;

    /**
* Empty constructor.
*/
    private ExampleIndirectReplication() {
    }

    /**
* Create 3 peers and start the example.
*
* @param args
* Empty
* @throws Exception .
*/
    public static void main(final String[] args) throws Exception {
        exmpleIndirectReplication();
    }

    /**
* Example of indirect replication with put. We store data in the DHT, then peers join that are closer to this data.
* The indirect replication moves the content to the close peers.
     * @throws ClassNotFoundException 
*
* @throws IOException .
* @throws InterruptedException .
*/
    private static void exmpleIndirectReplication() throws IOException, InterruptedException, ClassNotFoundException {
        final int port1 = 9101;
        final int nr1 = 1;
        final int port2 = 9102;
        final int nr2 = 2;
        final int port3 = 9103;
        final int nr3 = 4;
        Peer peer1 = new PeerMaker(new Number160(nr1)).setPorts(port1).setEnableIndirectReplication(true)
        		.setEnableTracker(true).makeAndListen();
               
        Peer peer2 = new PeerMaker(new Number160(nr2)).setPorts(port2).setEnableIndirectReplication(true)
        		.setEnableTracker(true).makeAndListen();
       
        Peer peer3 = new PeerMaker(new Number160(nr3)).setPorts(port3).setEnableIndirectReplication(true)
        		.setEnableTracker(true).makeAndListen();
          
        Peer[] peers = new Peer[] {peer1, peer2, peer3};
        //
        FutureDHT futureDHT = peer1.put(new Number160(nr3)).setRefreshSeconds(2).setData(new Data("store on peer1")).start();
        
        futureDHT.awaitUninterruptibly();
        futureDHT = peer1.get(new Number160(nr3)).start();
        futureDHT.awaitUninterruptibly();
        
        System.out.println(futureDHT.getData().getObject());
        // now peer1 gets to know peer2, transfer the data
        peer1.bootstrap().setPeerAddress(peer2.getPeerAddress()).start();
       
        
        Thread.sleep(ONE_SECOND);
        futureDHT = peer2.get(new Number160(nr3)).setDigest().start();
        futureDHT.awaitUninterruptibly();
        
        System.out.println("we found the data on " + futureDHT.getData().getObject() + " peers");
        
//        futureDHT = peer1.remove(new Number160(nr3)).setRefreshSeconds(2)
//                .setRepetitions(2).start();
        
        futureDHT = peers[2].remove(new Number160(nr3)).setRefreshSeconds(2)
                .setRepetitions(2).setDirectReplication().start();
        
        
        peer1.shutdown();
        
        
        peer2.bootstrap().setPeerAddress(peer3.getPeerAddress()).start();
        
       
        //peer2.put(new Number160(nr3)).setData(new Data("store on peer1")).start();
        
        Thread.sleep(ONE_SECOND * 7);
        futureDHT = peer3.get(new Number160(nr3)).setDigest().start();
        futureDHT.awaitUninterruptibly();
        System.out.println("we found the data on " + futureDHT.getRawDigest().size() + " peers");
        
        
        
        Thread.sleep(ONE_SECOND);
        futureDHT = peer2.get(new Number160(nr3)).setDigest().start();
        futureDHT.awaitUninterruptibly();
        System.out.println("we found the data on " + futureDHT.getRawDigest().size() + " peers");
     
        //peer2.shutdown();
        
        
        Thread.sleep(ONE_SECOND);
        futureDHT = peer3.get(new Number160(nr3)).setDigest().start();
        futureDHT.awaitUninterruptibly();
        System.out.println("we found the data on " + futureDHT.getRawDigest().size() + " peers");
        //shutdown(peers);
        peer3.shutdown();
        peer2.shutdown();
    }

    /**
* Shutdown all the peers.
*
* @param peers
* The peers in this P2P network
*/
    private static void shutdown(final Peer[] peers) {
        for (Peer peer : peers) {
            peer.shutdown();
        }
    }
}