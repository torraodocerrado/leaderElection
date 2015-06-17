package projects.t2.nodes.nodeImplementations;

import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.runtime.Global;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;

public abstract class NodeT2 extends Node {
	private boolean log_on = true;
	private boolean logmsg_on = false;
	public Node coordenatorGroup;
	protected double currentTimeOut = 0;
	protected int timeOut = 500;
	/* state of node 0 = Normal 1 = Prepare 2 = Accept */
	private int state;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
		if (this.state == 0) {
			this.currentTimeOut = 0;
		} else {
			this.currentTimeOut = Global.currentTime + this.timeOut;
		}
	}

	@Override
	public void init() {
		try {
			this.timeOut = Configuration
					.getIntegerParameter("MessageTransmission/timeOut");
		} catch (CorruptConfigurationEntryException e) {
			e.printStackTrace();
		}
	}

	public MobileNode Omega() {
		Connections nos = this.outgoingConnections;
		MobileNode result = null;
		if (this instanceof MobileNode) {
			result = (MobileNode) this;
		}
		for (Edge edge : nos) {
			if (((result == null) && (edge.endNode instanceof MobileNode))
					|| ((edge.endNode instanceof MobileNode) && (result.ID < edge.endNode.ID))) {
				result = (MobileNode) edge.endNode;
			}
		}
		return result;
	}

	protected void log(Object message) {
		if (this.log_on) {
			if (this instanceof Antenna) {
				System.out.println("STEP "
						+ ((int) (Math.round(Global.currentTime)))
						+ "- Antenna " + this.ID + " received: "
						+ message.toString());
			} else {
				if (IamCoordenator())
					System.out.println("STEP "
							+ ((int) (Math.round(Global.currentTime)))
							+ "- Coord " + this.ID + " received: "
							+ message.toString());
				else
					System.out.println("STEP "
							+ ((int) (Math.round(Global.currentTime)))
							+ "- Node " + this.ID + " received: "
							+ message.toString());
			}
		}
	}
	protected void logMsg(Object message) {
		if (this.logmsg_on) {
			if (this instanceof Antenna) {
				System.out.println("STEP "
						+ ((int) (Math.round(Global.currentTime)))
						+ "- Antenna " + this.ID + " received: "
						+ message.toString());
			} else {
				if (IamCoordenator())
					System.out.println("STEP "
							+ ((int) (Math.round(Global.currentTime)))
							+ "- Coord " + this.ID + " received: "
							+ message.toString());
				else
					System.out.println("STEP "
							+ ((int) (Math.round(Global.currentTime)))
							+ "- Node " + this.ID + " received: "
							+ message.toString());
			}
		}
	}

	private boolean IamCoordenator() {
		if (this.coordenatorGroup == null) {
			this.coordenatorGroup = this;
			return true;
		}
		return this.ID == this.coordenatorGroup.ID;
	}

	protected void checkTimeOut() {
		if (this.currentTimeOut == Global.currentTime) {
			this.reset();
			log("TIME-OUT in state " + this.state);
		}
	}

	protected void reset() {
		log("RESET state " + this.state);
		this.state = 0;
		this.outgoingConnections.removeAndFreeAllEdges();
		this.coordenatorGroup = null;
		if (this instanceof MobileNode) {
			((MobileNode) this).currentAntenna = null;
			((MobileNode) this).count_accept = 0;
			((MobileNode) this).count_prepare = 0;
		}
	}

}
