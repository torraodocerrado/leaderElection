package projects.t2.nodes.messages;

import projects.t2.nodes.nodeImplementations.MobileNode;
import sinalgo.nodes.Node;

public class Prepare extends T2Message {
	public MobileNode coord;
	
	public Prepare(Node sender, MobileNode coord) {
		super(sender);
		this.coord = coord;
	}
}