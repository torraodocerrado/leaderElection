package projects.leader.nodes.timers;

import java.util.ArrayList;

import projects.leader.nodes.messages.NetworkMessage;
import projects.leader.nodes.nodeImplementations.SimpleNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class NetworkMessageTimer extends Timer {
	private NetworkMessage message = null;

	public NetworkMessageTimer(NetworkMessage message) {
		this.message = message;
	}

	@Override
	public void fire() {
		switch(this.message.tipoMsg){
			case 0:
				this.fireLeaderElectionMsg();
				break;
			case 1:
				break;
			case 2:
				this.firePingMsg();
				break;
			case 3:
				break;
			case 4:
				break;
		}
	}
	
	private void fireLeaderElectionMsg(){
		ArrayList<SimpleNode> neighborhoods = ((SimpleNode) node).getHigherIDNeighborhoods();
		
		for(SimpleNode sn : neighborhoods){
			((SimpleNode) node).send(this.message, sn);
		}
	}
	
	private void firePingMsg(){
		Node networkLeader = ((SimpleNode) node).getNetworkLeader();
		this.node.send(this.message, networkLeader);
	}
}
