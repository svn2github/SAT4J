package org.sat4j.br4cp;

import br4cp.Configurator;
import br4cp.ConfiguratorFactory;
import br4cp.Protocol;

public class Sat4jConfiguratorFactory implements ConfiguratorFactory {

	private static final Sat4jConfiguratorFactory instance = new Sat4jConfiguratorFactory();
	
	private Sat4jConfiguratorFactory() {
		// prevents instantiation 
	}
	
	public static Sat4jConfiguratorFactory instance()  {
		return instance;
	}
	
	@Override
	public Configurator make(Protocol p) {
		// ready to take into account the protocols in the future
		switch (p) {
		default:
			return new Br4cpConfigurator();
		}
	}

}
