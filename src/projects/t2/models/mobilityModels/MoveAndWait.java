package projects.t2.models.mobilityModels;

import projects.defaultProject.models.mobilityModels.RandomWayPoint;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.runtime.Global;

public class MoveAndWait extends RandomWayPoint {
	private static boolean pause = false;
	static double wait = 1;
	int timeToMove = 0;
	int timeToPause = 0;

	/**
	 * The one and only constructor.
	 * 
	 * @throws CorruptConfigurationEntryException
	 *             When a needed configuration entry is missing.
	 */
	public MoveAndWait() throws CorruptConfigurationEntryException {
		super();
		timeToMove = Configuration.getIntegerParameter("MoveAndWait/timeToMove");
		timeToPause = Configuration.getIntegerParameter("MoveAndWait/timeToPause");

	}

	public Position getNextPos(Node n) {
		if (Global.currentTime % wait == 0) {
		if (isPause()) {
			MoveAndWait.setPause(false);
			wait = Global.currentTime + timeToMove;
		} else {
			MoveAndWait.setPause(true);
				wait = Global.currentTime + timeToPause;
			}
		}
		if (!MoveAndWait.isPause()) {
			Position newPos = super.getNextPos(n);
			return newPos;
		} else {
			return n.getPosition();
		}
	}

	public static boolean isPause() {
		return pause;
	}

	public static void setPause(boolean pause) {
		MoveAndWait.pause = pause;
	}
}
