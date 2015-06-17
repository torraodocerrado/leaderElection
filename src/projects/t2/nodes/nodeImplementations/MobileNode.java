package projects.t2.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.t2.nodes.messages.ACK_Accept;
import projects.t2.nodes.messages.Accept;
import projects.t2.nodes.messages.ACK_Prepare;
import projects.t2.nodes.messages.Decided;
import projects.t2.nodes.messages.Prepare;
import projects.t2.nodes.messages.Propose;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Connections;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

public class MobileNode extends NodeT2 {

	Antenna currentAntenna = null;
	int count_prepare = 0;
	int count_accept = 0;

	public Antenna getCurrentAntenna() {
		return currentAntenna;
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message message = inbox.next();
			if (message instanceof Propose) {
				logMsg("Propose by " + ((Propose) message).sender.ID);
				this.co_readPropose((Propose) message);
			}
			if (message instanceof Prepare) {
				logMsg("Prepare by " + ((Prepare) message).sender.ID);
				this.no_readPrepare((Prepare) message);
			}
			if (message instanceof ACK_Prepare) {
				logMsg("ACK_Prepare by " + ((ACK_Prepare) message).sender.ID);
				this.co_readACK_Prepare((ACK_Prepare) message);
			}
			if (message instanceof Accept) {
				logMsg("Accept by " + ((Accept) message).sender.ID);
				this.no_readAccept((Accept) message);
			}
			if (message instanceof ACK_Accept) {
				logMsg("Accept by " + ((ACK_Accept) message).sender.ID);
				this.co_readACK_Accept((ACK_Accept) message);
			}
		}
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void preStep() {
		this.checkAntenna();
		this.checkTimeOut();
	}

	public void checkAntenna() {
		Connections no = this.outgoingConnections;
		boolean has = false;
		if (no.size() == 0) {
			this.currentAntenna = null;
		} else {
			for (Edge edge : no) {
				if (edge.endNode instanceof Antenna) {
					if ((this.currentAntenna != null) && (this.currentAntenna.ID != edge.endNode.ID)) {
						this.reset();
						this.checkAntenna();
					} else {
						this.currentAntenna = (Antenna) edge.endNode;
						has = true;
					}
					break;
				}
			}
			if (!has) {
				this.reset();
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
		if (IamAlone()) {
			this.setColor(Color.RED);
		} else {
			if (Omega().ID == this.ID) {
				this.setColor(Color.GREEN);
			} else {
				this.setColor(Color.BLUE);
			}
		}
	}

	private boolean IamAlone() {
		return this.currentAntenna == null;
	}

	private void no_readPrepare(Prepare message) {
		if ((this.getState() == 0) && (Omega().ID == message.sender.ID)) {
			ACK_Prepare ack = new ACK_Prepare(this);
			this.send(ack, message.sender);
			this.setState(2);
		}
	}

	private void co_readPropose(Propose message) {
		if ((this.getState() == 0) && (Omega().ID == message.coord.ID)) {
			Connections no = this.outgoingConnections;
			for (Edge edge : no) {
				if (edge.endNode instanceof MobileNode) {
					Prepare ack = new Prepare(this);
					this.send(ack, edge.endNode);
					this.count_prepare++;
				}
			}
			if (this.count_prepare > 0) {
				this.setState(1);
			} else {
				this.co_sendDecided();
			}
		}
	}

	private void co_readACK_Prepare(ACK_Prepare message) {
		this.count_prepare--;
		if (this.count_prepare == 0) {
			this.co_startAccept();
		}
	}

	private void co_startAccept() {
		if ((this.getState() == 1) && (Omega().ID == this.ID)) {
			Connections no = this.outgoingConnections;
			for (Edge edge : no) {
				if (edge.endNode instanceof MobileNode) {
					Accept ack = new Accept(this);
					this.send(ack, edge.endNode);
					this.count_accept++;
				}
			}
			if (this.count_accept > 0) {
				this.setState(2);
			} else {
				this.co_sendDecided();
			}
		}
	}

	private void no_readAccept(Accept message) {
		if ((this.getState() == 2) && (Omega().ID == message.sender.ID)) {
			ACK_Accept ack = new ACK_Accept(this);
			this.send(ack, message.sender);
			this.setState(0);
		}
	}

	private void co_readACK_Accept(ACK_Accept message) {
		this.count_accept--;
		if (this.count_accept <= (this.outgoingConnections.size() - 1)) {
			this.co_sendDecided();
		}
	}

	private void co_sendDecided() {
		if (this.currentAntenna != null) {
			this.setState(0);
			this.count_accept = 0;
			this.count_prepare = 0;
			Decided decided = new Decided(this);
			this.send(decided, this.currentAntenna);
		} else {
			this.reset();
		}
	}

}
