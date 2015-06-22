package projects.p2.nodes.messages;

import projects.p2.nodes.nodeImplementations.NodeT2;
import sinalgo.nodes.Node;

public class ACK_Prepare extends T2Message {
	public NodeT2 coord;
	public NodeT2 no;
	
	
	public ACK_Prepare(int round,Node sender, NodeT2 coord, NodeT2 no) {
		super(round, sender);
		this.coord = coord;
		this.no = no;
	}
}