package projects.t2.nodes.messages;

import projects.t2.nodes.nodeImplementations.MobileNode;
import sinalgo.nodes.Node;

public class ACK_Prepare extends T2Message {
	public MobileNode coord;
	public MobileNode no;
	
	
	public ACK_Prepare(Node sender, MobileNode coord, MobileNode no) {
		super(sender);
		this.coord = coord;
		this.no = no;
	}
}