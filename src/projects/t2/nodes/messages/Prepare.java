package projects.t2.nodes.messages;

import projects.t2.nodes.nodeImplementations.NodeT2;
import sinalgo.nodes.Node;

public class Prepare extends T2Message {
	public NodeT2 coord;
	
	public Prepare(Node sender, NodeT2 coord) {
		super(sender);
		this.coord = coord;
	}
}