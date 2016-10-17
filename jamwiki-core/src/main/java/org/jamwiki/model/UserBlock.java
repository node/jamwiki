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
package org.jamwiki.model;

import java.io.Serializable;
import java.sql.Timestamp;
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a user block.
 */
public class UserBlock implements Serializable {

	private static final WikiLogger logger = WikiLogger.getLogger(UserBlock.class.getName());
	/** The start date for the block. */
	private Timestamp blockDate = new Timestamp(System.currentTimeMillis());
	/** The ID of the user who applied the block. */
	private int blockedByUserId = -1;
	/** The end date for the block. */
	private Timestamp blockEndDate;
	/** The internal primary key ID for the block record. */
	private int blockId = -1;
	/** The reason the block was applied. */
	private String blockReason;
	/** The IP address to block.  Either IP or user ID are required. */
	private String ipAddress;
	/** The date the block was lifted. */
	private Timestamp unblockDate;
	/** The ID of the user who lifted the block. */
	private Integer unblockedByUserId;
	/** The reason the block was lifted. */
	private String unblockReason;
	/** The ID of the user being blocked.  Either ID or IP address are required. */
	private Integer wikiUserId;

	/**
	 * Initialize a user block record with all required parameters.
	 *
	 * @param wikiUserId The ID of the user being blocked.  Either ID or IP address
	 *  must be specified when applying a block.
	 * @param ipAddress The IP address being blocked.  Either ID or IP address
	 *  must be specified when applying a block.
	 * @param blockEndDate The time when the block expires or <code>null</code> if
	 *  the block is infinite.
	 * @param blockedByUserId The ID of the user who is applying the block.
	 * @throws IllegalArgumentException Thrown if a required parameter is missing.
	 */
	public UserBlock(Integer wikiUserId, String ipAddress, Timestamp blockEndDate, int blockedByUserId) {
		if (wikiUserId == null && !Utilities.isIpAddress(ipAddress)) {
			throw new IllegalArgumentException("Either a valid user ID or a valid IP address are required when initializing a user block.");
		}
		if (blockedByUserId < 1) {
			throw new IllegalArgumentException("The ID of the user who is applying the block must be specified when initializing a UserBlock object.");
		}
		this.wikiUserId = wikiUserId;
		this.ipAddress = ipAddress;
		this.blockEndDate = blockEndDate;
		this.blockedByUserId = blockedByUserId;
	}

	/**
	 * Return the start date for the block.
	 *
	 * @return The start date for the block.
	 */
	public Timestamp getBlockDate() {
		return this.blockDate;
	}

	/**
	 * Set the start date for the block.
	 *
	 * @param blockDate The start date for the block.
	 */
	public void setBlockDate(Timestamp blockDate) {
		this.blockDate = blockDate;
	}

	/**
	 * Return the ID of the user who applied the block.
	 *
	 * @return The ID of the user who applied the block.
	 */
	public int getBlockedByUserId() {
		return this.blockedByUserId;
	}

	/**
	 * Utility method for retrieving the username of the user who applied the
	 * block.
	 */
	public String getBlockedByUsername() {
		String result = null;
		try {
			result = WikiBase.getDataHandler().lookupWikiUser(this.getBlockedByUserId()).getUsername();
		} catch (DataAccessException e) {
			logger.error("Failure while trying to retrieve username for user with ID " + this.getBlockedByUserId(), e);
		}
		return result;
	}

	/**
	 * Utility method for retrieving the username of the blocked user or the IP
	 * address if the wiki user ID is null.
	 */
	public String getBlockedUsernameOrIpAddress() {
		String result = this.getIpAddress();
		if (this.getWikiUserId() != null) {
			try {
				result = WikiBase.getDataHandler().lookupWikiUser(this.getWikiUserId()).getUsername();
			} catch (DataAccessException e) {
				logger.error("Failure while trying to retrieve username for user with ID " + this.getWikiUserId(), e);
			}
		}
		return result;
	}

