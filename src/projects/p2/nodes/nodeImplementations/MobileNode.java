package projects.p2.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import projects.p2.nodes.messages.ACK_Accept;
import projects.p2.nodes.messages.ACK_Prepare;
import projects.p2.nodes.messages.Accept;
import projects.p2.nodes.messages.Decided;
import projects.p2.nodes.messages.NACK;
import projects.p2.nodes.messages.Prepare;
import projects.p2.nodes.messages.Propose;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Connections;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class MobileNode extends NodeT2 {

	Antenna currentAntenna = null;
	int count_prepare = 0;
	int count_accept = 0;
	BufferedImage imgLider = null;
	BufferedImage imgOffline = null;
	BufferedImage imgPolitico = null;

	public Antenna getCurrentAntenna() {
		return currentAntenna;
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message message = inbox.next();
			if (message instanceof NACK) {
				this._readNACK((NACK) message);
			}
			if (message instanceof Propose) {
				logMsg("Propose by " + ((Propose) message).sender.ID);
				this.co_readPropose((Propose) message);
			}
			if (message instanceof Prepare) {
				logMsg("Prepare by " + ((Prepare) message).coord.ID);
				this.no_readPrepare((Prepare) message);
			}
			if (message instanceof ACK_Prepare) {
				logMsg("ACK_Prepare by " + ((ACK_Prepare) message).no.ID);
				this.co_readACK_Prepare((ACK_Prepare) message);
			}
			if (message instanceof Accept) {
				logMsg("Accept by " + ((Accept) message).coord.ID);
				this.no_readAccept((Accept) message);
			}
			if (message instanceof ACK_Accept) {
				logMsg("ACK_Accept by " + ((ACK_Accept) message).no.ID);
				this.co_readACK_Accept((ACK_Accept) message);
			}
		}
	}

	private void _readNACK(NACK message) {
		if (message.round == this.getRound()) {
			if (message.to.ID == this.ID) {
				this.reset();
			}
		}

	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void preStep() {
		this.checkAntenna();
	}

	public void checkAntenna() {
		if (this.useAntenna) {
			Antenna ant = this.getNewAntenna();
			if (this.currentAntenna != null && ant == null) {
				this.reset();
			} else if (this.currentAntenna == null) {
				this.currentAntenna = ant;
			} else if (this.currentAntenna.ID != ant.ID) {
				this.reset();
			}
		}
	}

	private Antenna getNewAntenna() {
		Connections no = this.outgoingConnections;
		for (Edge edge : no) {
			if (edge.endNode instanceof Antenna) {
				return (Antenna) edge.endNode;
			}
		}
		return null;
	}

	@Override
	public void init() {
		super.init();
		try {
			InputStream in = null;
			in = new FileInputStream("src/" + Configuration.userProjectDir
					+ "/" + Global.projectName + "/" + "images/offline.bmp");
			if ((imgOffline = ImageIO.read(in)) == null) {
				throw new FileNotFoundException(
						"\n 'map.bmp' - This image format is not supported.");
			}
			in.close();
			in = new FileInputStream("src/" + Configuration.userProjectDir
					+ "/" + Global.projectName + "/" + "images/lider.bmp");
			if ((imgLider = ImageIO.read(in)) == null) {
				throw new FileNotFoundException(
						"\n 'map.bmp' - This image format is not supported.");
			}
			in.close();
			in = new FileInputStream("src/" + Configuration.userProjectDir
					+ "/" + Global.projectName + "/" + "images/politico.bmp");
			if ((imgPolitico = ImageIO.read(in)) == null) {
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

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		int imgWidth = 0;
		int imgHeight = 0;
		int[][] grid = null;
		BufferedImage img = null;
		if (IamAlone()) {
			img = this.imgOffline;
			this.setColor(Color.black);
		} else if (IamCoord()) {
			img = this.imgLider;
			this.setColor(Color.red);
		} else {
			img = this.imgPolitico;
			this.setColor(Color.black);
		}
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
		this.drawNodeAsDiskWithText(g, pt, highlight, String.valueOf(this.ID),
				20, Color.white);
	}

	private boolean IamAlone() {
		return this.currentAntenna == null && this.useAntenna == true;
	}

	private boolean IamCoord() {
		if (this.coordenatorGroup != null)
			return this.coordenatorGroup.ID == this.ID;
		else
			return false;
	}

	private void co_readPropose(Propose message) {
		if (this.ID == message.coord.ID && (this.getRound() == message.round)
				&& this.ID == Omega().ID) {
			if (this.getState() != 0) {
				this.reset();
			}
			this.count_prepare = 0;
			Prepare prepare = new Prepare(message.round, this, this);
			this.send(prepare, message.sender);
			this.setState(1);
		} else {

		}
	}

	private void no_readPrepare(Prepare message) {
		if ((message.round > this.lastRound) &&(message.coord.ID != this.ID) && (this.getState() == 0)
					&& (Omega().ID == message.coord.ID)) {
				this.lastRound = message.round;
				ACK_Prepare ack = new ACK_Prepare(message.round, this,
						message.coord, this);
				while (this.currentAntenna == null) {
					this.checkAntenna();
				}
				this.send(ack, this.currentAntenna);
				this.setState(2);
			}
		 else {
			 NACK nack = new NACK(this.lastRound, this, message.coord);
				while (this.currentAntenna == null) {
					this.checkAntenna();
				}
			this.send(nack, this.currentAntenna);
		}
	}

	private void co_readACK_Prepare(ACK_Prepare message) {
		if ((this.getRound() == message.round) && this.getState() == 1
				&& message.coord.ID == this.ID) {
			this.count_prepare++;
			if (this.count_prepare > ((this.getTotalNodes()) / 2) - 1) {
				this.co_startAccept(message.round);
			}
		}
	}

	private void co_startAccept(int round) {
		if ((this.getRound() == round) && (this.getState() == 1)
				&& (Omega().ID == this.ID)) {
			Accept ack = new Accept(round, this, this);
			this.send(ack, this.currentAntenna);
			this.count_accept = 0;
			this.setState(2);
		}
	}

	private void no_readAccept(Accept message) {
		if ((message.coord.ID != this.ID) && (this.getState() == 2)
				&& (this.getRound() == message.round)
				&& (Omega().ID == message.coord.ID)) {
			ACK_Accept ack = new ACK_Accept(message.round, this, message.coord,
					this);
			this.send(ack, this.currentAntenna);
			this.coordenatorGroup = message.coord;
			this.setState(0);
		}
	}

	private void co_readACK_Accept(ACK_Accept message) {
		if (this.getState() == 2 && (this.getRound() == message.round)
				&& message.coord.ID == this.ID) {
			this.count_accept++;
			if (this.count_accept > ((this.getTotalNodes() / 2) - 1)) {
				this.co_sendDecided();
			}
		}
	}

	private void co_sendDecided() {
		if (this.currentAntenna != null) {
			this.setState(0);
			this.count_accept = 0;
			this.count_prepare = 0;
			this.coordenatorGroup = this;
			Decided decided = new Decided(round, this, this);
			this.send(decided, this.currentAntenna);
		} else {
			this.reset();
		}
	}

}
