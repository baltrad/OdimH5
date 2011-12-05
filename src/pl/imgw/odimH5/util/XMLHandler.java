/**
 * (C) 2011 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */

package pl.imgw.odimH5.util;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;

/**
 *
 * XMLHandler - handles a singleton of a factory for xml parsing
 * 
 *
 *
 *
 * @author <a href="mailto:tomasz.sznajderski@imgw.pl">Tomasz Sznajderski</a>
 *
 */

public class XMLHandler {

	private static XMLInputFactory factory = null;

	/**
	 * Get a singleton of a factory for xml file parsing.
	 *
	 * 
	 * 
	 * 
	 * 
	 * @return a factory singleton
	 */
	public static XMLInputFactory getFactoryInstance() throws FactoryConfigurationError {

		if (factory == null)
			factory = XMLInputFactory.newInstance();
		return factory;
	}
}