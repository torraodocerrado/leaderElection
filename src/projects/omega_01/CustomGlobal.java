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
package projects.omega_01;

import java.awt.Color;
import java.util.Enumeration;


import javax.swing.JOptionPane;

import projects.sample1.nodes.nodeImplementations.S1Node;

import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import projects.omega_01.nodes.nodeImplementations.OmegaNode;

/**
 * This class holds customized global state and methods for the framework. 
 * The only mandatory method to overwrite is 
 * <code>hasTerminated</code>
 * <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * @see sinalgo.runtime.AbstractCustomGlobal for more details.
 * <br>
 * In addition, this class also provides the possibility to extend the framework with
 * custom methods that can be called either through the menu or via a button that is
 * added to the GUI. 
 */
public class CustomGlobal extends AbstractCustomGlobal{
	
	
	Logging log = Logging.getLogger("omega_log.txt");
	
	
	
	/* (non-Javadoc)
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	public boolean hasTerminated() {
		return false;
	}

	
	public void postRound() {
		
		int nodes_em_eleicao = 0;
		int nodes_que_rodaram = 0;
		String estado_global = ""; 
		int estado_global_int;
		
		
		
		// conta quantos estão em eleição
		Enumeration<?> nodeEnumer = Tools.getNodeList().getNodeEnumeration();
		while(nodeEnumer.hasMoreElements()){
			OmegaNode node = (OmegaNode)nodeEnumer.nextElement();
			if(node.em_eleicao){
				nodes_em_eleicao++;
			}
		}
		
		// conta quantos estão em eleição
		Enumeration<?> nodeEnumer2 = Tools.getNodeList().getNodeEnumeration();
		while(nodeEnumer2.hasMoreElements()){
			OmegaNode node = (OmegaNode)nodeEnumer2.nextElement();
			if(node.roda_nesse_round){
				nodes_que_rodaram++;
			}
		}		
		
		
		if (nodes_em_eleicao == 0)  {
			estado_global = "Estável";
			estado_global_int = Tools.getNodeList().size(); 
		} else {
			estado_global = "Em eleição";
			estado_global_int = 0;
		}
		
		
		double dt = System.currentTimeMillis() - Global.startTimeOfRound.getTime();

		//log.logln("Round: " + (int)(Global.currentTime) +  dt + ";Msgs no Round: " + Global.numberOfMessagesInThisRound+ ";Nodes em eleição: " + nodes_em_eleicao);

		// round;  msg no round; caras em eleição; quantos rodaram; estado global inteiro ; estado global string
		log.logln((int)(Global.currentTime) + dt + ";" + Global.numberOfMessagesInThisRound+ ";" + nodes_em_eleicao + ";" + nodes_que_rodaram + ";" + estado_global_int + ";" + estado_global);
		
		
	}
	
	
	
	
	/**
	 * An example of a method that will be available through the menu of the GUI.
	 */
	@AbstractCustomGlobal.GlobalMethod(menuText="Echo")
	public void echo() {
		// Query the user for an input
		String answer = JOptionPane.showInputDialog(null, "This is an example.\nType in any text to echo.");
		// Show an information message 
		JOptionPane.showMessageDialog(null, "You typed '" + answer + "'", "Example Echo", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * An example to add a button to the user interface. In this sample, the button is labeled
	 * with a text 'GO'. Alternatively, you can specify an icon that is shown on the button. See
	 * AbstractCustomGlobal.CustomButton for more details.   
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="Clear", toolTipText="Reset the color of all nodes")
	public void sampleButton() {
		for(Node n : Tools.getNodeList()) {
			n.setColor(Color.BLACK);
		}
		Tools.repaintGUI(); // to have the changes visible immediately
	}
	
	/**
	 * Color all nodes red. 
	 */
	@AbstractCustomGlobal.CustomButton(imageName="red.gif", toolTipText="Set all nodes to red.")
	public void redButton() {
		for(Node n : Tools.getNodeList()) {
			n.setColor(Color.RED);
		}
		Tools.repaintGUI(); // to have the changes visible immediately
	}
	
	/**
	 * Color all nodes blue. 
	 */
	@AbstractCustomGlobal.CustomButton(imageName="blue.gif", toolTipText="Set all nodes to blue.")
	public void blueButton() {
		for(Node n : Tools.getNodeList()) {
			n.setColor(Color.BLUE);
		}
		Tools.repaintGUI(); // to have the changes visible immediately
	}
}
