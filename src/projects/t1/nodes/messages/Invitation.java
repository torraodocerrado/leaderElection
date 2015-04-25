package projects.t1.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class Invitation extends Message {
	public Node sender;
	public String group;

	@Override
	public Message clone() {
		return this;
	}

	public Invitation(Node sender, String group) {
		this.sender = sender;
		this.group = group;
	}

}