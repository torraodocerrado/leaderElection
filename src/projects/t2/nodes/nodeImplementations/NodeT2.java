package projects.t2.nodes.nodeImplementations;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.runtime.Global;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface;
import sinalgo.tools.Tools;

public abstract class NodeT2 extends Node {
	private boolean log_on = true;
	private boolean logmsg_on = true;
	public MobileNode coordenatorGroup;
	protected double currentTimeOut = 0;
	protected int timeOut;
	/* state of node 0 = Normal 1 = Prepare 2 = Accept */
	private int state;
	protected boolean useAntenna;

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
	
	public int getTotalNodes(){
		int i = 0;
		for (Node no : Tools.getNodeList()) {
			if(no instanceof MobileNode){
				i++;
			}
		}
		return i;
	}

	@Override
	public void init() {
		try {
			this.timeOut = Configuration
					.getIntegerParameter("MessageTransmission/timeOut");
			this.useAntenna = !Configuration.useMap;
		} catch (CorruptConfigurationEntryException e) {
			e.printStackTrace();
		}
	}

	public MobileNode Omega() {
		NodeCollectionInterface nos = Tools.getNodeList();
		MobileNode result = null;
		if (this instanceof MobileNode) {
			result = (MobileNode) this;
		}
		for (Node edge : nos) {
			if (((result == null) && (edge instanceof MobileNode))
					|| ((edge instanceof MobileNode) && (result.ID < edge.ID))) {
				if (((MobileNode) edge).currentAntenna != null) {
					result = (MobileNode) edge;
				}
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
			return false;
		}
		return this.ID == this.coordenatorGroup.ID;
	}

	protected void checkTimeOut() {
		if (this.currentTimeOut == Global.currentTime) {
			log("TIME-OUT in state " + this.state);
			this.reset();
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
