package projects.t1.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import projects.t1.nodes.messages.AYC_answer;
import projects.t1.nodes.messages.AYCoord;
import projects.t1.nodes.messages.Invitation;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

public class LeaderNode extends Node {
	// conjunto dos membros do próprio grupo
	public ArrayList<Node> upSet;
	// conjunto dos membros da união dos grupos
	public ArrayList<Node> up;
	// identificação do grupo (par [CoordID,count])
	public Node coordenatorGroup;
	public int coordenatorCount;
	// conjunto de outros coordenadores descobertos
	public ArrayList<Node> others;
	// Tipo do estado: 0 - 'Normal' | 1 - 'Election' | 2 'Reorganizing'
	public int state = 0;
	// aguardando resposta
	public boolean waitingAnswer = false;
	// Momento da última mensagem
	private double timeOfLastMessage;
	private double timeT1;
	private double timeT2;

	public void reset() {

	}

	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message message = inbox.next();
			if (message instanceof AYCoord) {
				this.answerAYCoord((AYCoord) message);
			}
			if (message instanceof AYC_answer) {
				this.processAYC_answer((AYC_answer) message);
			}
		}
	}

	private void processAYC_answer(AYC_answer message) {
		if ((this.IamCoordenator()) && (message.coord.ID == this.ID)
				&& (!this.others.contains(message.node))) {
			this.others.add(message.node);
		}
	}

	private void answerAYCoord(AYCoord aycoord) {
		AYC_answer ayc_answer = new AYC_answer(this, this.coordenatorGroup);
		this.send(ayc_answer, aycoord.sender);
	}

	public void checkMembers() {
		if ((this.state == 0) && (this.IamCoordenator())) {
			this.others = new ArrayList<Node>();
			AYCoord ayCoord = new AYCoord(this);
			this.broadcast(ayCoord);
			this.waitingAnswer = true;
			this.timeOfLastMessage = Global.currentTime;
		}
	}

	public boolean IamCoordenator() {
		return this.ID == this.coordenatorGroup.ID;
	}

	@Override
	public void preStep() {
		if ((Tools.getGlobalTime() % 500) == 0) {
			this.checkMembers();
		}
		if (this.timeOutLastMessage()) {
			this.merge();
		}
		if (this.timeOutT1()) {
			this.reorganize();
		}
	}

	private void reorganize() {
		if (this.IamCoordenator() && (this.state == 0)) {
			this.state = 2;
			int numAnswers = 0;
			this.timeT2 = Global.currentTime;
			Ready inviation = new Ready(this, this.getGroup());
			for (Node no : this.others) {
				this.send(inviation, no);
			}
		}

	}

	private void merge() {
		if (this.IamCoordenator() && (this.state == 0)) {
			this.state = 1;
			this.coordenatorCount++;
			this.upSet.addAll(this.up);
			this.up = new ArrayList<Node>();
			this.timeT1 = Global.currentTime;
			Invitation inviation = new Invitation(this, this.getGroup());
			for (Node no : this.others) {
				this.send(inviation, no);
			}
			for (Node no : this.upSet) {
				this.send(inviation, no);
			}
		}
	}

	private String getGroup() {
		return this.coordenatorCount + "|" + this.coordenatorCount;
	}

	private boolean timeOutLastMessage() {
		return this.waitingAnswer
				&& ((Global.currentTime - this.timeOfLastMessage) > 3);
	}

	private boolean timeOutT1() {
		return ((Global.currentTime - this.timeT1) > 3);
	}

	private boolean timeOutT2() {
		return ((Global.currentTime - this.timeT2) > 3);
	}

	@Override
	public void init() {
		this.up = new ArrayList<Node>();
		this.upSet = new ArrayList<Node>();
		this.others = new ArrayList<Node>();

		if ((this.ID % 2) == 0) {
			this.coordenatorGroup = Tools.getNodeByID(Tools.getNodeList()
					.size());
			this.coordenatorCount = 0;
			this.setColor(Color.YELLOW);
		} else {
			this.coordenatorGroup = this;
			this.coordenatorCount = 0;
			this.setColor(Color.RED);
		}
		if (this.IamCoordenator()) {
			this.setColor(Color.blue);
		}
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void postStep() {
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		double fraction = Math.max(0.1, ((double) upSet.size())
				/ Tools.getNodeList().size());
		this.drawingSizeInPixels = (int) (fraction * pt.getZoomFactor() * this.defaultDrawingSizeInPixels);
		this.drawAsDisk(g, pt, highlight, this.drawingSizeInPixels);
		if (this.IamCoordenator()) {
			this.drawNodeAsDiskWithText(g, pt, highlight, this.ID + ":"
					+ this.others.size(), 20, Color.BLACK);
		} else {
			this.drawNodeAsDiskWithText(g, pt, highlight,
					String.valueOf(this.ID), 20, Color.BLACK);
		}

	}

}
