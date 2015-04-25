package projects.t1.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import projects.t1.nodes.messages.CheckMembers;
import projects.t1.nodes.messages.T1Message;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class LeaderNode extends Node {
	// conjunto dos membros do próprio grupo
	public ArrayList<Integer> upSet;
	// conjunto dos membros da união dos grupos
	public ArrayList<Integer> up;
	// identificação do grupo (par [CoordID,count])
	public int coordenatorGroupID; 
	public int coordenatorCount;
	// conjunto de outros coordenadores descobertos
	public ArrayList<Integer> others; 
	// Tipo do estado: 0 - 'Normal' | 1 - 'Election' | 2 'Reorganizing'
	public int state = 0; 

	
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
	
	public void check_members(){
		if((this.state == 0)&&(this.IamCoordenator())){
			this.others = new ArrayList<Integer>();
			CheckMembers message = new CheckMembers(this.ID);
			this.broadcast(message);
		}
	}
	
	public boolean IamCoordenator(){
		return this.ID == this.coordenatorGroupID;
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
			this.state = 1;
			this.setColor(Color.RED);	
		} else {
			this.setColor(Color.BLUE);	
		}
	}

	@Override
	public void init() {
		this.up = new ArrayList<Integer>();
		this.upSet = new ArrayList<Integer>();
		this.upSet.add(this.ID);
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
		
	}
	
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		double fraction = Math.max(0.1, ((double) upSet.size()) / Tools.getNodeList().size());
		this.drawingSizeInPixels = (int) (fraction * pt.getZoomFactor() * this.defaultDrawingSizeInPixels);
		this.drawAsDisk(g, pt, highlight, this.drawingSizeInPixels);
		this.drawNodeAsDiskWithText(g, pt, highlight, this.ID+":"+ this.upSet.size()+"/"+Tools.getNodeList().size(), 20, Color.BLACK);
	}


}
