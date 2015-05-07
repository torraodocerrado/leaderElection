package projects.t1.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class Accept extends Message {
	public int coordenatorCount;
	public Node sender;

	@Override
	public Message clone() {
		return this;
	}

	public Accept(Node sender, int coordenatorCount) {
		this.sender = sender;
		this.coordenatorCount = coordenatorCount;
	}
}
