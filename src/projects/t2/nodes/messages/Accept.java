package projects.t2.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class Accept extends Message {
	public Node sender;

	@Override
	public Message clone() {
		return this;
	}

	public Accept(Node sender) {
		this.sender = sender;
	}
}
