package projects.t2.nodes.messages;

import projects.t2.nodes.nodeImplementations.NodeT2;
import sinalgo.nodes.Node;

public class ACK_Accept  extends T2Message {
	public NodeT2 coord;
	public NodeT2 no;

	public ACK_Accept(int round, Node sender, NodeT2 coord, NodeT2 no) {
		super(round, sender);
		this.coord = coord;
		this.no = no;
	}
}