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
package projects.omega_01.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

//import projects.defaultProject.nodes.timers.MessageTimer;
import projects.omega_01.nodes.messages.OmegaMessage;
import projects.omega_01.nodes.timers.OmegaLoopTimer;
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
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;

import java.util.Random;

import javax.imageio.ImageIO;


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
	private int taxa_de_sucesso = 50;
	
	

	

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// Nothing to do - we could check here, that proper models are set, and other settings are correct
	}

	
	//Logging log = Logging.getLogger("omega_log_nodeid_" + Integer.toString(this.ID) + ".txt");
	
	
	@Override
	public void handleMessages(Inbox inbox) {
		
		// Esse if é para criar assíncronicidade nesse round...
		// Com a taxa de rendimento ele roda ou não nesse round.
		
		// randon x, se x > taxa, roda_nesse_round = false else true
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
				//System.out.println("== msg de " + ""  + "recebida por " + this.ID + " com tipo= " + msg_p_analisar.tipo + " com data= " + msg_p_analisar.data);
				// 1: ok;
				if ((msg_p_analisar.tipo == 1) && (msg_p_analisar.data == this.meu_round))  {
					n_oks_recebidos++;
					if ((this.meu_lider == -1)  && (this.n_oks_recebidos >= 2)) {
						this.meu_lider = msg_p_analisar.data % this.n_nos;	
						n_oks_recebidos = 0;
					}
					//System.out.println("== (OK, k) com k = r ");
				}
				// 2: stop;
				if ((msg_p_analisar.tipo == 2) && (msg_p_analisar.data >= this.meu_round))  {
					// startround(k+1)
					this.iniciaRound(msg_p_analisar.data + 1);
					//System.out.println("== (STOP, k) com k >= r ");					
				}
				// 1: ok; 3: starround;
				if (((msg_p_analisar.tipo == 1) || (msg_p_analisar.tipo == 3))   &&  (msg_p_analisar.data > this.meu_round) ) {
					//startround (k)
					this.iniciaRound(msg_p_analisar.data);					
					//System.out.println("== (OK, k) ou (START, k) com k > r ");					
				}
				// qq ok, zera o timer_2delta = 0;
				if (msg_p_analisar.tipo == 1)  {
					timer_2delta = 0;
				}
			}else {
				System.out.println("============ msg Estranha recebida por " + this.ID + "============");
			}
		}
		
		}// fim do roda_nesse_round	
		
		
		
		// "upon timer > 2*delta"
		if (timer_2delta > (2 * delta +1)) {
			msg_stop = new OmegaMessage();
			msg_stop.tipo = 2; 	       //msg tipo stop
			msg_stop.data = meu_round; //valor da msg
			// detalhe interno do Sinalfo para pegar o ID do destinatário a partir do número dele...
			OmegaNode candidato = (OmegaNode) Tools.getNodeByID(meu_round % n_nos); 
			// cuidado somente por que o sinalgo não pode mandar msg para alguém deletado... e é justamente meu teste...
			if (candidato != null) {
				System.out.println("== STOP para candidato: " + candidato);
				send(msg_stop, candidato);
			}		
			iniciaRound(meu_round + 1);
		}
		// recurso para usar o global time como um timer de 2delta
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
				
		// Como descrito no Omega:
		
		// se não sou o provável lider, envio uma msg de inicio de round para ele
		if (meu_id != ( s % n_nos )) {

			//int teste = ( s % n_nos );
			//System.out.println("Inicia Round::  ID: " + meu_id + " round: " + s + " round % n_nos: " +  teste );
			
			OmegaMessage msg_inicial = new OmegaMessage();
			msg_inicial.tipo = 3; //msg tipo start
			msg_inicial.data = s; //valor da msg
			
			// detalhe interno do Sinalgo para pegar o ID do destinatário a partir do número dele...
			OmegaNode candidato = (OmegaNode) Tools.getNodeByID(s % n_nos); 
			
			// cuidado somente por que o sinalgo não pode mandar msg para alguém deletado... e é justamente meu teste...
			if (candidato != null) {
				System.out.println("== START para candidato: " + candidato);
				send(msg_inicial, candidato);
			}
			this.setColor(Color.DARK_GRAY); // todos de cinza ao entrar no round, menos o indicado;
			
		} else {
			this.setColor(Color.magenta); // só o indicado de magenta
		}
		
		meu_lider = -1;
		meu_round = s;
		n_oks_recebidos = 0;
		timer_2delta = 0;
		
		
	}
	
	
	
	//private boolean simpleDraw = false;
	
	@Override
	public void init() {

		
		jah_inicializou = false; // só na criação
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
		
		// verificar se este método é chamado in assync mode.
		
		// esses são passos de construção que deveriam estar no init(), mas pela construção do sinalgo, estão aqui...
		// explicar depois o porquê.
		if (!jah_inicializou) {
			try { 
				n_nos = Tools.getNodeList().size(); 
			    
				iniciaRound(1);
				
				timer_task_0 = new OmegaLoopTimer(this);
				timer_task_0.startRelative(1, this);
				
				randomGenerator = new Random();
							
				//ImageIO.read("peao_vazado.jpg");
				//ImageIO.read("peao_preto.jpg");
				//ImageIO.read("rei.png");
				 
				taxa_de_sucesso = Configuration.getIntegerParameter("Taxa_de_sucesso");
				
				jah_inicializou = true; // faz com que não executemos nada disso novamente...
			} catch (CorruptConfigurationEntryException e) {
				e.printStackTrace();
			}
		}
	
		
	}

	@Override
	public void postStep() {
		// not called in async mode!
		
		if (ID == meu_lider) { // sou lider, fico verde
			this.setColor(Color.green);	
			this.em_eleicao = false;
		}
		else {
			if (meu_lider != -1) {  // elegi alguém, fico azul
				this.setColor(Color.blue);
				this.em_eleicao = false;
			}
			else { // senão, estou em eleição e fico cinza...
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
