package projects.t2.models.connectivityModels;

import java.util.Enumeration;

import projects.t2.nodes.nodeImplementations.Antenna;
import projects.t2.nodes.nodeImplementations.MobileNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;
import sinalgo.runtime.Global;
import sinalgo.runtime.Main;
import sinalgo.runtime.Runtime;

/**
 * Implements a connection from a node to the antenna.
 */
public class AntennaConnection extends ConnectivityModelHelper {

	private static boolean initialized = false;
	private static double rMaxSquare; 

	public AntennaConnection() throws CorruptConfigurationEntryException {
		if (!initialized) { // only initialize once
			double geomNodeRMax = Configuration
					.getDoubleParameter("GeometricNodeCollection/rMax");
			try {
				rMaxSquare = Configuration.getDoubleParameter("UDG/rMax");
			} catch (CorruptConfigurationEntryException e) {
				Global.log
						.logln("\nWARNING: Did not find an entry for UDG/rMax in the XML configuration file. Using GeometricNodeCollection/rMax.\n");
				rMaxSquare = geomNodeRMax;
			}
			if (rMaxSquare > geomNodeRMax) {											
				Main.minorError("WARNING: The maximum transmission range used for the UDG connectivity model is larger than the maximum transmission range specified for the GeometricNodeCollection.\nAs a result, not all connections will be found! Either fix the problem in the project-specific configuration file or the '-overwrite' command line argument.");
			}
			rMaxSquare = rMaxSquare * rMaxSquare;
			initialized = true;
		}
	}

	protected boolean isConnected(Node from, Node to) {
		if ((to instanceof Antenna && from instanceof MobileNode)
				|| (from instanceof Antenna && to instanceof MobileNode)) {
			double dist = from.getPosition().squareDistanceTo(to.getPosition());
			return dist < rMaxSquare;
		}

		if (from instanceof Antenna && to instanceof Antenna) {
			return true;
		}

		return false;
	}

	@Override
	public boolean updateConnections(Node n) throws WrongConfigurationException {
		boolean edgeAdded = false;
		Enumeration<Node> pNE = Runtime.nodes.getNodeEnumeration();
		
		while (pNE.hasMoreElements()) {
			Node possibleNeighbor = pNE.nextElement();
			if (n.ID != possibleNeighbor.ID) {
				if (isConnected(n, possibleNeighbor)) {
					edgeAdded = !n.outgoingConnections.add(n, possibleNeighbor,
							true) || edgeAdded; // note: don't write it the
												// other way round, otherwise,
												// the edge is not added if
												// edgeAdded is true.
				}
			}
		}
		boolean dyingLinks = n.outgoingConnections.removeInvalidLinks();

		return edgeAdded || dyingLinks; // return whether an edge has been added
										// or removed.
	}

}
