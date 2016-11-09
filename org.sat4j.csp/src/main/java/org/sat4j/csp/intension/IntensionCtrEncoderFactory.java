package org.sat4j.csp.intension;

public class IntensionCtrEncoderFactory {
	
	private static IntensionCtrEncoderFactory instance = null;
	
	public static IntensionCtrEncoderFactory getInstance() {
		if(instance == null) {
			instance = new IntensionCtrEncoderFactory();
		}
		return instance;
	}
	
	private IntensionCtrEncoderFactory() {
		// Singleton DP
	}
	
	public IIntensionCtrEncoder newDefault(ICspToSatEncoder encoder) {
		return newTseitinBased(encoder);
	}
	
	public IIntensionCtrEncoder newTseitinBased(ICspToSatEncoder encoder) {
		return new TseitinBasedIntensionCtrEncoder(encoder);
	}

	public IIntensionCtrEncoder newNogoodBased(ICspToSatEncoder encoder) {
		return new NogoodBasedIntensionCtrEncoder(encoder);
	}

}
