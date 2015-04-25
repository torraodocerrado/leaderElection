package projects.t1.nodes.messages;

import sinalgo.nodes.messages.Message;

public class CheckMembers extends Message{
	public int idCoordenador;
	
	@Override
	public Message clone() {
		return null;
	}
	
	public CheckMembers(int idCoordenador){
		this.idCoordenador = idCoordenador;
	}

}