	/**
	 * Return the time at which the block expires.
	 *
	 * @return The time at which the block expires or <code>null</code> if
	 *  the block is infinite.
	 */
	public Timestamp getBlockEndDate() {
		return this.blockEndDate;
	}

	/**
	 * Return the internal primary key ID for the block record.
	 *
	 * @return The internal primary key ID for the block record.
	 */
	public int getBlockId() {
		return this.blockId;
	}

	/**
	 * Set the internal primary key ID for the block record.
	 *
	 * @param blockId The internal primary key ID for the block record.
	 */
	public void setBlockId(int blockId) {
		this.blockId = blockId;
	}

	/**
	 * Return the reason the block was applied.
	 *
	 * @return The reason the block was applied or <code>null</code>
	 *  if no reason was provided.
	 */
	public String getBlockReason() {
		return this.blockReason;
	}

	/**
	 * Set the reason the block was applied.
	 *
	 * @param blockReason The reason the block was applied or <code>null</code>
	 *  if no reason was provided.
	 */
	public void setBlockReason(String blockReason) {
		this.blockReason = blockReason;
	}

	/**
	 * Return the IP address to block.  Either IP or user ID are required.
	 *
	 * @return The IP address to block.
	 */
	public String getIpAddress() {
		return this.ipAddress;
	}

	/**
	 * Return the date the block was lifted.
	 *
	 * @return The date the block was lifted or
	 *  <code>null</code> if the block was never lifted.
	 */
	public Timestamp getUnblockDate() {
		return this.unblockDate;
	}

	/**
	 * Set the date the block was lifted.
	 *
	 * @param unblockDate The date the block was lifted or
	 *  <code>null</code> if the block was never lifted.
	 */
	public void setUnblockDate(Timestamp unblockDate) {
		this.unblockDate = unblockDate;
	}

	/**
	 * Return the ID of the user who lifted the block.
	 *
	 * @return The date the block was lifted or
	 *  <code>null</code> if the block was never lifted.
	 */
	public Integer getUnblockedByUserId() {
		return this.unblockedByUserId;
	}

	/**
	 * Set the ID of the user who lifted the block.
	 *
	 * @param unblockedByUserId The date the block was lifted or
	 *  <code>null</code> if the block was never lifted.
	 */
	public void setUnblockedByUserId(Integer unblockedByUserId) {
		this.unblockedByUserId = unblockedByUserId;
	}

	/**
	 * Utility method for retrieving the username of the user who lifted the
	 * block or <code>null</code> if the block was never lifted.
	 */
	public String getUnblockedByUsername() {
		String result = null;
		if (this.getUnblockedByUserId() != null) {
			try {
				result = WikiBase.getDataHandler().lookupWikiUser(this.getUnblockedByUserId()).getUsername();
			} catch (DataAccessException e) {
				logger.error("Failure while trying to retrieve username for user with ID " + this.getUnblockedByUserId(), e);
			}
		}
		return result;
	}

	/**
	 * Return the reason the block was lifted.
	 *
	 * @return The reason the block was lifted or <code>null</code>
	 *  if no reason was provided or the block was never lifted.
	 */
	public String getUnblockReason() {
		return this.unblockReason;
	}

	/**
	 * Set the reason the block was lifted.
	 *
	 * @param unblockReason The reason the block was lifted or <code>null</code>
	 *  if no reason was provided or the block was never lifted.
	 */
	public void setUnblockReason(String unblockReason) {
		this.unblockReason = unblockReason;
	}

	/**
	 * Return the ID of the user being blocked.  Either ID or IP address are required.
	 *
	 * @return The ID of the user being blocked.
	 */
	public Integer getWikiUserId() {
		return this.wikiUserId;
	}

	/**
	 * Utility method for determining if a block is expired or not.
	 *
	 * @return <code>true</code> if the block is not infinite and its end date is
	 *  in the past.
	 */
	public boolean isExpired() {
		return (this.getBlockEndDate() != null && this.getBlockEndDate().getTime() < System.currentTimeMillis());
	}
}