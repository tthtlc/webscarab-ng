/**
 * 
 */
package org.owasp.webscarab.dao;

import java.util.Collection;

import org.owasp.webscarab.domain.CookieConfiguration;

/**
 * @author lpz
 *
 */
public interface LinksDao {

	CookieConfiguration get(Integer id);
	
	void update(CookieConfiguration annotation);
	
	void delete(Integer id);
	
	Collection<CookieConfiguration> getAll();
	
}
