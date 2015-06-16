package projects.t2.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JOptionPane;

import projects.t2.nodes.messages.ByeBye;
import projects.t2.nodes.messages.InviteMessage;
import projects.t2.nodes.messages.SmsAckMessage;
import projects.t2.nodes.messages.SmsMessage;
import projects.t2.nodes.messages.SubscirbeMessage;
import projects.t2.nodes.timers.SmsTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.helper.Arrow;
import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

public class MobileNode extends Node {

	Logging log = Logging.getLogger();// ("smsLog.txt");

	Antenna currentAntenna = null;
	private int seqIDCounter = 0;

	public Antenna getCurrentAntenna() {
		return currentAntenna;
	}

	public int getNextSeqID() {
		return ++seqIDCounter;
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void handleMessages(Inbox inbox) {
	}

	public MobileNode() {
		try {
			this.defaultDrawingSizeInPixels = Configuration
					.getIntegerParameter("MobileNode/Size");
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}

	public String toString() {
		if (currentAntenna != null) {
			return "Connected to Antenna " + currentAntenna.ID;
		} else {
			return "Currently not connected.";
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void preStep() {
		Connections no = this.outgoingConnections;
		boolean has = false;
		if (no.size() == 0) {
			this.currentAntenna = null;
		} else {
			for (Edge edge : no) {
				if (edge.endNode instanceof Antenna){
					this.currentAntenna = (Antenna) edge.endNode;
					has = true;
					break;					
				}
			}
			if(!has){
				this.currentAntenna = null;
				this.outgoingConnections.removeAndFreeAllEdges();
			}
		}
	}

	@Override
	public void postStep() {
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		this.drawAsDisk(g, pt, highlight, this.drawingSizeInPixels);
		this.drawNodeAsDiskWithText(g, pt, highlight, String.valueOf(this.ID),
				20, Color.BLACK);
		if (this.IamAlone()) {
			this.setColor(Color.RED);
		} else {
			this.setColor(Color.BLUE);
		}
	}

	private boolean IamAlone() {
		return this.currentAntenna == null;
	}

}
