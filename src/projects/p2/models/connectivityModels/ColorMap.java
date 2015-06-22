package projects.p2.models.connectivityModels;

import java.util.Enumeration;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.io.mapIO.Map;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;
import sinalgo.runtime.Runtime;
import sinalgo.tools.Tools;

public class ColorMap  extends ConnectivityModelHelper {




	@Override
	protected boolean isConnected(Node from, Node to) {
		Map map = Tools.getBackgroundMap();
		return map.getColor(from.getPosition()).equals(map.getColor(to.getPosition()));
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
							true) || edgeAdded; 
				}
			}
		}
		boolean dyingLinks = n.outgoingConnections.removeInvalidLinks();
		return edgeAdded || dyingLinks; 
	}

}
