/**
 * 
 */
package org.owasp.webscarab.dao;

import java.util.Collection;

import org.owasp.webscarab.domain.FormValueConfiguration;

/**
 * @author lpz
 *
 */
public interface FormValueConfigurationDao {

	FormValueConfiguration get(Integer id);
	
	void update(FormValueConfiguration annotation);
	
	void delete(Integer id);
	
	Collection<FormValueConfiguration> getAll();
	
}
