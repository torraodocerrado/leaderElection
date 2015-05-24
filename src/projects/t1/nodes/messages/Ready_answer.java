package projects.t1.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class Ready_answer extends Message {
	public Node sender;
	public int coordenatorCount;
	public boolean accept;

	@Override
	public Message clone() {
		return this;
	}

	public Ready_answer(Node node, int coordenatorCount, boolean accept) {
		this.sender = node;
		this.coordenatorCount = coordenatorCount;
		this.accept = accept;
	}
}
