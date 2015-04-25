package projects.t1.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class AYC_answer extends Message {
	public Node coord;
	public Node node;

	@Override
	public Message clone() {
		return this;
	}

	public AYC_answer(Node node, Node coord) {
		this.coord = coord;
		this.node = node;
	}
}
