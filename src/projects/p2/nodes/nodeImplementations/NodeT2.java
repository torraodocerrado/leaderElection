package projects.p2.nodes.nodeImplementations;

import java.text.SimpleDateFormat;
import java.util.Date;

import projects.p2.LogFile;
import projects.p2.nodes.messages.Decided;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.runtime.Global;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface;
import sinalgo.tools.Tools;

public abstract class NodeT2 extends Node {
	private boolean log_on = true;
	private boolean logmsg_on = false;
	public NodeT2 coordenatorGroup;
	protected double currentTimeOut = 0;
	protected int timeOut;
	/* state of node 0 = Normal 1 = Prepare 2 = Accept */
	private int state;
	public static int round = 0;
	public int lastRound = 0;

	protected boolean useAntenna;
	public static LogFile fileLog;
	public static int consenso = 0;
	public static int msg = 0;

	public int getState() {
		return state;
	}

	public int nextRound() {
		return ++round;
	}
	
	public int getRound() {
		return round;
	}

	public void setState(int state) {
		this.state = state;
		if (this.state == 0) {
			this.currentTimeOut = 0;
		} else {
			this.currentTimeOut = Global.currentTime + this.timeOut;
		}
	}

	public int getTotalNodes() {
		int i = 0;
		for (Node no : Tools.getNodeList()) {
			if (!(no instanceof Antenna)) {
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
		if (fileLog == null) {
			fileLog = new LogFile(this.getNameFile());
			fileLog.add("step;qtdNosInstaveis;qtdRoundsNosInstaveis");
			fileLog.ln();
		}
	}

	@Override
	public void postStep() {
		this.checkTimeOut();
		if ((Global.currentTime % 5000 == 0)) {
			fileLog.add(((int) (Math.round(Global.currentTime))) + ";"
					+ consenso + ";" + Global.numberOfMessagesOverAll + ";");
			fileLog.ln();
		}
		if (Global.currentTime > 10010) {
			System.exit(0);
		}
	}

	private String getNameFile() {
		String prefixo = "0_";
		SimpleDateFormat ft = new SimpleDateFormat(
				"dd-MM-yyyy-'at'-hh-mm-ss-SSS-a");
		Date today = new Date();
		return prefixo + ft.format(today) + "_report.csv";
	}

	public Node Omega() {
		NodeCollectionInterface nos = Tools.getNodeList();
		Node result = null;
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

	protected void readDecided(Decided message) {
		if ((this.getState() == 2) && (Omega().ID == message.coord.ID)) {
			this.setState(0);
			this.coordenatorGroup = message.coord;
			log("Consenso realizado na antena " + this.ID + " coord "
					+ this.coordenatorGroup.ID);
			consenso++;
		}
	}

}
