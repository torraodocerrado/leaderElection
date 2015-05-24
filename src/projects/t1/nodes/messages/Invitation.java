package projects.t1.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class Invitation extends Message {
	public Node sender;
	public int coordenatorCount;

	@Override
	public Message clone() {
		return this;
	}

	public Invitation(Node coordenator, int coordenatorCount) {
		this.sender = coordenator;
		this.coordenatorCount = coordenatorCount;
	}

}