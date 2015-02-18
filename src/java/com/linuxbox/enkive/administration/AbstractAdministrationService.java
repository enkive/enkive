/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.linuxbox.enkive.administration;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.linuxbox.enkive.ProductInfo;
import com.linuxbox.util.EphemeralVersion;
import com.linuxbox.util.Version;
import com.linuxbox.util.Version.VersionException;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;


/**
 * Abstract base class for @ref AdministrationService implementations.  This
 * implements the update check.

 * @author dang
 *
 */
public abstract class AbstractAdministrationService implements AdministrationService {
	protected static final String UPDATE_URL = "https://www.enkive.org/version.php";
	private static final long MS_TO_DAYS = 1000 * 60 * 60 * 24;
	protected final static Log LOGGER = LogFactory.getLog("com.linuxbox.enkive.administration");

	private Timer timer;
	private long updateInterval;
	private VersionCheckUpdater updater = null;

	protected boolean checkForUpdates = true;

	public AbstractAdministrationService() {
		this.timer = new Timer();
	}

	public void setCheckForUpdates(boolean check) {
		checkForUpdates = check;
	}

	public boolean getCheckForUpdates() {
		return checkForUpdates;
	}

	public void setUpdateInterval(long interval) {
		updateInterval = interval;
	}

	public long getUpdateInterval() {
		return updateInterval;
	}

	@Override
	public Version updateCheck() throws VersionException {
		EphemeralVersion newVersion;
		
		if (!checkForUpdates) {
			// We were configured to not check
			if (this.updater != null) {
				this.updater.cancel();
			}
			return null;
		}

		newVersion = getCurrentVersion();
		
		if (newVersion.equals(ProductInfo.VERSION)) {
			return null;
		} else if (newVersion.compareTo(ProductInfo.VERSION) < 0) {
			// Should not be possible
			throw new VersionException("Latest version " + newVersion +
					" is older than this version " + ProductInfo.VERSION);
		}
		
		if (this.updater == null) {
			// Set the timer to check for updates again
			// schedule() takes microseconds, configuration is in days.
			this.updater = new VersionCheckUpdater(this);
			timer.schedule(this.updater, this.updateInterval * MS_TO_DAYS,
					this.updateInterval * MS_TO_DAYS);
		}
		// There's a newer version
		return newVersion;
	}
	
	private EphemeralVersion getCurrentVersion() throws VersionException {
	      URL url;
	      HttpURLConnection conn;
	      JSONObject json;
	      String versionURL = UPDATE_URL;
	      String uid;
	      String versionOrdinal;
	      String versionString;
	      String hash;
	      
	      uid = getUID();
	      versionOrdinal = "" + ProductInfo.VERSION.versionOrdinal;
	      versionString = ProductInfo.VERSION.toString();

	      try {
		  hash = new String((new Hex()).encode(
				  MessageDigest.getInstance("SHA-1").digest((uid +
						  versionOrdinal + versionString).getBytes())));
	      } catch (NoSuchAlgorithmException e1) {
		  return null;
	      }


	      try {
		      versionURL += "?uid=" + URLEncoder.encode(uid, "UTF-8");
		      versionURL += "&versionOrdinal=" + URLEncoder.encode(versionOrdinal, "UTF-8");
		      versionURL += "&versionString=" + URLEncoder.encode(versionString, "UTF-8");
		      versionURL += "&hash=" + URLEncoder.encode(hash, "UTF-8");
	      } catch (UnsupportedEncodingException e) {
		      throw new VersionException("Failed to get current version: " + e);
	      }
	      try {
	         url = new URL(versionURL);
	         conn = (HttpURLConnection) url.openConnection();
	         conn.setRequestMethod("GET");
	         Scanner scanner = new Scanner(url.openStream());
	         String response = scanner.useDelimiter("\\Z").next();
	         json = (JSONObject) JSONValue.parse(response);
	         scanner.close();
	      } catch (Exception e) {
	         throw new VersionException("Failed to get current version: " + e);
	      }
	      if (json == null) {
		      throw new VersionException("Failed to get current version: JSON was null");
	      }
	      
	      int verOrd;
	      String verString;
	      verOrd = Integer.parseInt((String)json.get("versionOrdinal"));
	      verString = (String)json.get("versionString");
	      
	      EphemeralVersion version = new EphemeralVersion(verString, verOrd);
	      
	      saveVersionCheck(version);
	      
	      return version;
	}
	
	/**
	 * Spring startup.
	 */
	public void startup() {
	}

	/**
	 * Spring shutdown
	 */
	public void shutdown() {
		timer.cancel();
	}

	/**
	 * Save this version check results into the backing store
	 * @param newVersion	Newly found version
	 */
	abstract protected void saveVersionCheck(Version newVersion);

	private class VersionCheckUpdater extends TimerTask {

		private AdministrationService adminService;

		VersionCheckUpdater(AdministrationService adminService) {
			this.adminService = adminService;
		}
		@Override
		public void run() {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Checking version");
			}
			Version newVersion;
			try {
				newVersion = adminService.updateCheck();
			} catch (VersionException e) {
				newVersion = null;
			}
			if (newVersion != null) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("        got " + newVersion);
				}
			}
		}
	}
}
