package org.cartaro.geoserver.security.drupal.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.logging.Logger;

import org.cartaro.geoserver.security.drupal.DrupalRoleService;
import org.geoserver.catalog.Catalog;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.impl.DataAccessRuleDAO;
import org.geotools.util.logging.Logging;

public class DrupalDataAccessRuleDAO extends DataAccessRuleDAO implements LastModificationTriggerable {
	protected static Logger LOGGER = Logging
			.getLogger(DrupalDataAccessRuleDAO.class);
	private final Catalog rawCatalog;

	/**
	 * Should be the time of last modification of permission related data in
	 * Drupal. Currently, this is just a time set in intervals because the
	 * former is unknown.
	 */
	private long lastModified;

	protected DrupalDataAccessRuleDAO(GeoServerDataDirectory dd,
			Catalog rawCatalog) throws IOException {
		super(dd, rawCatalog);
		LOGGER.info("Injected: DrupalDataAccessRuleDAO");
		this.rawCatalog = rawCatalog;

		// Change modification date to force update of permissions every 5s.
		Timer modificationTrigger = new Timer();
		modificationTrigger.scheduleAtFixedRate(new LastModificationTimerTask(this), 000, 2000);
	}

	@Override
	public List<DataAccessRule> getRules() {
		LOGGER.info("Injected: getRules");
		ArrayList<DataAccessRule> rules = new ArrayList<DataAccessRule>();

		// Insert rules from layers.properties
		rules.addAll(super.getRules());

		// Insert rules from Drupal instances
		GeoServerSecurityManager manager = GeoServerExtensions
				.bean(GeoServerSecurityManager.class);
		GeoServerRoleService activeRoleService = manager.getActiveRoleService();
		LOGGER.info("Injected: active role service:" + activeRoleService);
		if (activeRoleService instanceof DrupalRoleService) {
			DrupalRoleService roleService = (DrupalRoleService) activeRoleService;
			try {
				LOGGER.info("Injected: loading layer rules");
				rules.addAll(roleService.getLayerAccessRules(rawCatalog));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return rules;
	}

	/**
	 * Use current time to force reloading of permissions.
	 */
	@Override
	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long newLastModified) {
		lastModified = newLastModified;
		
	}
}
