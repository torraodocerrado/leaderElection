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
package projects.omega_00.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

//import projects.defaultProject.nodes.timers.MessageTimer;
//import sinalgo.configuration.Configuration;
//import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
//import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.gui.transformation.PositionTransformation;
//import sinalgo.io.eps.EPSOutputPrintStream;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

import java.util.Random;

import projects.omega_00.nodes.messages.OmegaMessage;
import projects.omega_00.nodes.timers.OmegaLoopTimer;


public class OmegaNode extends Node {
	
	// variaveis do problema
	public int n_nos;
	public int meu_round =-1;
	public int meu_lider = -1 ;
	public int n_oks_recebidos = 0;
	public int meu_id = this.ID; 
	public OmegaLoopTimer timer_task_0;
	public boolean jah_inicializou;
	public int timer_2delta = 0;
	public int delta = 3;
	public boolean em_eleicao = true;
	
	private OmegaMessage msg_stop;
	public boolean roda_nesse_round = true;
	private Random randomGenerator;
	private int num_randomico;
	private int taxa_de_sucesso = 90;
	
	

	

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// Nothing to do - we could check here, that proper models are set, and other settings are correct
	}

	
	@Override
	public void handleMessages(Inbox inbox) {
		

		num_randomico = randomGenerator.nextInt(100);
		if (taxa_de_sucesso > num_randomico) {
			roda_nesse_round = true;
		}else{
			roda_nesse_round = false;
		}
		
		
		if (roda_nesse_round) {
		
		while(inbox.hasNext()) {
			Message msg_recebida = inbox.next();
			if(msg_recebida instanceof OmegaMessage) {
				OmegaMessage msg_p_analisar = (OmegaMessage) msg_recebida;
			
				if ((msg_p_analisar.tipo == 1) && (msg_p_analisar.data == this.meu_round))  {
					n_oks_recebidos++;
					if ((this.meu_lider == -1)  && (this.n_oks_recebidos >= 2)) {
						this.meu_lider = msg_p_analisar.data % this.n_nos;	
						n_oks_recebidos = 0;
					}
					
				}
				
				if ((msg_p_analisar.tipo == 2) && (msg_p_analisar.data >= this.meu_round))  {
					
					this.iniciaRound(msg_p_analisar.data + 1);
										
				}
				
				if (((msg_p_analisar.tipo == 1) || (msg_p_analisar.tipo == 3))   &&  (msg_p_analisar.data > this.meu_round) ) {
					//startround (k)
					this.iniciaRound(msg_p_analisar.data);					
										
				}
				
				if (msg_p_analisar.tipo == 1)  {
					timer_2delta = 0;
				}
			}else {
				System.out.println("============ msg Estranha recebida por " + this.ID + "============");
			}
		}
		
		}
		

		if (timer_2delta > (2 * delta +1)) {
			msg_stop = new OmegaMessage();
			msg_stop.tipo = 2; 	       
			msg_stop.data = meu_round; 
			
			OmegaNode candidato = (OmegaNode) Tools.getNodeByID(meu_round % n_nos); 
			
			if (candidato != null) {
				System.out.println("== STOP para candidato: " + candidato);
				send(msg_stop, candidato);
			}		
			iniciaRound(meu_round + 1);
		}
		
		if (this.ID != meu_lider) {
			timer_2delta++;
		} else {
			timer_2delta = 0;
		}
				
		
	}

	/*
	@NodePopupMethod(menuText="Inicia Round ZERO")
	public void iniciaRoudZero() {
		iniciaRound(0);
	} */
	
	@NodePopupMethod(menuText="Inicia Round 1")
	public void iniciaRoudUm() {
		
		iniciaRound(1);
				
	}
	
	private void iniciaRound(int s) {
				

		if (meu_id != ( s % n_nos )) {


			
			OmegaMessage msg_inicial = new OmegaMessage();
			msg_inicial.tipo = 3; 
			msg_inicial.data = s; 
			
			OmegaNode candidato = (OmegaNode) Tools.getNodeByID(s % n_nos); 
			
			if (candidato != null) {
				System.out.println("== START para candidato: " + candidato);
				send(msg_inicial, candidato);
			}
			this.setColor(Color.DARK_GRAY); 
			
		} else {
			this.setColor(Color.magenta); 
		}
		
		meu_lider = -1;
		meu_round = s;
		n_oks_recebidos = 0;
		timer_2delta = 0;
		
		
	}
	
	
	
	@Override
	public void init() {

		
		jah_inicializou = false; 
		this.setColor(Color.RED);
		
		/*
		if(Configuration.hasParameter("S4Node/simpleDraw")) {
			try {
				simpleDraw = Configuration.getBooleanParameter("S4Node/simpleDraw");
			} catch (CorruptConfigurationEntryException e) {
				Tools.fatalError("Invalid config field S4Node/simpleDraw: Expected a boolean.\n" + e.getMessage());
			}
		} else {
			simpleDraw = false;
		}
		*/
		// nothing to do here
	}

	/*
	@Override
	public void neighborhoodChange() {
		// not called in async mode!
	}*/

	@Override
	public void preStep() {
		
		
		if (!jah_inicializou) {
			n_nos = Tools.getNodeList().size(); 
		    
			iniciaRound(1);
			
			timer_task_0 = new OmegaLoopTimer(this);
			timer_task_0.startRelative(1, this);
			
			randomGenerator = new Random();
						
			jah_inicializou = true; 
		}
	
		
	}

	@Override
	public void postStep() {
		
		
		if (ID == meu_lider) { 
			this.setColor(Color.green);	
			this.em_eleicao = false;
		}
		else {
			if (meu_lider != -1) {  
				this.setColor(Color.blue);
				this.em_eleicao = false;
			}
			else { 
				this.setColor(Color.DARK_GRAY);	
				this.em_eleicao = true;
			}
		}
	
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		
		super.drawNodeAsDiskWithText(g, pt, highlight, Integer.toString(this.ID) + " | " + Integer.toString(this.meu_lider) + " | " + Integer.toString(this.meu_round) , 16, Color.WHITE);

	}
	
	
	
	
	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub
		
	}

	
	/*	
	
	private boolean drawRound = false;

	private boolean isDrawRound() {
		if(drawRound) {
			return true;
		}
		if(getColor().equals(Color.YELLOW)) {
			return true;
		}
		return false;
	}
	

	
	@NodePopupMethod(menuText="Draw as Circle")
	public void drawRound() {
		drawRound = !drawRound;
		Tools.repaintGUI();
	}
	
	
	
	public void drawToPostScript(EPSOutputPrintStream pw, PositionTransformation pt) {
		if(isDrawRound()) {
			super.drawToPostScriptAsDisk(pw, pt, drawingSizeInPixels/2, getColor());
		} else {
			super.drawToPostscriptAsSquare(pw, pt, drawingSizeInPixels, getColor());
		}
	}
	
	

	
	/*
	
	
	@NodePopupMethod(menuText="Multicast RED")
	public void multicastRED() {
		sendColorMessage(Color.RED, null);
	}
	
	@NodePopupMethod(menuText="Multicast BLUE")
	public void multicastBLUE() {
		sendColorMessage(Color.BLUE, null);
	}

	@NodePopupMethod(menuText="BROADCAST GREEN")
	public void broadcastGREEN() {
		sendColorMessage(Color.GREEN, null);
	}
	
	@NodePopupMethod(menuText="BROADCAST YELLOW")
	public void broadcastYELLOW() {
		sendColorMessage(Color.YELLOW, null);
	}

	/**
	 * Sends a message to (a neighbor | all neighbors) with the specified color as message content.
	 * @param c The color to write in the message.
	 * @param to Receiver node, or null, if all neighbors should receive the message.

	private void sendColorMessage(Color c, Node to) {
		S4Message msg = new S4Message();
		msg.color = c;
		if(Tools.isSimulationInAsynchroneMode()) {
			// sending the messages directly is OK in async mode
			if(to != null) {
				send(msg, to);
			} else {
				broadcast(msg);
			}
		} else {
			// In Synchronous mode, a node is only allowed to send messages during the 
			// execution of its step. We can easily schedule to send this message during the
			// next step by setting a timer. The MessageTimer from the default project already
			// implements the desired functionality.
			MessageTimer t;
			if(to != null) {
				t = new MessageTimer(msg, to); // unicast
			} else {
				t = new MessageTimer(msg); // multicast
			}
			t.startRelative(Tools.getRandomNumberGenerator().nextDouble(), this);
		}
	}
	
	@NodePopupMethod(menuText="Unicast Gray")
	public void unicastGRAY() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			public void handleNodeSelectedEvent(Node n) {
				if(n == null) {
					return; // the user aborted
				}
				sendColorMessage(Color.GRAY, n);
			}
		}, "Select a node to which you want to send a 'yellow' message.");
	}

	
	/**
	 * This popup method demonstrates how a message can be sent
	 * even when there is no edge between the sender and receiver  
	@NodePopupMethod(menuText="send DIRECT PINK")
	public void sendDirectPink() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			public void handleNodeSelectedEvent(Node n) {
				if(n == null) {
					return; // the user aborted
				}
				S4Message msg = new S4Message();
				msg.color = Color.pink;
				if(Tools.isSimulationInAsynchroneMode()) {
					sendDirect(msg, n);
				} else {
					// we need to set a timer, such that the message is
					// sent during the next round, when this node performs its step.
					S4SendDirectTimer timer = new S4SendDirectTimer(msg, n);
					timer.startRelative(1.0, S4Node.this);
				}
			}
		}, "Select a node to which you want to send a direct 'PINK' message.");
	}
*/
	
	
	
	
	
	
}
