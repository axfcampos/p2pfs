package pt.ulisboa.tecnico.p2pfs.kademlia;

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

import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
/**
* Example of indirect replication with put.
*
* @author Thomas Bocek
*
*/
public final class TheFirstPeer {

    /**
* Empty constructor.
*/
    private TheFirstPeer() {
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
        
        new PeerMaker(new Number160(nr1)).setPorts(port1).setEnableIndirectReplication(true)
        		.setEnableTracker(true).makeAndListen();
        
    }
}