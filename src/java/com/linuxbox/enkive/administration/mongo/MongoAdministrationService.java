/**
 * 
 */
package com.linuxbox.enkive.administration.mongo;

import java.util.Date;

import org.bson.types.ObjectId;

import com.linuxbox.enkive.administration.AbstractAdministrationService;
import com.linuxbox.util.EphemeralVersion;
import com.linuxbox.util.Version;
import com.linuxbox.util.Version.VersionException;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * Store administrative information in MongoDB
 * @author dang
 *
 */
public class MongoAdministrationService extends AbstractAdministrationService {

	public static final String MONGO_ID = "_id";
	public static final String INSTANCE_UID = "instanceUID";
	public static final String VERSION_CHECK = "versionCheck";
	public static final String TIMESTAMP = "timestamp";
	public static final String VERSION_ORDINAL = "versionOrdinal";
	public static final String VERSION_STRING = "versionString";
	
	protected DBCollection adminColl;
	protected String UID;
	protected Version version;
	
	public MongoAdministrationService(MongoClient m, String dbName, String adminCollName) {
		super();
		DB workspaceDb = m.getDB(dbName);
		adminColl = workspaceDb.getCollection(adminCollName);
	}
	
	public MongoAdministrationService(MongoDbInfo info) {
		super();
		adminColl = info.getCollection();
	}
	
	/**
	 * Spring startup.  Make sure we always have a UID.
	 */
	public void startup() {
		super.startup();
		getUID();
		try {
			updateCheck();
		} catch (VersionException e) {
			// Nothing to do
		}
	}
	
	/**
	 * "And now, because you can't have one without the other... The Hawiian War Chant!"
	 */
	public void shutdown() {
		super.shutdown();
	}


	/* (non-Javadoc)
	 * @see com.linuxbox.enkive.administration.AdministrationService#getUID()
	 */
	@Override
	public String getUID() {
		if (UID == null) {
			DBObject UIDObject = adminColl.findOne(INSTANCE_UID);
			if (UIDObject == null) {
				UID = setInstanceID(ObjectId.get().toString());
			} else {
				UID = UIDObject.get(INSTANCE_UID).toString();
			}
		}
		
		return UID;
	}

	/* (non-Javadoc)
	 * @see com.linuxbox.enkive.administration.AdministrationService#setUID(java.lang.String)
	 */
	@Override
	public void setUID(String UID) {
		this.UID = setInstanceID(UID);
	}

	/* (non-Javadoc)
	 * @see com.linuxbox.enkive.administration.AbstractAdministrationService#saveVersionCheck(com.linuxbox.util.Version)
	 */
	protected void saveVersionCheck(Version newVersion) {
		DBObject checkObject = adminColl.findOne(VERSION_CHECK);
		if (checkObject == null) {
			checkObject = new BasicDBObject();
			checkObject.put(MONGO_ID, VERSION_CHECK);
			adminColl.insert(checkObject);
		}
		DBObject updateObject = new BasicDBObject();
		updateObject.put(TIMESTAMP, new Date());
		updateObject.put(VERSION_ORDINAL, newVersion.versionOrdinal);
		updateObject.put(VERSION_STRING, newVersion.versionString);
		adminColl.update(checkObject, updateObject);
		version = newVersion;
	}
	
	/* (non-Javadoc)
	 * @see com.linuxbox.enkive.administration.AdministrationService#getVersion()
	 */
	@Override
	public Version getVersion() {
		if (version == null) {
			DBObject checkObject = adminColl.findOne(VERSION_CHECK);
			if (checkObject != null) {
				String verString = checkObject.get(VERSION_STRING).toString();
				int verOrd = Integer.parseInt(checkObject.get(VERSION_ORDINAL).toString());
				version = new EphemeralVersion(verString, verOrd);
			}
		}
		
		return version;
	}

	private String setInstanceID(String newUID) {
		DBObject UIDObject = adminColl.findOne(INSTANCE_UID);
		if (UIDObject == null) {
			UIDObject = new BasicDBObject();
			UIDObject.put(MONGO_ID, INSTANCE_UID);
			UIDObject.put(INSTANCE_UID, newUID);
			adminColl.insert(UIDObject);
		} else {
			DBObject updateObject = new BasicDBObject();
			updateObject.put(INSTANCE_UID, newUID);
			adminColl.update(UIDObject, updateObject);
		}
		
		return newUID;
	}

}
