package org.kutsuki.matchaserver.dao;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.kutsuki.matchaserver.EmailManager;

public final class DaoManager {
    private static final String HOSTNAME = "70.35.201.164";

    public static final EmailManager EMAIL = new EmailManager();
    public static final EventDao EVENT = new EventDao();
    public static final HotelDao HOTEL = new HotelDao();
    public static final LocationDao LOCATION = new LocationDao();
    public static final RoomDao ROOM = new RoomDao();

    private static final int PORT = 9300;
    private static final String CLUSTER_NAME = "cluster.name";
    private static final String XANADU_CLUSTER = "xanaduCluster";

    private static final TransportClient CLIENT;

    static {
	Settings settings = Settings.builder().put(CLUSTER_NAME, XANADU_CLUSTER).build();
	CLIENT = new PreBuiltTransportClient(settings);

	try {
	    CLIENT.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(HOSTNAME), PORT));
	} catch (UnknownHostException e) {
	    EMAIL.emailException("Error getting host by name: " + HOSTNAME, e);
	}

	// close on shutdown
	Runtime.getRuntime().addShutdownHook(new Thread() {
	    @Override
	    public void run() {
		if (DaoManager.getClient() != null) {
		    DaoManager.getClient().close();
		}
	    }
	});
    }

    // private constructor
    private DaoManager() {
	// do nothing
    }

    // getClient
    public static TransportClient getClient() {
	return CLIENT;
    }
}
