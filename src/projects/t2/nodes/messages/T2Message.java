package projects.t2.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class T2Message extends Message {
	public Node sender;

	@Override
	public Message clone() {
		return this;
	}

	public T2Message(Node sender) {
		this.sender = sender;
	}
}
