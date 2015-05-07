package projects.t1.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import projects.t1.nodes.messages.AYC_answer;
import projects.t1.nodes.messages.AYCoord;
import projects.t1.nodes.messages.Accept;
import projects.t1.nodes.messages.Accept_answer;
import projects.t1.nodes.messages.Invitation;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

public class LeaderNode extends Node {

	// conjunto dos membros do proprio grupo
	public ArrayList<Node> upSet;
	// conjunto dos membros da uniao dos grupos
	public ArrayList<Node> up;
	// identificao do grupo (par [CoordID,count])
	public Node coordenatorGroup;
	public Node oldCoordenatorGroup;
	public int coordenatorCount = 0;
	// conjunto de outros coordenadores descobertos
	public ArrayList<Node> others;
	// Momento da ultima mensagem
	private double timeAYCoord;
	private double timerToMerge = 0;
	private int timeOutAYCoord = 5;
	// Tipo do estado: 0 - 'Normal' | 1 - 'Election' | 2 'Reorganizing'
	private int state = 0;
	// contadores de mensagens
	public int waitingAnswerAYCoord = 0;
	public int waitingAnswerInvitation = 0;
	public int waitingAccept_answer = 0;

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
			if (message instanceof Invitation) {
				this.answerInvitation((Invitation) message);
			}
			if (message instanceof Accept) {
				this.answerAccept((Accept) message);
			}
			if (message instanceof Accept_answer) {
				this.processAccept_answer((Accept_answer) message);
			}
		}
	}

	@Override
	public void preStep() {
		if ((Tools.getGlobalTime() % 500) == 0) {
			this.checkMembers();
		}
		this.checkTimeOutAYCoord();
		this.checkTimeOutMerge();
	}

	@Override
	public void init() {
		this.up = new ArrayList<Node>();
		this.upSet = new ArrayList<Node>();
		this.others = new ArrayList<Node>();
		this.coordenatorGroup = this;
		this.setColor(Color.RED);
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		double fraction = Math.max(0.1, ((double) upSet.size()) / Tools.getNodeList().size());
		this.drawingSizeInPixels = (int) (fraction * pt.getZoomFactor() * this.defaultDrawingSizeInPixels);
		this.drawAsDisk(g, pt, highlight, this.drawingSizeInPixels);
		this.drawNodeAsDiskWithText(g, pt, highlight, String.valueOf(this.ID), 20, Color.BLACK);
		if (this.IamCoordenator()) {
			this.setColor(Color.blue);
		} else {
			this.setColor(Color.RED);
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

	private boolean IamCoordenator() {
		if (this.coordenatorGroup == null)
			return false;
		else
			return this.ID == this.coordenatorGroup.ID;
	}

	/*-------------------------------------------------------------------------------------------------*/

	private void checkMembers() {
		if ((this.state == 0) && (this.IamCoordenator())) {
			this.others = new ArrayList<Node>();
			AYCoord ayCoord = new AYCoord(this);
			this.broadcast(ayCoord);
			this.waitingAnswerAYCoord = Tools.getNodeList().size() - 1;
			this.timeAYCoord = Global.currentTime;
		}
	}

	/*-------------------------------------------------------------------------------------------------*/

	private void merge() {
		if ((this.IamCoordenator()) && (this.state == 0)) {
			this.state = 1;
			this.coordenatorCount++;
			this.upSet = this.up;
			this.up = new ArrayList<Node>();
			for (Node no : this.others) {
				this.waitingAnswerInvitation++;
				Invitation message = new Invitation(this, this.coordenatorCount);
				this.send(message, no);
			}
		}
	}

	private void checkTimeOutMerge() {
		if ((this.state == 0) && (this.timerToMerge == Global.currentTime)) {
			System.out.println(" start merge " + this.ID);
			this.merge();
		}
	}

	/*-------------------------------------------------------------------------------------------------*/

	private void checkTimeOutAYCoord() {
		double temp = Global.currentTime - this.timeAYCoord;
		if ((this.state == 0) && (this.waitingAnswerAYCoord > 0) && (temp > this.timeOutAYCoord)) {
			this.waitingAnswerAYCoord = 0;
			this.timeAYCoord = 0;
			System.out.println("time out AYCoord " + this.ID);
		}
	}

	private void answerAYCoord(AYCoord aycoord) {
		System.out.println("AYCoord from " + aycoord.sender.ID);
		AYC_answer ayc_answer = new AYC_answer(this, this.coordenatorGroup);
		this.send(ayc_answer, aycoord.sender);
	}

	private void processAYC_answer(AYC_answer message) {
		System.out.println("AYC_answer from " + message.node.ID);
		if (message.coord.ID != this.ID) {
			this.others.add(message.coord);
		}
		this.waitingAnswerAYCoord--;
		if ((this.waitingAnswerAYCoord == 0) && (this.others.size() > 0)) {
			this.timerToMerge = Global.currentTime + ((Tools.getNodeList().size() * 10) - (this.ID * 10)) + 5;
		}
	}

	/*-------------------------------------------------------------------------------------------------*/

	private void answerInvitation(Invitation message) {
		System.out.println("Invitation from " + this.ID + " by " + message.coord.ID);
		if (this.state == 0) {
			this.oldCoordenatorGroup = this.coordenatorGroup;
			this.upSet = this.up;
			this.state = 1;
			this.coordenatorGroup = message.coord;
			this.coordenatorCount = message.coordenatorCount;
			if (this.oldCoordenatorGroup == this) {
				for (Node no : this.upSet) {
					this.send(message, no);
				}
			}
			Accept accept = new Accept(this, this.coordenatorCount);
			this.send(accept, message.coord);
		}
	}

	/*-------------------------------------------------------------------------------------------------*/
	private void answerAccept(Accept message) {
		System.out.println("Accept by " + message.sender.ID);
		Accept_answer accept_answer;
		if ((this.state == 1) && (this.IamCoordenator()) && (message.coordenatorCount == this.coordenatorCount)) {
			this.up.add(message.sender);
			accept_answer = new Accept_answer(this, true);
		} else {
			accept_answer = new Accept_answer(this, false);
		}
		this.send(accept_answer, message.sender);
		this.waitingAnswerInvitation--;

		if (this.waitingAnswerInvitation == 0) {
			this.state = 0;
		}
	}

	private void processAccept_answer(Accept_answer message) {
		this.state = 0;
		System.out.println("Accept_answer from " + this.ID + " by " + message.sender.ID);
	}

	/*-------------------------------------------------------------------------------------------------*/

}
