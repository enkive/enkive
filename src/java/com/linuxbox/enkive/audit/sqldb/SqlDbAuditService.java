/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
 ******************************************************************************/

package com.linuxbox.enkive.audit.sqldb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.audit.AuditEntry;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceException;

public class SqlDbAuditService implements AuditService {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.audit.sqldb");

	protected static final String INSERT_STATEMENT = "INSERT INTO events (timestamp,code,username,description) VALUES (?,?,?,?);";
	protected static final String BY_ID_STATEMENT = "SELECT id,timestamp,code,username,description FROM events WHERE id=?;";
	protected static final String SEARCH_STATEMENT = "SELECT id,timestamp,code,username,description FROM events WHERE id=?;";
	protected static final String MOST_RECENT_STATEMENT = "SELECT id,timestamp,code,username,description FROM events ORDER BY timestamp DESC LIMIT ? OFFSET ?";
	protected static final String COUNT_STATEMENT = "SELECT COUNT(id) FROM events";

	private DataSource dataSource;

	/**
	 * Size of the description column in the events table. Useful if we have to
	 * truncate a long description.
	 */
	private int descriptionColumnSize;

	public SqlDbAuditService(DataSource dataSource) {
		this.dataSource = dataSource;

		descriptionColumnSize = -1;
		try {
			descriptionColumnSize = getDescriptionColumnSize();
			if (LOGGER.isInfoEnabled())
				LOGGER.info("description column size is "
						+ descriptionColumnSize);
		} catch (AuditServiceException e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("could not determine column size for description in events table");
		}
	}

	abstract class AuditOperation<R> {
		private Statement statement;
		private ResultSet resultSet;

		/**
		 * Subclasses should provide an implementation of this method that
		 * performs the actual operation. The implementation should call
		 * setStatement and setResultSet when the create such objects, so they
		 * can be cleaned up appropriately.
		 * 
		 * @param connection
		 * @return what this operation should return
		 * @throws SQLException
		 * @throws AuditServiceException
		 */
		public abstract R execute(Connection connection) throws SQLException,
				AuditServiceException;

		public R executeAuditOperation() throws AuditServiceException {
			Connection connection = null;
			try {
				connection = dataSource.getConnection();
				return execute(connection);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AuditServiceException(e);
			} finally {
				try {
					if (resultSet != null) {
						resultSet.close();
					}
				} catch (SQLException e) {
					if (LOGGER.isErrorEnabled())
						LOGGER.error("cannot close result set", e);
				}
				try {
					if (statement != null) {
						statement.close();
					}
				} catch (SQLException e) {
					if (LOGGER.isErrorEnabled())
						LOGGER.error("cannot close statement", e);
				}
				try {
					if (connection != null) {
						connection.close();
					}
				} catch (SQLException e) {
					LOGGER.error("cannot close connection", e);
				}
			}
		}

		public void setStatement(Statement statement) {
			this.statement = statement;
		}

		public void setResultSet(ResultSet resultSet) {
			this.resultSet = resultSet;
		}
	}

	@Override
	public void addEvent(final int eventCode, final String userIdentifier,
			final String description) throws AuditServiceException {
		addEvent(eventCode, userIdentifier, description, true);
	}

	@Override
	public void addEvent(final int eventCode, final String userIdentifier,
			final String description, final boolean truncateDescription)
			throws AuditServiceException {
		AuditOperation<Integer> op = new AuditOperation<Integer>() {
			@Override
			public Integer execute(Connection connection) throws SQLException {
				PreparedStatement statement = connection
						.prepareStatement(INSERT_STATEMENT);
				setStatement(statement);

				Date nowDate = new Date();
				Timestamp nowTimestamp = new Timestamp(nowDate.getTime());

				// truncate description if allowed and requested and the
				// description exists
				String descriptionAlternate = description;
				if (truncateDescription && descriptionColumnSize > 0
						&& description != null
						&& description.length() > descriptionColumnSize) {
					descriptionAlternate = description.substring(0,
							descriptionColumnSize);
				}

				// JDBC is 1-based, not 0-based
				statement.setTimestamp(1, nowTimestamp);
				statement.setInt(2, eventCode);
				statement.setString(3, userIdentifier);
				statement.setString(4, descriptionAlternate);
				Integer result = statement.executeUpdate();
				return result;
			}
		};

		op.executeAuditOperation(); // return value is ignored
	}

