package pt.tecnico.ulisboa.p2pfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;

/**
* This simple example creates 10 nodes, bootstraps to the first and put and get data from those 10 nodes.
*
* @author draft
*/
public class ExampleUtils
{
    private static final Random RND = new Random( 42L );

    /**
* Bootstraps peers to the first peer in the array.
*
* @param peers The peers that should be bootstrapped
*/
    public static void bootstrap( Peer[] peers )
    {
        List<FutureBootstrap> futures1 = new ArrayList<FutureBootstrap>();
        List<FutureDiscover> futures2 = new ArrayList<FutureDiscover>();
        for ( int i = 1; i < peers.length; i++ )
        {
            FutureDiscover tmp = peers[i].discover().setPeerAddress( peers[0].getPeerAddress() ).start();
            futures2.add( tmp );
        }
        for ( FutureDiscover future : futures2 )
        {
            future.awaitUninterruptibly();
        }
        for ( int i = 1; i < peers.length; i++ )
        {
            FutureBootstrap tmp = peers[i].bootstrap().setPeerAddress( peers[0].getPeerAddress() ).start();
            futures1.add( tmp );
        }
        for ( int i = 1; i < peers.length; i++ )
        {
            FutureBootstrap tmp = peers[0].bootstrap().setPeerAddress( peers[i].getPeerAddress() ).start();
            futures1.add( tmp );
        }
        for ( FutureBootstrap future : futures1 )
        {
            future.awaitUninterruptibly();
        }
    }

    /**
* Create peers with a port and attach it to the first peer in the array.
*
* @param nr The number of peers to be created
* @param port The port that all the peer listens to. The multiplexing is done via the peer Id
* @return The created peers
* @throws IOException IOException
*/
    public static Peer[] createAndAttachNodes( int nr, int port ) throws IOException
    {
        Peer[] peers = new Peer[nr];
        for ( int i = 0; i < nr; i++ )
        {
            if ( i == 0 )
            {
                peers[0] = new PeerMaker( new Number160( RND ) ).setPorts( port ).setEnableIndirectReplication(true).makeAndListen();
            }
            else
            {
                peers[i] = new PeerMaker( new Number160( RND ) ).setMasterPeer( peers[0] ).setEnableIndirectReplication(true).makeAndListen();
            }
        }
        return peers;
    }
}