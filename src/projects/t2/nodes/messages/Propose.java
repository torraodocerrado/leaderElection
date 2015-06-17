package projects.t2.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class Propose extends Message {
	public Node sender;
	public Node coord;

	@Override
	public Message clone() {
		return this;
	}

	public Propose(Node sender, Node coord) {
		this.sender = sender;
		this.coord = coord;
	}
}