	@Override
	public AuditEntry getEvent(final String identifer)
			throws AuditServiceException {
		AuditOperation<AuditEntry> op = new AuditOperation<AuditEntry>() {
			@Override
			public AuditEntry execute(Connection connection)
					throws SQLException, AuditServiceException {
				PreparedStatement statement = connection
						.prepareStatement(BY_ID_STATEMENT);
				setStatement(statement);

				// JDBC is 1-based, not 0-based
				statement.setString(1, identifer);
				ResultSet resultSet = statement.executeQuery();
				setResultSet(resultSet);

				List<AuditEntry> resultList = createAuditEntries(resultSet);
				if (resultList.size() == 0) {
					return null;
				} else if (resultList.size() == 1) {
					return resultList.get(0);
				} else {
					throw new AuditServiceException(
							"severe error : audit trail lookup returned multiple results");
				}
			}
		};

		return op.executeAuditOperation();
	}

	@Override
	public List<AuditEntry> getMostRecentByPage(final int perPage,
			final int page) throws AuditServiceException {
		if (perPage < 1) {
			throw new AuditServiceException(
					"perPage must be at least 1 and should be at least 10");
		}
		if (page < 1) {
			throw new AuditServiceException("page must be at least 1");
		}

		AuditOperation<List<AuditEntry>> op = new AuditOperation<List<AuditEntry>>() {
			@Override
			public List<AuditEntry> execute(Connection connection)
					throws SQLException, AuditServiceException {
				PreparedStatement statement = connection
						.prepareStatement(MOST_RECENT_STATEMENT);
				setStatement(statement);

				// JDBC is 1-based, not 0-based
				statement.setInt(1, perPage);
				statement.setInt(2, (page - 1) * perPage);
				ResultSet resultSet = statement.executeQuery();
				setResultSet(resultSet);

				return createAuditEntries(resultSet);
			}
		};

		return op.executeAuditOperation();
	}

	@Override
	public List<AuditEntry> search(Integer eventCode, String userIdentifer,
			Date startDate, Date endDate) throws AuditServiceException {
		throw new AuditServiceException("feature not implemented yet");
	}

	@Override
	public long getAuditEntryCount() throws AuditServiceException {
		AuditOperation<Long> op = new AuditOperation<Long>() {
			@Override
			public Long execute(Connection connection) throws SQLException,
					AuditServiceException {
				long count = 0;

				PreparedStatement statement = connection
						.prepareStatement(COUNT_STATEMENT);
				setStatement(statement);

				ResultSet resultSet = statement.executeQuery();
				setResultSet(resultSet);
				if (resultSet.next()) {
					count = resultSet.getLong(1);
				} else {
					throw new AuditServiceException(
							"could not count the number of entries in the audit log");
				}

				return count;
			}
		};

		return op.executeAuditOperation();
	}

	private List<AuditEntry> createAuditEntries(ResultSet resultSet)
			throws SQLException {
		List<AuditEntry> results = new ArrayList<AuditEntry>();
		while (resultSet.next()) {
			try {
				int id = resultSet.getInt(1);
				Date timestamp = resultSet.getTimestamp(2);
				int eventCode = resultSet.getInt(3);
				String username = resultSet.getString(4);
				String description = resultSet.getString(5);
				results.add(new AuditEntry(Integer.toString(id), timestamp,
						eventCode, username, description));
			} catch (SQLException e) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Could not return audit entry", e);
			}
		}
		return results;
	}

	/**
	 * Since the description column is of finite size, we may need to truncate
	 * long descriptions to fit in the column. We need to determine the size of
	 * the column, which is likely to be described in a Liquibase changelog XML
	 * file. Rather than try to keep the Java and XML copies in sync, this just
	 * retrieves the current column size from the databases metadata.
	 * 
	 * @return
	 * @throws AuditServiceException
	 */
	public int getDescriptionColumnSize() throws AuditServiceException {
		AuditOperation<Integer> op = new AuditOperation<Integer>() {
			@Override
			public Integer execute(Connection connection) throws SQLException,
					AuditServiceException {
				int columnSize = -1;

				DatabaseMetaData metaData = connection.getMetaData();
				ResultSet resultSet = metaData.getColumns(null, null, "events",
						"description");
				setResultSet(resultSet);

				if (resultSet.next()) {
					columnSize = resultSet.getInt("COLUMN_SIZE");
					if (resultSet.next()) {
						throw new AuditServiceException(
								"retrieved multiple column metadata entries for the description column in the events table");
					}
				} else {
					throw new AuditServiceException(
							"could not retrieve the size of the description column in the events table");
				}

				return columnSize;
			}
		};

		return op.executeAuditOperation();
	}
}
