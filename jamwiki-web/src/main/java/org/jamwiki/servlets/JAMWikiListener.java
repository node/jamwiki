/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.servlets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jamwiki.db.WikiDatabase;
import org.jamwiki.utils.WikiCache;
import org.jamwiki.utils.WikiUtil;

/**
 * A ServletContextListener that will do necessary initialization
 * and cleanup when the Servlet Context is initialized and destroyed. 
 */
public class JAMWikiListener implements ServletContextListener {

	/**
	 * Initialize the database connection pool and disk cache.
	 *
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0) {
		if (!WikiUtil.isFirstUse()) {
			WikiDatabase.initialize();
			WikiCache.initialize();
		}
	}

	/**
	 * Clean up the database connection pool and disk cache.
	 *
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		WikiDatabase.shutdown();
		WikiCache.shutdown();
	}
}
