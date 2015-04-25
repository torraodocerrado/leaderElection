package projects.t1.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class Ready extends Message {
	public Node sender;
	public String group;

	@Override
	public Message clone() {
		return this;
	}

	public Ready(Node sender, String group) {
		this.sender = sender;
		this.group = group;
	}

}