package projects.t1.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class AYC_answer extends Message {
	public Node coord;
	public Node sender;

	@Override
	public Message clone() {
		return this;
	}

	public AYC_answer(Node sender, Node coord) {
		this.sender = sender;
		if (coord == null)
			this.coord = sender;
		else
			this.coord = coord;
	}
}
