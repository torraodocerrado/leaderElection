package projects.p2.models.distributionModels;


import java.util.Vector;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;
import sinalgo.tools.statistics.Distribution;


public class GridDistribution extends DistributionModel{

	private java.util.Random rand = Distribution.getRandom();
	
	double radius = 0;
	double horizontalFactor = 0;
	double verticalFactor = 0;
	
	private Vector<Position> positions = new Vector<Position>();
	private int returnNum = 0;
	
	public void initialize(){
		try {
			radius = Configuration.getDoubleParameter("AntennaConnection/rMax");
		} catch (CorruptConfigurationEntryException e) {
			e.printStackTrace();
		}
		horizontalFactor = (Configuration.dimX - 2.1*radius)/(radius*2.1);
		verticalFactor = (Configuration.dimY - 2.1*radius)/(radius*2.1);
		
		int ihF = (int)horizontalFactor;
		int ivF = (int)verticalFactor;
		
		int number = 0;
		
		for(int i = 0; i < ihF+1; i++){
			for(int j = 0; j < ivF+1; j++){
				if(number < numberOfNodes){
					positions.add(new Position(radius + i*(radius*2.1), radius + j*(radius*2.1), 0));
				}
			}
		}
	}
	
	
	@Override
	public Position getNextPosition() {
		if(returnNum < positions.size()){
			return positions.elementAt(returnNum++);
		}
		else{
			double randomPosX = rand.nextDouble() * Configuration.dimX;
			double randomPosY = rand.nextDouble() * Configuration.dimY;
			return new Position(randomPosX, randomPosY, 0);
		}
	}
	
	public void setParamString(String s){}

}
