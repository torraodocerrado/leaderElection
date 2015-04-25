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
package projects.t1.nodes.nodeImplementations;


import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import projects.t1.nodes.messages.T1Message;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;



public class PresentNode extends Node {
	/* conjunto dos membros do próprio grupo */
	private ArrayList<Integer> upSet;
	
	public void reset(){
		
	}
	
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message message =  inbox.next();			
			if(message instanceof T1Message){
				this.addNodeUpSet((T1Message) message);
			}
		}
	}
	
	
	
	private void addNodeUpSet(T1Message message){
		if((message.tipoMsg == 1) && (!this.upSet.contains(((T1Message)message).idNode))){
			this.upSet.add(((T1Message)message).idNode);
			Tools.repaintGUI();
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
		if((this.upSet.size() < Tools.getNodeList().size())){
			this.broadcast(new T1Message(1, this.ID));
			this.setColor(Color.RED);	
		} else {
			this.setColor(Color.BLUE);	
		}
	}

	@Override
	public void init() {
		this.upSet = new ArrayList<Integer>();
		this.upSet.add(this.ID);
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
	
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		double fraction = Math.max(0.1, ((double) upSet.size()) / Tools.getNodeList().size());
		this.drawingSizeInPixels = (int) (fraction * pt.getZoomFactor() * this.defaultDrawingSizeInPixels);
		this.drawAsDisk(g, pt, highlight, this.drawingSizeInPixels);
		this.drawNodeAsDiskWithText(g, pt, highlight, this.ID+":"+ this.upSet.size()+"/"+Tools.getNodeList().size(), 20, Color.BLACK);
	}


}
