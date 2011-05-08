/**
 * 
 */
package org.owasp.webscarab.dao;

import java.util.Collection;
import org.owasp.webscarab.domain.FuzzingVariable;


/**
 * @author lpz
 *
 */
public interface FuzzingVariableDao {

	FuzzingVariable get(Integer id);
	
	int update(FuzzingVariable annotation);
	
	void delete(Integer id);
	
	Collection<FuzzingVariable> getAll();
	
}
