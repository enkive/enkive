package com.linuxbox.enkive;

import com.linuxbox.util.Version;
import com.linuxbox.util.dbmigration.DbVersionManager;

public class EnkiveDbVersionManager extends DbVersionManager {
	public EnkiveDbVersionManager() throws DbVersionManagerException {
		super();
		associateHelp(ProductInfo.V1_2P, 0);
		associateHelp(ProductInfo.V1_3P, 1);
		associateHelp(ProductInfo.V1_3_1P, 1);
		associateHelp(ProductInfo.V1_4RC, 2);
		associateHelp(ProductInfo.V1_4RC2, 3);
		associateHelp(ProductInfo.V1_4P, 3);
		associateHelp(ProductInfo.V1_4_1RC1, 3);
	}

	protected void associateHelp(Version swVersion, int dbVersion)
			throws DbVersionManagerException {
		DbVersion dbv = new DbVersion(dbVersion);
		associate(swVersion, dbv);
	}
}
