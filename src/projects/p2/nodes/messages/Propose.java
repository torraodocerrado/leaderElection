package projects.p2.nodes.messages;

import sinalgo.nodes.Node;

public class Propose extends T2Message {
	public Node coord;

	public Propose(int round, Node sender, Node coord) {
		super(round, sender);
		this.coord = coord;
	}
}