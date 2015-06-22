package projects.p2.nodes.messages;

import java.util.ArrayList;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class T2Message extends Message {
	public Node sender;
	public ArrayList<Integer> ids;
	public int round;

	@Override
	public Message clone() {
		return this;
	}

	public T2Message(int round, Node sender) {
		this.round = round;
		this.sender = sender;
		this.ids = new ArrayList<>();
	}
	
}
