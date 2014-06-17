package org.vaadin.addons.upload;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;

/**
 * Arrange the upload components in a horizontal layout.
 * 
 * @author bogdanudrescu
 */
@SuppressWarnings("serial")
public class HorizontalUploadGroup extends UploadGroup {

	/* (non-Javadoc)
	 * @see com.example.utils.upload.UploadGroup#getComponentContainer()
	 */
	@Override
	protected ComponentContainer createComponentContainer() {
		return new HorizontalLayout();
	}

}
