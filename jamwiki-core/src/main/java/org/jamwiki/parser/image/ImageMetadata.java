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
package org.jamwiki.parser.image;

/**
 * Utility method used to model image metadata including alignment, max dimension, etc.
 */
public class ImageMetadata {

	/** Flag indicating whether the image can be resized larger than its current size. */
	private boolean allowEnlarge = true;
	private String alt = null;
	private boolean bordered = false;
	private ImageBorderEnum border = ImageBorderEnum.NOT_SPECIFIED;
	private String caption = null;
	private int galleryHeight = 0;
	private ImageHorizontalAlignmentEnum horizontalAlignment = ImageHorizontalAlignmentEnum.NOT_SPECIFIED;
	/**
	 * Destination to link to when the image is clicked.  May be either a topic or a URL.  If
	 * this value is an empty string then no link is generated.
	 */
	private String link = null;
	/**
	 * A value in pixels indicating the maximum height value allowed for the image.  If this
	 * value is greater than zero then the image will be resized so that the height does not
	 * exceed this value.
	 */
	private int maxHeight = -1;
	/**
	 * A value in pixels indicating the maximum width value allowed for the image.  If this
	 * value is greater than zero then the image will be resized so that the width does not
	 * exceed this value.
	 */
	private int maxWidth = -1;
	private ImageVerticalAlignmentEnum verticalAlignment = ImageVerticalAlignmentEnum.NOT_SPECIFIED;
	/** Link title for images that are wrapped by a link. */
	private String title = null;

	/**
	 * Return a flag indicating whether the image can be resized larger than its
	 * actual dimensions.
	 */
	public boolean getAllowEnlarge() {
		return this.allowEnlarge;
	}

	/**
	 * Set a flag indicating whether the image can be resized larger than its
	 * actual dimensions.
	 */
	public void setAllowEnlarge(boolean allowEnlarge) {
		this.allowEnlarge = allowEnlarge;
	}

	/**
	 *
	 */
	public String getAlt() {
		return this.alt;
	}

	/**
	 *
	 */
	public void setAlt(String alt) {
		this.alt = alt;
	}

	/**
	 * An image can be both frameless and bordered, so add an additional field
	 * to hold the bordered property.
	 */
	public boolean getBordered() {
		return this.bordered;
	}

	/**
	 * An image can be both frameless and bordered, so add an additional field
	 * to hold the bordered property.
	 */
	public void setBordered(boolean bordered) {
		this.bordered = bordered;
	}

	/**
	 *
	 */
	public ImageBorderEnum getBorder() {
		return this.border;
	}

	/**
	 *
	 */
	public void setBorder(ImageBorderEnum border) {
		this.border = border;
	}

	/**
	 *
	 */
	public String getCaption() {
		return this.caption;
	}

	/**
	 *
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * Gallery height is used for adding padding around image gallery images.
	 * Specify the total vertical space available for display, in pixels.
	 */
	public int getGalleryHeight() {
		return this.galleryHeight;
	}

	/**
	 * Gallery height is used for adding padding around image gallery images.
	 * Specify the total vertical space available for display, in pixels.
	 */
	public void setGalleryHeight(int galleryHeight) {
		this.galleryHeight = galleryHeight;
	}

	/**
	 *
	 */
	public ImageHorizontalAlignmentEnum getHorizontalAlignment() {
		return this.horizontalAlignment;
	}

	/**
	 *
	 */
	public void setHorizontalAlignment(ImageHorizontalAlignmentEnum horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	/**
	 *
	 */
	public String getLink() {
		return this.link;
	}

	/**
	 *
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 *
	 */
	public int getMaxHeight() {
		return this.maxHeight;
	}

	/**
	 *
	 */
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	/**
	 *
	 */
	public int getMaxWidth() {
		return this.maxWidth;
	}

	/**
	 *
	 */
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	/**
	 *
	 */
	public ImageVerticalAlignmentEnum getVerticalAlignment() {
		return this.verticalAlignment;
	}

	/**
	 *
	 */
	public void setVerticalAlignment(ImageVerticalAlignmentEnum verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	/**
	 * Return the HTML title tag to use with the link surrounding the
	 * image tag.  This value will not have been escaped so the link
	 * builder will need to escape it.
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Return the HTML title tag to use with the link surrounding the
	 * image tag.  This value will not have been escaped so the link
	 * builder will need to escape it.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
}
