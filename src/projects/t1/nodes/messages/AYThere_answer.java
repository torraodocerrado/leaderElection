package projects.t1.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class AYThere_answer extends Message {

	public Node sender;
	public boolean answer;
	public int numCoord;

	@Override
	public Message clone() {
		return this;
	}

	public AYThere_answer(Node sender, boolean answer, int numCoord) {
		this.sender = sender;
		this.answer = answer;
		this.numCoord = numCoord;
	}

}
