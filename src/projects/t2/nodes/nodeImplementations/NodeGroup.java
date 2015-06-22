package projects.t2.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import projects.t2.nodes.messages.ACK_Accept;
import projects.t2.nodes.messages.ACK_Prepare;
import projects.t2.nodes.messages.Accept;
import projects.t2.nodes.messages.Decided;
import projects.t2.nodes.messages.Prepare;
import projects.t2.nodes.messages.Propose;
import projects.t2.nodes.messages.T2Message;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class NodeGroup extends NodeT2 {

	int count_prepare = 0;
	int count_accept = 0;
	BufferedImage imgLider = null;
	BufferedImage imgOffline = null;
	BufferedImage imgPolitico = null;

	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message message = inbox.next();
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
			if (message instanceof Decided) {
				logMsg("Decided by " + ((Decided) message).sender.ID);
				this.readDecided((Decided) message);
			}
		}
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public Node Omega() {
		ArrayList<Node> result = new ArrayList<>();
		result = this.dfs(result, this);
		Node max = this;
		for (Node node : result) {
			if (max.ID < node.ID) {
				max = node;
			}
		}
		return max;
	}

	public ArrayList<Node> dfs(ArrayList<Node> visitados, Node raiz) {
		visitados.add(raiz);
		Connections conn = raiz.outgoingConnections;
		for (Edge edge : conn) {
			if (!visitados.contains(edge.endNode)) {
				dfs(visitados, edge.endNode);
			}
			if (!visitados.contains(edge.startNode)) {
				dfs(visitados, edge.startNode);
			}
		}
		return visitados;
	}

	@Override
	public void preStep() {
		this.checkPropose();
	}

	private void checkPropose() {
		if (this.getState() == 0 && Omega().ID == this.ID) {
			Propose propose = new Propose(this, this);
			co_readPropose(propose);
		}
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
		return this.outgoingConnections.size() == 0;
	}

	private boolean IamCoord() {
		if (this.coordenatorGroup != null)
			return this.coordenatorGroup.ID == this.ID;
		else
			return false;
	}

	private void co_readPropose(Propose message) {
		logMsg("Propose by " + ((Propose) message).sender.ID);
		if (this.ID == message.coord.ID &&this.ID == Omega().ID) {
			if (this.getState() != 0) {
				this.reset();
			}
			this.count_prepare = 0;
			Prepare prepare = new Prepare(this, this);
			this.flooding(prepare);
			this.setState(1);
		}
	}

	private void no_readPrepare(Prepare message) {
		this.flooding(message);
		if ((message.coord.ID != this.ID) &&(this.getState() == 0)
				&& (Omega().ID == message.coord.ID)) {
			ACK_Prepare ack = new ACK_Prepare(this, message.coord, this);
			this.flooding(ack);
			this.setState(2);
		}
	}

	private void co_readACK_Prepare(ACK_Prepare message) {
		this.flooding(message);
		if (this.getState() == 1 &&message.coord.ID == this.ID) {
			this.count_prepare++;
			if (this.count_prepare > ((this.getTotalNodes()) / 2) - 1) {
				this.co_startAccept();
			}
		}
	}

	private void co_startAccept() {
		if ((this.getState() == 1) &&(Omega().ID == this.ID)) {
			Accept ack = new Accept(this, this);
			this.flooding(ack);
			this.count_accept = 0;
			this.setState(2);
		}
	}

	private void no_readAccept(Accept message) {
		this.flooding(message);
		if ((message.coord.ID != this.ID)&& (this.getState() == 2)
				&& (Omega().ID == message.coord.ID)) {
			ACK_Accept ack = new ACK_Accept(this, message.coord, this);
			this.flooding(ack);
			this.coordenatorGroup = message.coord;
			this.setState(0);
		}
	}

	private void co_readACK_Accept(ACK_Accept message) {
		this.flooding(message);
		if (this.getState() == 2 &&message.coord.ID == this.ID) {
			this.count_accept++;
			if (this.count_accept > ((this.getTotalNodes() / 2) - 1)) {
				this.co_sendDecided();
			}
		}
	}

	private void co_sendDecided() {
		this.setState(0);
		this.count_accept = 0;
		this.count_prepare = 0;
		this.coordenatorGroup = this;
		log("Consenso realizado no No " + this.ID + " coord "
				+ this.coordenatorGroup.ID);
		consenso++;
		Decided decided = new Decided(this, this);
		this.flooding(decided);
	}

	private void flooding(T2Message message) {
		Connections nos = this.outgoingConnections;
		message.ids.add(this.ID);
		message.sender = this;
		ArrayList<Node> send = new ArrayList<>();
		for (Edge edge : nos) {
			if (!message.ids.contains(edge.endNode.ID)) {
				message.ids.add(edge.endNode.ID);
				send.add(edge.endNode);
			}
			if (!message.ids.contains(edge.startNode.ID)) {
				message.ids.add(edge.startNode.ID);
				send.add(edge.startNode);
			}
		}
		for (Node node : send) {
			this.send(message, node);
		}
	}

}
