package org.vaadin.addons.upload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vaadin.addons.upload.UploadProducer.UploadProducerAdapter;
import org.vaadin.addons.upload.UploadProgress.UploadStatus;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;

/**
 * Layout {@link UploadProgress} components which are produced by a {@link UploadProducer}.
 * 
 * Right now dummy class just to test the producer. Well ... not so dummy anymore :)
 * 
 * @author bogdanudrescu
 */
@SuppressWarnings("serial")
public abstract class UploadGroup extends Panel {

	/*
	 * Produce and manages the UploadProgress components.
	 */
	private UploadProducer producer;

	/*
	 * The UploadProducer listener.
	 */
	private UploadProducerHandler handler = new UploadProducerHandler();

	/*
	 * The layout where to add the producers.
	 */
	private ComponentContainer componentContainer;

	/**
	 * Create a default upload group component.
	 */
	public UploadGroup() {
		init();
	}

	/*
	 * Initialize the group.
	 */
	private void init() {
		// First create the layout container.
		componentContainer = createComponentContainer();
		componentContainer.setSizeFull();
		setContent(componentContainer);

		// Then create the producer.
		producer = new UploadProducer(handler);
	}

	/**
	 * Gets the UploadProducer object which actually manage the upload components.
	 * @return	the upload producer.
	 */
	public UploadProducer getProducer() {
		return producer;
	}

	/**
	 * Remove all uploads from the producer and the UI components.
	 */
	public void removeAllUploads() {
		// Put all components to remove in the list first.
		List<UploadProgress> uploadProgresses = new ArrayList<>(componentContainer.getComponentCount());
		Iterator<Component> iterator = componentContainer.iterator();
		while (iterator.hasNext()) {
			UploadProgress uploadProgress = (UploadProgress) iterator.next();
			if (uploadProgress.getStatus() != UploadStatus.NONE) {
				uploadProgresses.add(uploadProgress);
			}
		}

		// Then remove them.
		Iterator<UploadProgress> uploadIterator = uploadProgresses.iterator();
		while (uploadIterator.hasNext()) {
			UploadProgress uploadProgress = uploadIterator.next();
			producer.removeUpload(uploadProgress);
		}

		// TODO: Move this in UploadProducer.
		// FIXME: ASK: What's faster?

		//		init();
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.AbstractComponent#detach()
	 */
	@Override
	public void detach() {
		producer.removeUploadProducerListener(handler);

		handler = null;

		super.detach();
	}

	/**
	 * Creates the component container where the UploadProgress components will be added.
	 * @return	a ComponentContainer object, most likely a layout.
	 */
	protected abstract ComponentContainer createComponentContainer();

	/*
	 * Handle the UploadProducer notifications.
	 */
	private class UploadProducerHandler extends UploadProducerAdapter {

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProducer.UploadProducerAdapter#uploadProgressProduced(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void uploadProgressProduced(UploadProgress uploadProgress) {
			componentContainer.addComponent(uploadProgress);
		}

		/* (non-Javadoc)
		 * @see com.example.utils.upload.UploadProducer.UploadProducerAdapter#shouldRemoveUploadProgress(com.example.utils.upload.UploadProgress)
		 */
		@Override
		public void shouldRemoveUploadProgress(UploadProgress uploadProgress) {
			componentContainer.removeComponent(uploadProgress);
		}

	}

}
