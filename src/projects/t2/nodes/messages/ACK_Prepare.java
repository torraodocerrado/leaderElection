package projects.t2.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class ACK_Prepare extends Message {
	public Node sender;

	@Override
	public Message clone() {
		return this;
	}

	public ACK_Prepare(Node sender) {
		this.sender = sender;
	}
}