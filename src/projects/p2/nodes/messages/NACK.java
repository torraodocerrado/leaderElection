package projects.p2.nodes.messages;

import sinalgo.nodes.Node;

public class NACK extends T2Message{
	public Node to;

	public NACK(int round, Node sender, Node to) {
		super(round, sender);
		this.to = to;
	}

}
