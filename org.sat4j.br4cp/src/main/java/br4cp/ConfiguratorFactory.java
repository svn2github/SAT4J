package br4cp;

/**
 * Factory to retrieve a configurator suitable for a given protocol.
 * 
 * This Factory will typically be implemented using the Singleton design pattern.
 * 
 * @author leberre
 *
 */
public interface ConfiguratorFactory {

	/**
	 * Create a configurator object suitable for a given protocol.
	 * 
	 * @param p a protocol
	 * @return a configurator object suitable for the protocol p.
	 */
	Configurator make(Protocol p);
}
