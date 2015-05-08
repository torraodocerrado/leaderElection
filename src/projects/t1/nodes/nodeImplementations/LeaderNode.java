package projects.t1.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import projects.t1.nodes.messages.AYC_answer;
import projects.t1.nodes.messages.AYCoord;
import projects.t1.nodes.messages.AYThere;
import projects.t1.nodes.messages.AYThere_answer;
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
	private double timeOut = 100;
	private double timeOutAYThere = 0;
	// Tipo do estado: 0 - 'Normal' | 1 - 'Election' | 2 'Reorganizing'
	private int state = 0;
	// contadores de mensagens
	private int waitingAnswerAYCoord = 0;
	// AnswerInvitation
	private double timeOutAnswerInvitation = 0;
	private int waitingAnswerInvitation = 0;

	private Random randomGenerator;
	private boolean log_on = true;
	private int coinChancePositive = 100;

	@Override
	public void handleMessages(Inbox inbox) {
		if (this.flipTheCoin()) {
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
				if (message instanceof AYThere) {
					this.answerAYThere((AYThere) message);
				}
				if (message instanceof AYThere_answer) {
					this.processAYThere_answer((AYThere_answer) message);
				}
			}
		} else {
			System.out.println("perdeu mensagem na moeda ID: " + this.ID);
		}
	}

	@Override
	public void preStep() {
		this.checkMembers();
		this.checkCoord();
		this.checkTimerToMerge();
		this.timeOutAYCoord();
		this.timeOutAYThere();
		this.timeOutAnswerInvitation();
	}

	@Override
	public void init() {
		this.up = new ArrayList<Node>();
		this.upSet = new ArrayList<Node>();
		this.others = new ArrayList<Node>();
		this.coordenatorGroup = this;
		this.setColor(Color.RED);
		this.randomGenerator = new Random();
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
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
			this.timeOutAnswerInvitation = Global.currentTime;
		}
	}

	private void checkTimerToMerge() {
		if ((this.state == 0) && (this.timerToMerge == Global.currentTime)) {
			log("Start merge " + this.ID);
			this.merge();
		}
	}

	/*-------------------------------------------------------------------------------------------------*/

	private void checkMembers() {
		if (Global.currentTime % 100 == 0) {
			if ((this.waitingAnswerAYCoord == 0) && (this.state == 0) && (this.IamCoordenator())) {
				this.others = new ArrayList<Node>();
				AYCoord ayCoord = new AYCoord(this);
				this.broadcast(ayCoord);
				this.waitingAnswerAYCoord = this.outgoingConnections.size();
				this.timeAYCoord = Global.currentTime;
			}
		}
	}

	private void timeOutAYCoord() {
		double temp = Global.currentTime - this.timeAYCoord;
		if ((this.state == 0) && (this.waitingAnswerAYCoord > 0) && (temp > this.timeOut)) {
			this.waitingAnswerAYCoord = 0;
			this.timeAYCoord = 0;
			log("time out AYCoord " + this.ID);
		}
	}

	private void timeOutAnswerInvitation() {
		double temp = Global.currentTime - this.timeOutAnswerInvitation;
		if ((this.waitingAnswerInvitation > 0) && (temp > this.timeOut)) {
			this.state = 0;
			this.waitingAnswerInvitation = 0;
			this.timeOutAnswerInvitation = 0;
			log("Time out timeOutAnswerInvitation " + this.ID);
		}
	}

	private void answerAYCoord(AYCoord aycoord) {
		log("AYCoord from " + aycoord.sender.ID);
		AYC_answer ayc_answer = new AYC_answer(this, this.coordenatorGroup);
		this.send(ayc_answer, aycoord.sender);
	}

	private void processAYC_answer(AYC_answer message) {
		log("AYC_answer from " + this.ID + " by " + message.node.ID);
		if (message.coord.ID != this.ID) {
			this.others.add(message.coord);
		}
		this.waitingAnswerAYCoord--;
		if ((this.waitingAnswerAYCoord == 0) && (this.others.size() > 0)) {
			this.timerToMerge = Global.currentTime + (Tools.getNodeList().size() * 10 + (10 - this.ID * 10));
			log("ID: " + this.ID + " timer: " + this.timerToMerge + " actualy " + Global.currentTime);
		}
	}

	/*-------------------------------------------------------------------------------------------------*/

	private void answerInvitation(Invitation message) {
		if (this.state == 0) {
			log("Invitation from " + this.ID + " by " + message.coord.ID);
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
		log("Accept from " + message.sender.ID + " by " + this.ID);
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
		this.others.clear();
		this.timerToMerge = 0;
		log("Accept_answer from " + this.ID + " by " + message.sender.ID);
	}

	/*-------------------------------------------------------------------------------------------------*/

	private void checkCoord() {
		if (Global.currentTime % 100 == 0) {
			if ((this.timeOutAYThere == 0) && (this.state == 0) && (!this.IamCoordenator())) {
				AYThere aythere = new AYThere(this, this.coordenatorCount);
				this.send(aythere, this.coordenatorGroup);
				this.timeOutAYThere = Global.currentTime;
			}
		}
	}

	private void timeOutAYThere() {
		if (this.timeOutAYThere > 0) {
			double temp = Global.currentTime - this.timeOutAYThere;
			if ((this.state == 0) && (temp > this.timeOut)) {
				this.timeOutAYThere = 0;
				log("Time out timeOutAYThere " + this.ID);
				this.recovery();
			}
		}

	}

	private void answerAYThere(AYThere message) {
		AYThere_answer ayt_answer;
		if (this.IamCoordenator()) {
			if ((message.coordenatorCount == this.coordenatorCount) && (this.up.contains(message.sender))) {
				ayt_answer = new AYThere_answer(this, true);
			} else {
				ayt_answer = new AYThere_answer(this, false);
			}
			this.send(ayt_answer, message.sender);
		}
	}

	private void recovery() {
		this.state = 1;
		this.coordenatorCount++;
		this.coordenatorGroup = this;
		this.up = new ArrayList<Node>();
		this.state = 0;
	}

	private void processAYThere_answer(AYThere_answer message) {
		this.timeOutAYThere = 0;
		if (!message.answer) {
			recovery();
		}

	}

	/*-------------------------------------------------------------------------------------------------*/

	private boolean flipTheCoin() {
		int num_randomico = randomGenerator.nextInt(100);
		if (coinChancePositive > num_randomico) {
			return true;
		} else {
			return false;
		}
	}

	private void log(String message) {
		if (this.log_on) {
			System.out.println(message);
		}
	}
}
