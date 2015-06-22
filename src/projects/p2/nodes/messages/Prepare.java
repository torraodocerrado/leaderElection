package projects.p2.nodes.messages;

import projects.p2.nodes.nodeImplementations.NodeT2;
import sinalgo.nodes.Node;

public class Prepare extends T2Message {
	public NodeT2 coord;
	
	public Prepare(int round, Node sender, NodeT2 coord) {
		super(round,sender);
		this.coord = coord;
	}
}