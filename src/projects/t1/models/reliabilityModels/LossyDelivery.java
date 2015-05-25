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
package projects.t1.models.reliabilityModels;

import java.util.Random;

import sinalgo.models.ReliabilityModel;
import sinalgo.nodes.messages.Packet;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

/**
 * A loossy reliability model that drops messages with a constant probability.
 * <p>
 * The percentage of dropped messages has to be specified in the configuration
 * file:
 * <p>
 * &lt;LossyDelivery dropRate="..."/&gt;
 */
public class LossyDelivery extends ReliabilityModel {
	private Random randomGenerator;
	private int coinChancePositive = 98;
	private double timeDisablePart1 = 0;
	private double timeDisablePart2 = 0;
	private double timeDisablePart3 = 0;
	private double timeOffLine = 100;

	public LossyDelivery() {
		this.randomGenerator = new Random();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sinalgo.models.ReliabilityModel#reachesDestination(sinalgo.nodes.messages
	 * .Packet)
	 */
	@Override
	public boolean reachesDestination(Packet p) {
		if (isSameGroup(p)) {
			return true;
		}

		// GROUP 1
		if ((this.timeDisablePart1 < Global.currentTime) && (this.flipTheCoin())) {
			this.timeDisablePart1 = Global.currentTime + this.timeOffLine;
			System.out.println("Desabilitou grupo 1 até o round " + this.timeDisablePart1);
		}

		// GROUP 2
		if ((this.timeDisablePart2 < Global.currentTime) && (this.flipTheCoin())) {
			this.timeDisablePart2 = Global.currentTime + this.timeOffLine;
			System.out.println("Desabilitou grupo 2 até o round " + this.timeDisablePart2);
		}

		// GROUP 3
		if ((this.timeDisablePart3 < Global.currentTime) && (this.flipTheCoin())) {
			this.timeDisablePart3 = Global.currentTime + this.timeOffLine;
			System.out.println("Desabilitou grupo 3 até o round " + this.timeDisablePart3);
		}

		int part = this.getGroup(p.destination.ID);
		switch (part) {
		case 1:
			System.out.println("ID " + p.destination.ID + " " + this.timeDisablePart1 + " " + Global.currentTime);
			return this.timeDisablePart1 < Global.currentTime;
		case 2:
			System.out.println("ID " + p.destination.ID + " " + this.timeDisablePart2 + " " + Global.currentTime);
			return this.timeDisablePart2 < Global.currentTime;
		case 3:
			System.out.println("ID " + p.destination.ID + " " + this.timeDisablePart3 + " " + Global.currentTime);
			return this.timeDisablePart3 < Global.currentTime;
		default:
			return true;
		}

	}

	public boolean isSameGroup(Packet p) {
		return getGroup(p.origin.ID) == getGroup(p.destination.ID);
	}

	private int getGroup(int idNode) {
		int sizeGroup = (Tools.getNodeList().size() / 3);
		if ((idNode <= sizeGroup)) {
			return 1;
		}
		if ((idNode > sizeGroup) && (idNode <= (sizeGroup * 2))) {
			return 2;
		}
		return 3;
	}

	private boolean flipTheCoin() {
		int num_randomico = randomGenerator.nextInt(100);
		if (coinChancePositive < num_randomico) {
			return true;
		} else {
			return false;
		}
	}

}
