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
package org.jamwiki.utils;

import java.io.File;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.DiskStoreConfiguration;
import org.jamwiki.Environment;

/**
 * Implement utility functions that interact with the cache and provide the
 * infrastructure for storing and retrieving items from the cache.
 */
public class WikiCache<K, V> {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiCache.class.getName());
	private static CacheManager CACHE_MANAGER = null;
	private static boolean INITIALIZED = false;
	// track whether this instance was instantiated from an ehcache.xml file or using configured properties.
	private static final String EHCACHE_XML_CONFIG_FILENAME = "ehcache-jamwiki.xml";
	/** Directory for cache files. */
	private static final String CACHE_DIR = "cache";
	private final String cacheName;

	/**
	 * Initialize a new cache with the given name.
	 *
	 * @param cacheName The name of the cache being created.  This name should not
	 *  be re-used, otherwise unexpected results could be returned.
	 */
	public WikiCache(String cacheName) {
		this.cacheName = cacheName;
	}

	/**
	 * Add an object to the cache.
	 *
	 * @param key A String, Integer, or other object to use as the key for
	 *  storing and retrieving this object from the cache.
	 * @param value The object that is being stored in the cache.
	 */
	public void addToCache(K key, V value) {
		this.getCache().put(new Element(key, value));
	}

	/**
	 * Internal method used to retrieve the Cache object created for this
	 * instance's cache name.  If no cache exists with the given name then
	 * a new cache will be created.
	 *
	 * @return The existing cache object, or a new cache if no existing cache
	 *  exists.
	 * @throws IllegalStateException if an attempt is made to retrieve a cache
	 *  using XML configuration and the cache is not configured.
	 */
	private Cache getCache() throws CacheException {
		if (!WikiCache.INITIALIZED) {
			WikiCache.initialize();
		}
		if (!WikiCache.CACHE_MANAGER.cacheExists(this.cacheName)) {
			// all caches should be configured from ehcache.xml
			throw new IllegalStateException("No cache named " + this.cacheName + " is configured in the ehcache.xml file");
		}
		return WikiCache.CACHE_MANAGER.getCache(this.cacheName);
	}

	/**
	 * Return the name of the cache that this instance was configured with.
	 */
	public String getCacheName() {
		return this.cacheName;
	}

	/**
	 * Initialize the cache, clearing any existing cache instances and loading
	 * a new cache instance.
	 */
	public static void initialize() {
		try {
			File file = ResourceUtil.getClassLoaderFile(EHCACHE_XML_CONFIG_FILENAME);
			logger.info("Initializing cache configuration from " + file.getAbsolutePath());
			Configuration configuration = ConfigurationFactory.parseConfiguration(file);
			if (WikiCache.CACHE_MANAGER != null) {
				WikiCache.CACHE_MANAGER.removalAll();
				WikiCache.CACHE_MANAGER.shutdown();
				WikiCache.CACHE_MANAGER = null;
			}
			File directory = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), CACHE_DIR);
			if (!directory.exists()) {
				directory.mkdir();
			}
			DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
			diskStoreConfiguration.setPath(directory.getPath());
			configuration.addDiskStore(diskStoreConfiguration);
			WikiCache.CACHE_MANAGER = new CacheManager(configuration);
		} catch (Exception e) {
			logger.error("Failure while initializing cache", e);
			throw new RuntimeException(e);
		}
		logger.info("Initializing cache with disk store: " + WikiCache.CACHE_MANAGER.getDiskStorePath());
		WikiCache.INITIALIZED = true;
	}

	/**
	 * Return <code>true</code> if the key is in the specified cache, even
	 * if the value associated with that key is <code>null</code>.
	 */
	public boolean isKeyInCache(K key) {
		return this.getCache().isKeyInCache(key);
	}

	/**
	 * Close the cache manager.
	 */
	public static void shutdown() {
		WikiCache.INITIALIZED = false;
		if (WikiCache.CACHE_MANAGER != null) {
			WikiCache.CACHE_MANAGER.shutdown();
			WikiCache.CACHE_MANAGER = null;
		}
	}

	/**
	 * Remove all values from the cache.
	 */
	public void removeAllFromCache() {
		this.getCache().removeAll();
	}

	/**
	 * Remove a value from the cache with the given key.
	 *
	 * @param key The key for the record that is being removed from the cache.
	 */
	public void removeFromCache(K key) {
		this.getCache().remove(key);
	}

	/**
	 * Remove a key from the cache in a case-insensitive manner.  This method
	 * is significantly slower than removeFromCache and should only be used when
	 * the key values may not be exactly known.
	 */
	public void removeFromCacheCaseInsensitive(String key) {
		for (Object cacheKey : this.getCache().getKeys()) {
			// with the upgrade to ehcache 2.4.2 it seems that null cache keys are possible...
			if (cacheKey != null && cacheKey.toString().equalsIgnoreCase(key)) {
				this.getCache().remove(cacheKey);
			}
		}
	}

	/**
	 * Retrieve an object from the cache.  IMPORTANT: this method will return
	 * <code>null</code> if no matching element is cached OR if the cached
	 * object has a value of <code>null</code>.  Callers should call
	 * {@link #isKeyInCache} if a <code>null</code> value is returned to
	 * determine whether a <code>null</code> was cached or if the value does
	 * not exist in the cache.
	 *
	 * @param key The key for the record that is being retrieved from the
	 *  cache.
	 * @return The cached object if one is found, <code>null</code> otherwise.
	 */
	public V retrieveFromCache(K key) {
		Element element = this.getCache().get(key);
		return (element != null) ? (V)element.getObjectValue() : null;
	}
}
