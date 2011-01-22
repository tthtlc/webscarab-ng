/**
 * 
 */
package org.owasp.webscarab.dao;

import java.util.Collection;

import org.owasp.webscarab.domain.HeaderConfiguration;

/**
 * @author lpz
 *
 */
public interface HeaderConfigurationDao {

	HeaderConfiguration get(Integer id);
	
	void update(HeaderConfiguration annotation);
	
	void delete(Integer id);
	
	Collection<HeaderConfiguration> getAll();
	
}
