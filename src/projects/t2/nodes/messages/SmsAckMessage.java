package projects.t2.nodes.messages;

import projects.t2.nodes.timers.SmsTimer;
import sinalgo.nodes.Node;

public class SmsAckMessage extends SmsMessage {
	public int smsSeqID;  // the sequence ID of the message to ACK
	
	public SmsAckMessage(int aSeqID, Node aReceiver, Node aSender, String aText, SmsTimer aTimer) {
		super(aSeqID, aReceiver, aSender, aText, aTimer);
	}
}
