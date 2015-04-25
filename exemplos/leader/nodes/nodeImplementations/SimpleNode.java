/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package projects.leader.nodes.nodeImplementations;

import java.awt.Color;
import java.util.ArrayList;

import projects.leader.nodes.messages.NetworkMessage;
import projects.leader.nodes.timers.NetworkMessageTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.runtime.Runtime;
import sinalgo.tools.Tools;

/**
 * The Node of the sample project.
 */
public class SimpleNode extends Node {

	private final int ELECTION = 0;
	private final int STOP = 1;
	private final int PING = 2;
	private final int PONG = 3;
	private final int ANNOUNCE_LEADER = 4;

	private Node networkLeader = null;
	private boolean runningLeaderElection = false;
	private boolean waitingAnswer = false;
	private double timeOfLastMsgSent = 0; 

	/**
	 * Seta o presente nó como o líder da rede
	 * */
	public void setAsNetworkLeader() {
		this.networkLeader = this;
		this.setColor(Color.RED);
		this.proclaimLeadership();
	}

	/**
	 * Proclama o presente nó como leader da rede
	 * */
	private void proclaimLeadership() {
		this.broadcast(new NetworkMessage(this.ANNOUNCE_LEADER));
		this.runningLeaderElection = false;
		this.waitingAnswer = false;
	}

	/**
	 * Armazena a informação de quem é o líder da rede
	 * */
	private void setNetworkLeader(Node leader) {
		this.networkLeader = leader;
	}

	/**
	 * Inicia eleição para definir o líder da rede
	 * */
	private void startLeaderElection() {
		this.runningLeaderElection = true;
		this.waitingAnswer = true;
		this.fireLeaderElectionMsg();
	}
	
	public ArrayList<SimpleNode> getHigherIDNeighborhoods() {
		ArrayList<SimpleNode> neighborhoods = new ArrayList<SimpleNode>();

		for (Node n : Runtime.nodes) {
			if (n.ID > this.ID) {
				neighborhoods.add((SimpleNode) n);
			}
		}

		return neighborhoods;
	}

	private void fireLeaderElectionMsg() {
		NetworkMessageTimer timer = new NetworkMessageTimer(new NetworkMessage(ELECTION));
		timer.startRelative(1, this);
		
		this.timeOfLastMsgSent = Global.currentTime;
		Tools.appendToOutput("Node " + this.ID + " has just started leader election." + "\n\n");
	}


	/**
	 * TODO verificar como checar retorno
	 * */
	@NodePopupMethod(menuText = "Ping Leader")
	public void pingLeader() {
		if(this.networkLeader == null)
			this.startLeaderElection();
		else
			this.sendPingMsgToLeader();
	}
	
	private void sendPingMsgToLeader(){
		NetworkMessageTimer timer = new NetworkMessageTimer(new NetworkMessage(this.PING));
		timer.startRelative(1, this);
		this.timeOfLastMsgSent = Global.currentTime;
		this.waitingAnswer = true;
	}
	
	public Node getNetworkLeader(){
		return this.networkLeader;
	}

	/**
	 * This method is invoked after all the Messages are received. Overwrite it
	 * to specify what to do with incoming messages.
	 * 
	 * @param inbox
	 *            a instance of a iterator-like class Inbox. It is used to
	 *            traverse the incoming packets and to get information about
	 *            them.
	 * @see Node#step() for the order of calling the methods.
	 */
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message message = inbox.next();			
			
			if (message instanceof NetworkMessage) {
				Node sender = inbox.getSender();
				
				switch(((NetworkMessage) message).tipoMsg){
					case 0: // ELECTION
						Tools.appendToOutput(sender.ID +
								" ~> " +
								this.ID +
								": LEADER ELECTION" +
								"\n\n");
						this.send(new NetworkMessage(this.STOP), sender);
						if(!this.runningLeaderElection){
							this.startLeaderElection();
							this.runningLeaderElection = true;
							this.waitingAnswer = true;
							this.timeOfLastMsgSent = Global.currentTime;
						}
						
						break;
					case 1: // STOP
						this.runningLeaderElection = false;
						this.waitingAnswer = false;
						this.timeOfLastMsgSent = 0;
						Tools.appendToOutput(sender.ID +
								" ~> " +
								this.ID +
								": STOP" +
								"\n\n");
						break;
					case 2: // PING
						if(this.ID == this.networkLeader.ID)
							this.send(new NetworkMessage(this.PONG), sender);
							Tools.appendToOutput(sender.ID +
									" ~> " +
									this.ID +
									": PING" +
									"\n\n");
						break;
					case 3: // PONG
						this.waitingAnswer = false;
						Tools.appendToOutput(sender.ID +
								" ~> " +
								this.ID +
								": PONG" +
								"\n\n");
						break;
					case 4:
						Tools.appendToOutput(sender.ID +
								" ~> " +
								this.ID +
								": SET LEADER" +
								"\n\n");
						this.setNetworkLeader(sender);
				}
			}
		}
	}

	/**
	 * This method is invoked at the beginning of each step. Add actions to this
	 * method that this node should perform in every step.
	 * 
	 * @see Node#step() for the calling sequence of the node methods.
	 */
	@Override
	public void preStep() {
		if(this.waitingAnswer){
			// Imagine a leader election starting at time 1
			// time 2 - leader election message is sent to higher ID nodes
			// time 3 - higher ID nodes answer with STOP message
			// time 4 - original sender receives message
			if(this.runningLeaderElection){
				if( (Global.currentTime - this.timeOfLastMsgSent) > 3)
					this.setAsNetworkLeader();
			}
			
			// Imagine a PING message being sent at time 1
			// time 2 - ping message is sent
			// time 3 - leader receives message and Answer
			// time 4 - original sender receives message
			else if((Global.currentTime - this.timeOfLastMsgSent) > 3){
				this.startLeaderElection();
			}
		}
	}

	/**
	 * This method is called exactly once upon creation of this node and allows
	 * the subclasses to perform some node-specific initialization.
	 * <p>
	 * When a set of nodes is generated, this method may be called before all
	 * nodes are added to the framework. Therefore, this method should not
	 * depend on other nodes of the framework.
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	/**
	 * At the beginning of each round, the framework moves all nodes according
	 * to their mobility model. Then, it iterates over all nodes to update the
	 * connections, according to the nodes connectivity model.
	 * <p>
	 * This method is called in the step of this node if the set of outgoing
	 * connections had changed in this round. I.e. a new edge was added or an
	 * edge was removed.
	 * <p>
	 * As a result, this method is called nearly always in the very first round,
	 * when the network graph is determined for the first time.
	 */
	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub

	}

	/**
	 * The node calls this method at the end of its step.
	 */
	@Override
	public void postStep() {
		// TODO Auto-generated method stub

	}

	/**
	 * This method checks if the configuration meets the specification of the
	 * node. This function is called exactly once just after the initialisazion
	 * of a node but before the first usage.
	 * 
	 * @throws WrongConfigurationException
	 *             if the requirements are not met.
	 */
	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}
}
