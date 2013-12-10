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

import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaDto;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaEntryDto;
import pt.ulisboa.tecnico.p2pfs.fuse.MemoryDirectory;
import pt.ulisboa.tecnico.p2pfs.fuse.MemoryFile;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.ShortString;
import net.tomp2p.storage.Data;

/**
* Example of indirect replication with put.
*
* @author Thomas Bocek
*
*/
public final class TheFirstPeer {

    private static final int ONE_SECOND = 1000;

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
        
        Peer peer1 = new PeerMaker(new Number160(nr1)).setPorts(port1).setEnableIndirectReplication(true)
        		.setEnableTracker(true).makeAndListen();
        
        FuseKademliaDto dir = new FuseKademliaDto("/");
        
        dir.addContent(new FuseKademliaEntryDto("Sample file.txt",'f'));
        dir.addContent(new FuseKademliaEntryDto("Sample file 2.txt",'f'));
		dir.addContent(new FuseKademliaEntryDto("Sample directory", 'd'));
		dir.addContent(new FuseKademliaEntryDto("Directory with files", 'd'));
		//dirWithFiles.add(new MemoryFile("hello.txt", "This is some sample text.\n"));
		//dirWithFiles.add(new MemoryFile("hello again.txt", "This another file with text in it! Oh my!\n"));
		//final MemoryDirectory nestedDirectory = new MemoryDirectory("Sample nested directory");
		//dirWithFiles.add(nestedDirectory);
		//nestedDirectory.add(new MemoryFile("So deep.txt", "Man, I'm like, so deep in this here file structure.\n"));
		
        /*
        FutureDHT futureDHT = peer1.put(Number160.createHash("joao-file-/"))
				 .setRefreshSeconds(2).setData(new Data(dir)).start();
		futureDHT.awaitUninterruptibly();
        
		futureDHT = peer1.put(Number160.createHash("joao-/Sample file.txt"))
				 .setRefreshSeconds(2).setData(new Data("ola")).start();
		futureDHT.awaitUninterruptibly();
		
		futureDHT = peer1.get(Number160.createHash("joao-/Sample file.txt")).start();
        futureDHT.awaitUninterruptibly();
        
        System.out.println(futureDHT.getData().getObject());
		
		
		futureDHT = peer1.put(Number160.createHash("joao-/Sample file 2.txt"))
				 .setRefreshSeconds(2).setData(new Data("ola2")).start();
		futureDHT.awaitUninterruptibly();
		
		futureDHT = peer1.get(Number160.createHash("joao-/Sample file.txt")).start();
        futureDHT.awaitUninterruptibly();
        
        System.out.println(futureDHT.getData().getObject());
		
        */
    }
}