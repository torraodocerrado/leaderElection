package projects.t2.nodes.messages;

import projects.t2.nodes.nodeImplementations.MobileNode;
import sinalgo.nodes.Node;

public class Accept extends T2Message {
	public MobileNode coord;

	public Accept(Node sender, MobileNode coord) {
		super(sender);
		this.coord = coord;
	}
}
