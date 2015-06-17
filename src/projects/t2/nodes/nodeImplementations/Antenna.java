package projects.t2.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.t2.nodes.messages.Decided;
import projects.t2.nodes.messages.Propose;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class Antenna extends NodeT2 {

	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message message = inbox.next();
			if (message instanceof Decided) {
				logMsg("Decided by " + ((Decided) message).sender.ID);
				this.readDecided((Decided) message);
			}
		}
	}

	private void readDecided(Decided message) {
		if ((this.getState() == 2) && (Omega().ID == message.sender.ID)) {
			this.setState(0);
			log("Consenso realizado na antena "+this.ID);
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void preStep() {
		if (this.getState() == 0) {
			propose();
		}
		this.checkTimeOut();
	}

	private void propose() {
		MobileNode leader = Omega();
		if (leader != null) {
			Propose propose = new Propose(this, leader);
			this.send(propose, leader);
			this.setState(2);
		}
	}

	@Override
	public void postStep() {
	}

	private static int radius;
	{
		try {
			radius = Configuration
					.getIntegerParameter("GeometricNodeCollection/rMax");
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}

	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		Color bckup = g.getColor();
		g.setColor(Color.BLACK);
		this.drawingSizeInPixels = (int) (defaultDrawingSizeInPixels * pt
				.getZoomFactor());
		super.drawAsDisk(g, pt, highlight, drawingSizeInPixels);
		g.setColor(Color.black);
		pt.translateToGUIPosition(this.getPosition());
		int r = (int) (radius * pt.getZoomFactor());
		g.drawOval(pt.guiX - r, pt.guiY - r, r * 2, r * 2);
		g.setColor(bckup);

		this.drawNodeAsDiskWithText(g, pt, highlight, String.valueOf(this.ID),
				20, Color.BLACK);
		this.setColor(Color.yellow);
	}

	public Antenna() {
		try {
			this.defaultDrawingSizeInPixels = Configuration
					.getIntegerParameter("Antenna/Size");
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}

}
