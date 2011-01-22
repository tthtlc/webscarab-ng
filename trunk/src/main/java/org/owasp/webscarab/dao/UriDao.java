/**
 * 
 */
package org.owasp.webscarab.dao;

import java.net.URI;
import java.util.Collection;

/**
 * @author rdawes
 *
 */
public interface UriDao {

    Integer findUriId(URI uri);
    
    URI findUri(Integer id);
    
    Integer saveUri(URI uri);

    Collection<URI> getAll();
}
