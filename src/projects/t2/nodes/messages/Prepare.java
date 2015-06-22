package projects.t2.nodes.messages;

import projects.t2.nodes.nodeImplementations.NodeT2;
import sinalgo.nodes.Node;

public class Prepare extends T2Message {
	public NodeT2 coord;
	
	public Prepare(int round, Node sender, NodeT2 coord) {
		super(round,sender);
		this.coord = coord;
	}
}