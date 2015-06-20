package projects.t2.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import projects.t2.nodes.messages.Decided;
import projects.t2.nodes.messages.Propose;
import projects.t2.nodes.messages.T2Message;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Connections;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

public class Antenna extends NodeT2 {
	BufferedImage img = null;

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
			this.flooding(message);
		}
	}

	@Override
	public void init() {
		super.init();
		try {
			InputStream in = null;
			in = new FileInputStream("src/" + Configuration.userProjectDir
					+ "/" + Global.projectName + "/" + "images/templo.bmp");
			if ((img = ImageIO.read(in)) == null) {
				throw new FileNotFoundException(
						"\n 'map.bmp' - This image format is not supported.");
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void flooding(Message message) {
		if (((T2Message) message).sender instanceof MobileNode) {
			this.sendAllConected(message);
		} else {
			this.sendAllMobileNodeConected(message);
		}
	}

	private void sendAllMobileNodeConected(Message message) {
		Connections nodes = this.outgoingConnections;
		((T2Message) message).sender = this;
		for (Edge edge : nodes) {
			if (edge.endNode instanceof MobileNode) {
				this.send(message, edge.endNode);
			}
		}
	}

	private void sendAllConected(Message message) {
		((T2Message) message).sender = this;
		this.broadcast(message);
	}

	private void readDecided(Decided message) {
		if ((this.getState() == 2) && (Omega().ID == message.coord.ID)) {
			this.setState(0);
			this.coordenatorGroup = message.coord;
			log("Consenso realizado na antena " + this.ID + " coord "
					+ this.coordenatorGroup.ID);
		}
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void preStep() {
		if (this.getState() == 0) {
			propose();
		}
	}

	private void propose() {
		MobileNode leader = Omega();
		if (leader != null) {
			if (this.outgoingConnections.contains(this, leader)) {
				Propose propose = new Propose(this, leader);
				this.send(propose, leader);
				this.setState(2);
			}
		}
	}

	@Override
	public void postStep() {
		this.checkTimeOut();
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
		g.setColor(Color.GRAY);
		pt.translateToGUIPosition(this.getPosition());
		int r = (int) (radius * pt.getZoomFactor());
		g.drawOval(pt.guiX - r, pt.guiY - r, r * 2, r * 2);
		g.setColor(bckup);

		int imgWidth = 0;
		int imgHeight = 0;
		int[][] grid = null;
		imgWidth = img.getWidth();
		imgHeight = img.getHeight();
		grid = new int[imgWidth][imgHeight];
		// copy the image data
		for (int i = 0; i < imgWidth; i++) {
			for (int j = 0; j < imgHeight; j++) {
				grid[i][j] = img.getRGB(i, j);
			}
		}

		int iniX = (int) this.getPosition().xCoord - (imgWidth / 2);
		int iniY = (int) this.getPosition().yCoord - (imgHeight / 2);

		for (int i = iniX; i < imgWidth + iniX; i++) {
			for (int j = iniY; j < imgHeight + iniY; j++) {
				pt.translateToGUIPosition(i, j, 0); // top left corner of cell
				int topLeftX = pt.guiX, topLeftY = pt.guiY;
				pt.translateToGUIPosition((i + 1), (j + 1), 0); // bottom right
																// corner of
																// cell
				Color col = new Color(grid[i - iniX][j - iniY]);
				g.setColor(col);
				g.fillRect(topLeftX, topLeftY, pt.guiX - topLeftX, pt.guiY
						- topLeftY);
			}
		}

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
