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
package projects.omega_01.nodes.timers;

import projects.omega_01.nodes.messages.OmegaMessage;
import projects.omega_01.nodes.nodeImplementations.OmegaNode;

public class OmegaLoopTimer extends sinalgo.nodes.timers.Timer {
	private OmegaMessage msg; // the msg to send
	private OmegaNode meu_node; // the node to send the msg to
		
	public OmegaLoopTimer(OmegaNode origem) {
		this.msg = new OmegaMessage();
		this.meu_node = origem;
		msg.tipo = 1; //msg tipo ok
		msg.data = meu_node.meu_round; //valor da msg
			
	}
	
	@Override
	public void fire() {
		
		if (meu_node != null) { // esse if de fora é para os casos de quando se remove o nó e o timer acaba acordando e o sinalgo da erro.
			if (meu_node.ID == (meu_node.meu_round % meu_node.n_nos)) {				
				    msg.data = meu_node.meu_round;
					this.meu_node.broadcast(this.msg);
					System.out.println("== envio de OK por " + this.meu_node.ID);
					
					// isto esta aqui somente por que o sinalgo não envia msg de um nó para ele mesmo.
					// logo, me candidato e aceito...
					this.meu_node.meu_lider = this.meu_node.ID;
			}			
		this.startRelative(1, this.meu_node);
		}
	}

}
